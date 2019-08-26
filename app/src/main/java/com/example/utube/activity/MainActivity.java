package com.example.utube.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.text.TextUtils;
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
import com.example.utube.model.Videos;
import com.example.utube.viewmodel.ItemViewModel;
import com.example.utube.viewmodel.ItemViewModelFactory;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz MainActivity";
    private VideoAdapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ActivityMainBinding binding;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;
    private ItemViewModel itemViewModelForNetwork;
    private ItemViewModel itemViewModelForDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(this);

        adapter = new VideoAdapter(this);
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
                                                        Log.e(TAG, "case 1 position " + position);
                                                        if (adapter.getCurrentList() != null) {
                                                            int sizeBefore = mDb.videoDao().getAllVideos(0, 1000).size();
                                                            Log.e(TAG, "case 1 sizeBefore " + sizeBefore);
                                                            final Videos.Item item = adapter.getCurrentList().get(position);
                                                            if (item != null) {
                                                                Log.e(TAG, "case 1 item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                                                                // mDb.videoDao().deleteVideo(item);
                                                                mDb.videoDao().deleteVideo2(item.getId().getVideoId());
                                                            }
                                                            int sizeAfter = mDb.videoDao().getAllVideos(0, 1000).size();
                                                            Log.e(TAG, "case 1 sizeAfter " + sizeAfter);
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (isConnected() && !TextUtils.isEmpty(binding.searchEditText.getText().toString())) {
                                                                        adapter.getCurrentList().remove(item);
                                                                        adapter.notifyItemRemoved(position);
                                                                    } else {
                                                                        loadVideosFromDatabase();
                                                                    }
                                                                }
                                                            });
                                                        }
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
                        final int position = viewHolder.getAdapterPosition();
                        Log.e(TAG, "onSwiped position " + position);
                        if (adapter.getCurrentList() != null) {
                            int sizeBefore = mDb.videoDao().getAllVideos(0, 1000).size();
                            Log.e(TAG, "onSwiped sizeBefore " + sizeBefore);
                            final Videos.Item item = adapter.getCurrentList().get(position);
                            if (item != null) {
                                Log.e(TAG, "onSwiped item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                                // mDb.videoDao().deleteVideo(item);
                                mDb.videoDao().deleteVideo2(item.getId().getVideoId());
                            }
                            int sizeAfter = mDb.videoDao().getAllVideos(0, 1000).size();
                            Log.e(TAG, "onSwiped sizeAfter " + sizeAfter);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isConnected() && !TextUtils.isEmpty(binding.searchEditText.getText().toString())) {
                                        adapter.getCurrentList().remove(item);
                                        adapter.notifyItemRemoved(position);
                                    } else {
                                        loadVideosFromDatabase();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerView);

        binding.searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    loadVideosOverInternetWhenTextChange();
                } else {
                    Toast.makeText(MainActivity.this, "There is no network connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        Log.e(TAG, "isConnected: " + (networkInfo != null && networkInfo.isConnected()));
        return networkInfo != null && networkInfo.isConnected();
    }

    private void loadVideosOverInternetWhenTextChange() {
        Log.e(TAG, "loadVideosOverInternetWhenTextChange: ");
        disposable.clear();
        disposable.add(RxTextView.textChangeEvents(binding.searchEditText)
                .skipInitialValue()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .switchMap(new Function<TextViewTextChangeEvent, ObservableSource<TextViewTextChangeEvent>>() {
                    @Override
                    public Observable<TextViewTextChangeEvent> apply(TextViewTextChangeEvent textViewTextChangeEvent) {
                        Log.e(TAG, "apply: textViewTextChangeEvent " + textViewTextChangeEvent.text().toString());
                        return Observable.just(textViewTextChangeEvent);
                    }
                })
                .subscribeWith(new DisposableObserver<TextViewTextChangeEvent>() {
                    @Override
                    public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                        Log.e(TAG, "onNext: textViewTextChangeEvent " + textViewTextChangeEvent.text().toString());
                        itemViewModelForNetwork = new ItemViewModel(getApplication(), textViewTextChangeEvent.text().toString());
                        Log.e(TAG, "onNext: itemViewModelForNetwork " + itemViewModelForNetwork);
                        getVideosFromInternet();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(TAG, "onError: " + throwable);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "onComplete: ");
                    }
                }));
    }

    private void getVideosFromInternet() {
        Log.e(TAG, "getVideosFromInternet: binding.searchEditText.getText().toString() " + binding.searchEditText.getText().toString());
        itemViewModelForNetwork.getNetworkPagedList().observe(MainActivity.this, new Observer<PagedList<Videos.Item>>() {
            @Override
            public void onChanged(@Nullable PagedList<Videos.Item> items) {
                Log.e(TAG, "getVideosFromInternet onChanged: getNetworkPagedList ");
                if (items != null) {
                    Log.e(TAG, "getVideosFromInternet onChanged: items.size() " + items.size());
                    for (Videos.Item item : items)
                        Log.e(TAG, "getVideosFromInternet onChanged: item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                    Log.e(TAG, "getVideosFromInternet onChanged: Before adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                    Log.e(TAG, "getVideosFromInternet onChanged: Before adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
                    adapter.submitList(items);
                    Log.e(TAG, "getVideosFromInternet onChanged: After adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                    Log.e(TAG, "getVideosFromInternet onChanged: After adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
                    if (items.size() == 0) {
                        binding.emptyList.setVisibility(View.VISIBLE);
                        Log.e(TAG, "getVideosFromInternet onChanged: binding.emptyList.setVisibility(View.VISIBLE)");
                    } else {
                        binding.emptyList.setVisibility(View.GONE);
                        Log.e(TAG, "getVideosFromInternet onChanged: binding.emptyList.setVisibility(View.GONE)");
                    }
                }
            }
        });
    }

    private void loadVideosFromDatabase() {
        ItemViewModelFactory itemViewModelFactoryForDatabase = new ItemViewModelFactory(getApplication(), "zzzzz");
        Log.e(TAG, "loadVideosFromDatabase: itemViewModelFactoryForDatabase " + itemViewModelFactoryForDatabase);
        itemViewModelForDatabase = ViewModelProviders.of(MainActivity.this, itemViewModelFactoryForDatabase).get(ItemViewModel.class);
        Log.e(TAG, "loadVideosFromDatabase: itemViewModelForDatabase " + itemViewModelForDatabase);
        Log.e(TAG, "loadVideosFromDatabase Before adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
        Log.e(TAG, "loadVideosFromDatabase Before adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
        getVideosFromDatabase();
        Log.e(TAG, "loadVideosFromDatabase After adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
        Log.e(TAG, "loadVideosFromDatabase After adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
    }

    private void getVideosFromDatabase() {
        dataBaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Videos.Item> items = mDb.videoDao().getAllVideos(0, 1000);
                Log.e(TAG, "getVideosFromDatabase: items.size() " + items.size());
                for (Videos.Item item : items) {
                    Log.e(TAG, "getVideosFromDatabase: item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                }
            }
        });
        itemViewModelForDatabase.getDatabasePagedList().observe(MainActivity.this, new Observer<PagedList<Videos.Item>>() {
            @Override
            public void onChanged(@Nullable PagedList<Videos.Item> items) {
                Log.e(TAG, "getVideosFromDatabase onChanged: getDatabasePagedList ");
                if (items != null) {
                    Log.e(TAG, "Updating list of video items from LiveData in ViewModel");
                    Log.e(TAG, "getVideosFromDatabase onChanged: items.size() " + items.size());
                    for (Videos.Item item : items) {
                        Log.e(TAG, "getVideosFromDatabase onChanged: item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                    }
                    Log.e(TAG, "getVideosFromDatabase onChanged: Before adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                    Log.e(TAG, "getVideosFromDatabase onChanged: Before adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
                    adapter.submitList(items);
                    Log.e(TAG, "getVideosFromDatabase onChanged: After adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                    Log.e(TAG, "getVideosFromDatabase onChanged: After adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
                    if (items.size() == 0) {
                        binding.emptyList.setVisibility(View.VISIBLE);
                        Log.e(TAG, "getVideosFromDatabase onChanged: binding.emptyList.setVisibility(View.VISIBLE)");
                    } else {
                        binding.emptyList.setVisibility(View.GONE);
                        Log.e(TAG, "getVideosFromDatabase onChanged: binding.emptyList.setVisibility(View.GONE)");
                    }
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.e(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);
    }
}
