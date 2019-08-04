package com.example.utube.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.utube.AppExecutor;
import com.example.utube.ItemClickSupport;
import com.example.utube.R;
import com.example.utube.VideoAdapter;
import com.example.utube.database.AppDatabase;
import com.example.utube.databinding.ActivityMainBinding;
import com.example.utube.model.Video;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;
import com.example.utube.viewmodel.MainViewModelDatabase;
import com.example.utube.viewmodel.MainViewModelNetwork;
import com.example.utube.viewmodel.MainViewModelNetworkFactory;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import org.json.JSONObject;

import java.util.ArrayList;
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
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz MainActivity";
    private VideoAdapter adapter;
    private List<Videos.Item> videoItems;
    private RetrofitApiService retrofitApiService;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ActivityMainBinding binding;
    private SharedPreferences sharedPreferences;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;
    private MainViewModelDatabase mainViewModelDatabase;
    private MainViewModelNetwork mainViewModelNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(this);
        retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);
        mainViewModelDatabase = ViewModelProviders.of(this).get(MainViewModelDatabase.class);
        MainViewModelNetworkFactory mainViewModelNetworkFactory = new MainViewModelNetworkFactory(binding.searchEditText);
        mainViewModelNetwork = ViewModelProviders.of(this, mainViewModelNetworkFactory).get(MainViewModelNetwork.class);

        videoItems = new ArrayList<>();
        adapter = new VideoAdapter(videoItems, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addWinkToEmptyListTextView();
        loadVideosFromDatabase();

        ItemClickSupport.addTo(binding.recyclerView).setOnItemLongClickListener(
                new ItemClickSupport.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClicked(final RecyclerView recyclerView, final int position, final View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        View.inflate(MainActivity.this, R.layout.dialog, null);
                        CharSequence[] dialogButtons = new CharSequence[]{
                                getString(R.string.update),
                                getString(R.string.delete),
                                getString(android.R.string.cancel)};
                        builder.setItems(dialogButtons,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                Intent editorIntent = new Intent(MainActivity.this, EditorActivity.class);
                                                editorIntent.putExtra("idPrimaryKey", (int) v.getTag());
                                                startActivity(editorIntent);
                                                break;
                                            case 1:
                                                dataBaseExecutor.execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDb.videoDao().deleteVideo(videoItems.get(position));
                                                    }
                                                });
                                                break;
                                            case 2:
                                                break;
                                        }
                                    }
                                });
                        builder.create().show();
                        return true;
                    }
                });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        mDb.videoDao().deleteVideo(videoItems.get(position));
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerView);

        binding.searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainViewModelNetwork.setTextViewTextChangeEvent(RxTextView.textChangeEvents(binding.searchEditText));
                loadVideosOverInternetWhenTextChange();
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void loadVideosOverInternetWhenTextChange() {
        if (!isConnected()) {
            Toast.makeText(MainActivity.this, "There is no network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        disposable.clear();
        disposable.add(mainViewModelNetwork.getTextViewTextChangeEvent(binding.searchEditText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMap(new Function<TextViewTextChangeEvent, Observable<Videos.Item>>() {
                    @Override
                    public Observable<Videos.Item> apply(TextViewTextChangeEvent textViewTextChangeEvent) {
                        Log.e(TAG, "apply: " + textViewTextChangeEvent.text());
                        return getObservableAllVideos(textViewTextChangeEvent)
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
                                .concatMap(new Function<Videos.Item, ObservableSource<Videos.Item>>() {
                                    @Override
                                    public ObservableSource<Videos.Item> apply(final Videos.Item item) {
                                        return getObservableVideoDuration(item);
                                    }
                                });
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

    private ObservableSource<Videos.Item> getObservableVideoDuration(final Videos.Item item) {
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
        return new DisposableObserver<Videos.Item>() {
            @Override
            public void onNext(final Videos.Item item) {
                Log.e(TAG, "onNext: getDisposableObserverVideos " + item.getSnippet().getTitle());
                videoItems.add(item);
                adapter.notifyDataSetChanged();
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.videoDao().insertVideo(item);
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
                            Toast.makeText(MainActivity.this, jObjError.getJSONObject("error").getString("message"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

            @Override
            public void onComplete() {
                Log.e(TAG, "onComplete: getDisposableObserverVideos");
            }
        };
    }

    private void loadVideosFromDatabase() {
        mainViewModelDatabase.getVideoItemsListLiveData().observe(this, new Observer<List<Videos.Item>>() {
            @Override
            public void onChanged(@Nullable List<Videos.Item> items) {
                if (items != null) {
                    Log.e(TAG, "Updating list of video items from LiveData in ViewModel");
                    videoItems.clear();
                    videoItems.addAll(items);
                    adapter.notifyDataSetChanged();
                    if (adapter.getItemCount() != 0) binding.emptyList.setVisibility(View.GONE);
                }
            }
        });
    }

    private void addWinkToEmptyListTextView() {
        Html.ImageGetter imageGetter = new Html.ImageGetter() {
            @Override
            public Drawable getDrawable(String source) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_wink);
                drawable.setBounds(0, -5, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                return drawable;
            }
        };
        Spanned spanned = Html.fromHtml(
                getString(R.string.empty_list) + " <img src='" + getResources().getDrawable(R.drawable.ic_wink) + "'/>",
                imageGetter,
                null
        );
        binding.emptyList.setText(spanned);
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
