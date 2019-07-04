package com.example.utube;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Toast;

import com.example.utube.databinding.ActivityMainBinding;
import com.example.utube.model.Videos;
import com.example.utube.network.RetrofitApiClient;
import com.example.utube.network.RetrofitApiService;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz MainActivity";
    private List<Videos.Item> items;
    private VideoAdapter adapter;
    private RetrofitApiService retrofitApiService;
    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        items = new ArrayList<>();
        adapter = new VideoAdapter(items, this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrofitApiService = RetrofitApiClient.getClient().create(RetrofitApiService.class);

        disposable.add(RxTextView.textChangeEvents(binding.searchEditText)
                .skipInitialValue()
                .debounce(300, TimeUnit.MILLISECONDS)
                .switchMap(new Function<TextViewTextChangeEvent, Observable<Videos>>() {
                    @Override
                    public Observable<Videos> apply(TextViewTextChangeEvent textViewTextChangeEvent) {
                        return retrofitApiService.getAllVideos(
                                "search",
                                textViewTextChangeEvent.text().toString(),
                                "snippet",
                                "video",
                                "5",
                                "any")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .subscribeWith(new DisposableObserver<Videos>() {
                    @Override
                    public void onNext(Videos video) {
                        Log.e(TAG, "onNext: video " + video);
                        Log.e(TAG, "onNext: video.getItems().size() " + video.getItems().size());
                        items.clear();
                        items.addAll(video.getItems());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: " + throwable);
                        Toast.makeText(MainActivity.this, "Error!!!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");
                    }
                }));
    }
}
