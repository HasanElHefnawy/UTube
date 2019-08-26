package com.example.utube.datasource;

import android.arch.paging.PageKeyedDataSource;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.AppExecutor;
import com.example.utube.R;
import com.example.utube.database.AppDatabase;
import com.example.utube.model.Video;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.example.utube.database.AppDatabase.DATABASE_NAME;
import static com.example.utube.database.AppDatabase.TABLE_NAME;

public class NetworkDataSource extends PageKeyedDataSource<String, Videos.Item> {
    private static final String TAG = "zzzz NetworkDataSource";
    private Context context;
    private SharedPreferences sharedPreferences;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;
    private CompositeDisposable disposable = new CompositeDisposable();
    private RetrofitApiService retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);
    private String nextPageToken;
    private String query;

    NetworkDataSource(Context context, String query) {
        this.context = context;
        this.query = query;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params,
                            @NonNull final LoadInitialCallback<String, Videos.Item> callback) {
        Log.e(TAG, "loadInitial: params.requestedLoadSize " + params.requestedLoadSize);
        Log.e(TAG, "loadInitial: query " + query);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(context);
        disposable.clear();
        disposable.add(Observable.just(query)
                .switchMap(new Function<String, Observable<Videos.Item>>() {
                    @Override
                    public Observable<Videos.Item> apply(String query) {
                        return getObservableAllVideos("")
                                .map(new Function<Videos, List<Videos.Item>>() {
                                    @Override
                                    public List<Videos.Item> apply(Videos videos) {
                                        nextPageToken = videos.getNextPageToken();
                                        Log.e(TAG, "loadInitial map: nextPageToken " + nextPageToken);
                                        callback.onResult(videos.getItems(), null, nextPageToken);
                                        Log.e(TAG, "loadInitial callback.onResult ");
                                        return videos.getItems();
                                    }
                                })
                                .flatMap(new Function<List<Videos.Item>, Observable<Videos.Item>>() {
                                    @Override
                                    public Observable<Videos.Item> apply(List<Videos.Item> items) {
                                        if (items.size() != 0) {
                                            dataBaseExecutor.execute(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mDb.videoDao().insertVideo(new Videos.Item());
                                                    int sizeBefore = mDb.videoDao().getAllVideos(0, 1000).size();
                                                    Log.e(TAG, "loadInitial map sizeBefore " + sizeBefore);
                                                    deleteAndReset();
                                                    Log.e(TAG, "loadInitial map: deleteAndReset ");
                                                    int sizeAfter = mDb.videoDao().getAllVideos(0, 1000).size();
                                                    Log.e(TAG, "loadInitial map sizeAfter " + sizeAfter);
                                                }
                                            });
                                        }
                                        return Observable.fromIterable(items);
                                    }
                                })
                                .concatMap(new Function<Videos.Item, Observable<Videos.Item>>() {
                                    @Override
                                    public Observable<Videos.Item> apply(final Videos.Item item) {
                                        return getObservableVideoDuration(item);
                                    }
                                });
                    }
                })
                .subscribeWith(getDisposableObserverVideos())
        );
    }

    @Override
    public void loadBefore(@NonNull final LoadParams<String> params,
                           @NonNull final LoadCallback<String, Videos.Item> callback) {
        Log.e(TAG, "loadBefore: params.key " + params.key + "\tparams.requestedLoadSize " + params.requestedLoadSize);
        Log.e(TAG, "loadBefore: query " + query);
    }

    @Override
    public void loadAfter(@NonNull final LoadParams<String> params,
                          @NonNull final LoadCallback<String, Videos.Item> callback) {
        Log.e(TAG, "loadAfter: params.key " + params.key + "\tparams.requestedLoadSize " + params.requestedLoadSize);
        Log.e(TAG, "loadAfter: query " + query);
        disposable.add(Observable.just(query)
                .switchMap(new Function<String, Observable<Videos.Item>>() {
                    @Override
                    public Observable<Videos.Item> apply(String query) {
                        return getObservableAllVideos(params.key)
                                .map(new Function<Videos, List<Videos.Item>>() {
                                    @Override
                                    public List<Videos.Item> apply(Videos videos) {
                                        nextPageToken = videos.getNextPageToken();
                                        Log.e(TAG, "loadAfter map: nextPageToken " + nextPageToken);
                                        return videos.getItems();
                                    }
                                })
                                .flatMap(new Function<List<Videos.Item>, Observable<Videos.Item>>() {
                                    @Override
                                    public Observable<Videos.Item> apply(List<Videos.Item> items) {
                                        callback.onResult(items, nextPageToken);
                                        Log.e(TAG, "loadAfter callback.onResult ");
                                        return Observable.fromIterable(items);
                                    }
                                })
                                .concatMap(new Function<Videos.Item, Observable<Videos.Item>>() {
                                    @Override
                                    public Observable<Videos.Item> apply(final Videos.Item item) {
                                        return getObservableVideoDuration(item);
                                    }
                                });
                    }
                })
                .subscribeWith(getDisposableObserverVideos())
        );
    }

    private Observable<Videos> getObservableAllVideos(String page) {
        return retrofitApiService.getAllVideos(
                "search",
                query,
                "snippet",
                "video",
                page,
                sharedPreferences.getString(context.getString(R.string.setting_max_results_key), "5"),
                sharedPreferences.getString(context.getString(R.string.setting_video_duration_key), "any")
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Videos.Item> getObservableVideoDuration(final Videos.Item item) {
        Log.e(TAG, "getObservableVideoDuration: " + item.getId().getVideoId() + "\t" + item.getDuration());
        return retrofitApiService.getVideoDuration(
                "videos",
                item.getId().getVideoId(),
                "contentDetails"
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<Video, Videos.Item>() {
                    @Override
                    public Videos.Item apply(Video video) {
                        String duration = video.getItems().get(0).getContentDetails().getDuration();
                        item.setDuration(duration);
                        return item;
                    }
                });
    }

    private DisposableObserver<Videos.Item> getDisposableObserverVideos() {
        Log.e(TAG, "getDisposableObserverVideos: ");
        return new DisposableObserver<Videos.Item>() {
            @Override
            public void onNext(final Videos.Item item) {
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int sizeBefore = mDb.videoDao().getAllVideos(0, 1000).size();
                        Log.e(TAG, "onNext: getDisposableObserverVideos sizeBefore " + sizeBefore);
                        mDb.videoDao().insertVideo(item);
                        Log.e(TAG, "onNext: getDisposableObserverVideos item " + item);
                        Log.e(TAG, "onNext: getDisposableObserverVideos " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle() + "\t" + item.getDuration());
                        int sizeAfter = mDb.videoDao().getAllVideos(0, 1000).size();
                        Log.e(TAG, "onNext: getDisposableObserverVideos sizeAfter " + sizeAfter);
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "onError: getDisposableObserverVideos " + throwable);
                if (throwable instanceof HttpException) {
                    Response response = ((HttpException) throwable).response();
                    Log.e(TAG, "onError: response " + response);
                    if (response != null && response.errorBody() != null) {
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().string());
                            Toast.makeText(context, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: getDisposableObserverVideos");
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int size = mDb.videoDao().getAllVideos(0, 1000).size();
                        Log.e(TAG, "onComplete: getDisposableObserverVideos size " + size);
                    }
                });
            }
        };
    }

    private void deleteAndReset() {
        SQLiteDatabase database;
        database = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME), null);
        String deleteTable = "DELETE FROM " + TABLE_NAME;
        database.execSQL(deleteTable);
        String deleteSqliteSequence = "DELETE FROM sqlite_sequence WHERE name = '" + TABLE_NAME + "'";
        database.execSQL(deleteSqliteSequence);
    }
}
