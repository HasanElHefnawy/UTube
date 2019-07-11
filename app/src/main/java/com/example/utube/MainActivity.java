package com.example.utube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoEntry;
import com.example.utube.databinding.ActivityMainBinding;
import com.example.utube.model.Video;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz MainActivity";
    private List<VideoEntry> videoEntries;
    private VideoAdapter adapter;
    private RetrofitApiService retrofitApiService;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(this);

        loadVideosFromDatabase();

        retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);

        disposable.add(RxTextView.textChangeEvents(binding.searchEditText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMap(new Function<TextViewTextChangeEvent, Observable<Videos>>() {
                    @Override
                    public Observable<Videos> apply(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return getObservableAllVideos(textViewTextChangeEvent);
                    }
                })
                .map(new Function<Videos, List<Videos.Item>>() {
                    @Override
                    public List<Videos.Item> apply(Videos videos) {
                        return videos.getItems();
                    }
                })
                .flatMap(new Function<List<Videos.Item>, ObservableSource<Videos.Item>>() {
                    @Override
                    public ObservableSource<Videos.Item> apply(List<Videos.Item> items) {
                        if (items.size() != 0) {
                            dataBaseExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    mDb.clearAllTables();
                                }
                            });
                        }
                        return Observable.fromIterable(items);
                    }
                })
                .subscribeWith(getDisposableObserverVideos())
        );
    }

    private Observable<Videos> getObservableAllVideos(TextViewTextChangeEvent textViewTextChangeEvent) {
        return retrofitApiService.getAllVideos(
                "search",
                textViewTextChangeEvent.text().toString(),
                "snippet",
                "video",
                sharedPreferences.getString(getString(R.string.setting_max_results_key), "5"),
                sharedPreferences.getString(getString(R.string.setting_video_duration_key), "any")
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private DisposableObserver<Videos.Item> getDisposableObserverVideos() {
        return new DisposableObserver<Videos.Item>() {
            @Override
            public void onNext(Videos.Item item) {
                Log.e(TAG, "onNext: getDisposableObserverVideos");
                final String videoId = item.getId().getVideoId();
                final String title = item.getSnippet().getTitle();
                final String thumbnailsUrl = item.getSnippet().getThumbnails().getDefault().getUrl();
                final DateTime publishedAt = DateTime.parse(item.getSnippet().getPublishedAt());
                getObservableVideoDuration(item, videoId, title, thumbnailsUrl, publishedAt);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e(TAG, "onError: getDisposableObserverVideos " + throwable);
                Toast.makeText(MainActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: getDisposableObserverVideos");
            }
        };
    }

    private void getObservableVideoDuration(Videos.Item item, final String videoId, final String title, final String thumbnailsUrl, final DateTime publishedAt) {
        final String[] duration = new String[1];
        retrofitApiService.getVideoDuration(
                "videos",
                item.getId().getVideoId(),
                "contentDetails")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<Video>() {
                    @Override
                    public void onNext(Video video) {
                        Log.e(TAG, "onNext: getObservableVideoDuration");
                        duration[0] = parseDuration(video.getItems().get(0).getContentDetails().getDuration());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: getObservableVideoDuration " + throwable);
                        Toast.makeText(MainActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: getObservableVideoDuration");
                        dataBaseExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.videoDao().insertVideo(new VideoEntry(videoId, title, thumbnailsUrl, publishedAt, duration[0]));
                            }
                        });
                        loadVideosFromDatabase();
                    }
                });
    }

    private void loadVideosFromDatabase() {
        dataBaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                videoEntries = mDb.videoDao().getAllVideos();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter = new VideoAdapter(videoEntries, MainActivity.this);
                        binding.recyclerView.setAdapter(adapter);
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    }
                });
            }
        });
    }

    private String parseDuration(String duration) {
        Period period = Period.parse(duration);
        int hours = period.getHours();
        int mins = period.getMinutes();
        int secs = period.getSeconds();
        if (hours == 0)
            return String.format(getResources().getStringArray(R.array.duration)[0], mins, secs);
        else
            return String.format(getResources().getStringArray(R.array.duration)[1], hours, mins, secs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
