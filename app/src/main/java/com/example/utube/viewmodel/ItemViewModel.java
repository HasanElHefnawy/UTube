package com.example.utube.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.DataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.AppExecutor;
import com.example.utube.R;
import com.example.utube.database.AppDatabase;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class ItemViewModel extends ViewModel {
    private static final String TAG = "zzzz ItemViewModel";
    private LiveData<PagedList<Videos.Item>> databasePagedList;
    private Application application;
    private String query;
    private AppDatabase mDb;
    private SharedPreferences sharedPreferences;
    private CompositeDisposable disposable = new CompositeDisposable();
    private RetrofitApiService retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);
    private String nextPageToken;
    private boolean isRequestInProgress = false;
    private BoundaryCallbackListener boundaryCallbackListener;

    public interface BoundaryCallbackListener {
        void onItemAtEndLoaded();

        void onLoadingNewItemsCompleted();
    }

    public ItemViewModel(Application application, String query) {
        Log.e(TAG, "ItemViewModel: ");
        this.application = application;
        this.query = query;
        mDb = AppDatabase.getInstance(application);
        // The database must have 25 entries at least before uploading to firebase storage!!!
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        String sizeString = sharedPreferences.getString(application.getString(R.string.setting_max_results_key), application.getString(R.string.setting_max_results_default_value));
        int size = 15;
        if (sizeString != null)
            size = Integer.valueOf(sizeString);
        nextPageToken = sharedPreferences.getString("nextPageToken", "");
        Log.e(TAG, "ItemViewModel: nextPageToken " + nextPageToken);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setInitialLoadSizeHint(size)
                .setPageSize(size)
                .build();
        Executor executor = Executors.newFixedThreadPool(3);
        PagedList.BoundaryCallback<Videos.Item> boundaryCallback = new PagedList.BoundaryCallback<Videos.Item>() {
            @Override
            public void onItemAtEndLoaded(@NonNull Videos.Item itemAtEnd) {
                Log.e(TAG, "onItemAtEndLoaded: ");
                super.onItemAtEndLoaded(itemAtEnd);
                boundaryCallbackListener.onItemAtEndLoaded();
                fetchVideosFromInternetAndStoreInDatabase();
            }

            @Override
            public void onZeroItemsLoaded() {
                Log.e(TAG, "onZeroItemsLoaded: ");
                super.onZeroItemsLoaded();
                fetchVideosFromInternetAndStoreInDatabase();
            }
        };

        DataSource.Factory<Integer, Videos.Item> getAllVideos2 = mDb.videoDao().getQueryVideos("%" + query + "%");   // percent sign (%) wildcard to find any values using SQLite LIKE operator
        databasePagedList = new LivePagedListBuilder<>(getAllVideos2, config)
                .setFetchExecutor(executor)
                .setBoundaryCallback(boundaryCallback)
                .build();
    }

    private void fetchVideosFromInternetAndStoreInDatabase() {
        Log.e(TAG, "fetchVideosFromInternetAndStoreInDatabase: isRequestInProgress " + isRequestInProgress);
        if (isRequestInProgress) return;
        isRequestInProgress = true;
        disposable.clear();
        disposable.add(Observable.just(query)
                .switchMap((Function<String, Observable<Videos.Item>>) query -> getObservableAllVideos(nextPageToken)
                        .map(videos -> {
                            nextPageToken = videos.getNextPageToken();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("nextPageToken", nextPageToken);
                            editor.apply();
                            Log.e(TAG, "fetchVideosFromInternetAndStoreInDatabase map: nextPageToken " + nextPageToken);
                            return videos.getItems();
                        })
                        .flatMap((Function<List<Videos.Item>, Observable<Videos.Item>>) Observable::fromIterable)
                        .concatMap((Function<Videos.Item, Observable<Videos.Item>>) this::getObservableVideoDuration))
                .subscribeWith(getDisposableObserverVideos())
        );
    }

    public LiveData<PagedList<Videos.Item>> getDatabasePagedList() {
        Log.e(TAG, "getDatabasePagedList: databasePagedList " + databasePagedList);
        Log.e(TAG, "getDatabasePagedList: databasePagedList.getValue() " + databasePagedList.getValue());
        return databasePagedList;
    }

    private Observable<Videos> getObservableAllVideos(String page) {
        return retrofitApiService.getAllVideos(
                "search",
                query,
                "snippet",
                "video",
                page,
                sharedPreferences.getString(application.getString(R.string.setting_max_results_key), "5"),
                sharedPreferences.getString(application.getString(R.string.setting_video_duration_key), "any")
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
                .map(video -> {
                    String duration = video.getItems().get(0).getContentDetails().getDuration();
                    item.setDuration(duration);
                    return item;
                });
    }

    private DisposableObserver<Videos.Item> getDisposableObserverVideos() {
        Log.e(TAG, "getDisposableObserverVideos: ");
        final Executor dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        return new DisposableObserver<Videos.Item>() {
            @Override
            public void onNext(final Videos.Item item) {
                dataBaseExecutor.execute(() -> {
                    int sizeBefore = mDb.videoDao().getAllVideos().size();
                    Log.e(TAG, "onNext: getDisposableObserverVideos sizeBefore " + sizeBefore);
                    mDb.videoDao().insertVideo(item);
                    Log.e(TAG, "onNext: getDisposableObserverVideos item " + item);
                    Log.e(TAG, "onNext: getDisposableObserverVideos " + item.getIdPrimaryKey() + "\t" + item.getSnippet().getTitle() + "\t" + item.getDuration());
                    int sizeAfter = mDb.videoDao().getAllVideos().size();
                    Log.e(TAG, "onNext: getDisposableObserverVideos sizeAfter " + sizeAfter);
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
                            Toast.makeText(application, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(application, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
                boundaryCallbackListener.onLoadingNewItemsCompleted();
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: getDisposableObserverVideos");
                dataBaseExecutor.execute(() -> {
                    int size = mDb.videoDao().getAllVideos().size();
                    Log.e(TAG, "onComplete: getDisposableObserverVideos size " + size);
                });
                isRequestInProgress = false;
                boundaryCallbackListener.onLoadingNewItemsCompleted();
            }
        };
    }

    public void setBoundaryCallbackListener(BoundaryCallbackListener boundaryCallbackListener) {
        this.boundaryCallbackListener = boundaryCallbackListener;
    }
}
