package com.example.utube.activity;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.example.utube.model.Videos;
import com.example.utube.util;
import com.example.utube.viewmodel.ItemViewModel;
import com.example.utube.viewmodel.ItemViewModelFactory;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

import static com.example.utube.database.AppDatabase.DATABASE_NAME;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzzz MainActivity";
    private VideoAdapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private ActivityMainBinding binding;
    private Executor dataBaseExecutor;
    private AppDatabase mDb;
    private SharedPreferences sharedPreferences;
    private ItemViewModel itemViewModel;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private String userName;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private FirebaseStorage firebaseStorage;
    private StorageReference videoStorageReference;
    private Uri databaseUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        dataBaseExecutor = AppExecutor.getInstance().dataBaseExecutor();
        mDb = AppDatabase.getInstance(this);

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(this);
        Log.e(TAG, "onCreate: firebaseApp " + firebaseApp);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build(),
                        new AuthUI.IdpConfig.GoogleBuilder().build());
                if (user != null) {
                    // User is signed in
                    Log.e("zzzzz", "There is a user " + user);
                    userName = user.getDisplayName();
                } else {
                    // User is signed out
                    Log.e("zzzzz", "There is no user null");
                    userName = ANONYMOUS;
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
                firebaseStorage = FirebaseStorage.getInstance();
                videoStorageReference = firebaseStorage.getReference().child(userName).child("videos.db");
            }
        };
        String databasePath = getDatabasePath(DATABASE_NAME).getAbsolutePath();
        Log.e(TAG, "onCreate: databasePath " + databasePath);
        databaseUri = Uri.fromFile(new File(databasePath));
        Log.e(TAG, "onCreate: databaseUri " + databaseUri);

        adapter = new VideoAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String query = sharedPreferences.getString("query", "");
        binding.searchEditText.setText(query);
        Log.e(TAG, "onCreate: query " + query);

        util.checkNetworkConnection(this);
        addWinkToEmptyListTextView();
        dataBaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onCreate: mDb.videoDao().getAllVideos().size() " + mDb.videoDao().getAllVideos().size());
                if (mDb.videoDao().getAllVideos().size() != 0) {
                    ItemViewModelFactory itemViewModelFactory = new ItemViewModelFactory(getApplication(), query);
                    itemViewModel = ViewModelProviders.of(MainActivity.this, itemViewModelFactory).get(ItemViewModel.class);
                    getVideosFromDatabase(itemViewModel);
                }
            }
        });

        binding.searchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareLoadingVideosFromDatabase();
            }
        });

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
                                                            int sizeBefore = mDb.videoDao().getAllVideos().size();
                                                            Log.e(TAG, "case 1 sizeBefore " + sizeBefore);
                                                            final Videos.Item item = adapter.getCurrentList().get(position);
                                                            if (item != null) {
                                                                Log.e(TAG, "case 1 item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                                                                mDb.videoDao().deleteVideo(item);
                                                            }
                                                            int sizeAfter = mDb.videoDao().getAllVideos().size();
                                                            Log.e(TAG, "case 1 sizeAfter " + sizeAfter);
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
                            int sizeBefore = mDb.videoDao().getAllVideos().size();
                            Log.e(TAG, "onSwiped sizeBefore " + sizeBefore);
                            final Videos.Item item = adapter.getCurrentList().get(position);
                            if (item != null) {
                                Log.e(TAG, "onSwiped item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                                mDb.videoDao().deleteVideo(item);
                            }
                            int sizeAfter = mDb.videoDao().getAllVideos().size();
                            Log.e(TAG, "onSwiped sizeAfter " + sizeAfter);
                        }
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void prepareLoadingVideosFromDatabase() {
        Log.e(TAG, "prepareLoadingVideosFromDatabase: ");
        disposable.clear();
        disposable.add(RxTextView.textChangeEvents(binding.searchEditText)
                .skipInitialValue()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .switchMap(new Function<TextViewTextChangeEvent, ObservableSource<TextViewTextChangeEvent>>() {
                    @Override
                    public Observable<TextViewTextChangeEvent> apply(TextViewTextChangeEvent textViewTextChangeEvent) {
                        Log.e(TAG, "apply: textViewTextChangeEvent " + textViewTextChangeEvent.text().toString());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("query", textViewTextChangeEvent.text().toString());
                        editor.putString("nextPageToken", "");
                        editor.apply();
                        return Observable.just(textViewTextChangeEvent);
                    }
                })
                .subscribeWith(new DisposableObserver<TextViewTextChangeEvent>() {
                    @Override
                    public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                        Log.e(TAG, "onNext: textViewTextChangeEvent " + textViewTextChangeEvent.text().toString());
                        itemViewModel = new ItemViewModel(getApplication(), textViewTextChangeEvent.text().toString());
                        Log.e(TAG, "prepareLoadingVideosFromDatabase: itemViewModel " + itemViewModel);
                        Log.e(TAG, "prepareLoadingVideosFromDatabase Before adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                        Log.e(TAG, "prepareLoadingVideosFromDatabase Before adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
                        getVideosFromDatabase(itemViewModel);
                        Log.e(TAG, "prepareLoadingVideosFromDatabase After adapter.submitList: adapter.getItemCount() " + adapter.getItemCount());
                        Log.e(TAG, "prepareLoadingVideosFromDatabase After adapter.submitList: adapter.getCurrentList() " + adapter.getCurrentList());
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

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(MainActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void getVideosFromDatabase(ItemViewModel itemViewModel) {
        dataBaseExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Videos.Item> items = mDb.videoDao().getAllVideos();
                Log.e(TAG, "getVideosFromDatabase: items.size() " + items.size());
                for (Videos.Item item : items) {
                    Log.e(TAG, "getVideosFromDatabase: item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                }
            }
        });
        itemViewModel.getDatabasePagedList().observe(MainActivity.this, new Observer<PagedList<Videos.Item>>() {
            @Override
            public void onChanged(@Nullable PagedList<Videos.Item> items) {
                Log.e(TAG, "getVideosFromDatabase onChanged: getDatabasePagedList ");
                if (items != null) {
                    Log.e(TAG, "Updating list of video items from LiveData in ViewModel");
                    Log.e(TAG, "getVideosFromDatabase onChanged: items.size() " + items.size());
                    int nullItem = 0;
                    for (Videos.Item item : items) {
                        if (item != null) {
                            Log.e(TAG, "getVideosFromDatabase onChanged: item.getIdPrimaryKey() " + item.getIdPrimaryKey() + " " + item.getSnippet().getTitle());
                        } else {
                            nullItem++;
                            Log.e(TAG, "getVideosFromDatabase onChanged: null item " + nullItem);
                        }
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

    private void uploadDatabaseToFirebase() {
        videoStorageReference.putFile(databaseUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                Log.e(TAG, "uploadDatabaseToFirebase then: task.isSuccessful() " + task.isSuccessful());
                if (!task.isSuccessful() && task.getException() != null) {
                    Log.e(TAG, "uploadDatabaseToFirebase then: task.getException() " + task.getException());
                    throw task.getException();
                }
                return videoStorageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Log.e(TAG, "uploadDatabaseToFirebase onComplete: task.isSuccessful() " + task.isSuccessful());
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.e(TAG, "uploadDatabaseToFirebase onComplete: downloadUri " + downloadUri);
                    Toast.makeText(getApplicationContext(), "Upload completed successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void downloadDatabaseFromFirebase() {
        videoStorageReference.getFile(databaseUri).continueWithTask(new Continuation<FileDownloadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<FileDownloadTask.TaskSnapshot> task) throws Exception {
                Log.e(TAG, "downloadDatabaseFromFirebase then: task.isSuccessful() " + task.isSuccessful());
                if (!task.isSuccessful() && task.getException() != null) {
                    Log.e(TAG, "downloadDatabaseFromFirebase then: task.getException() " + task.getException());
                    throw task.getException();
                }
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                System.exit(0);
                return null;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.upload_database:
                uploadDatabaseToFirebase();
                break;
            case R.id.download_database:
                deleteDatabase(DATABASE_NAME);
                downloadDatabaseFromFirebase();
                break;
            case R.id.clear_database:
                dataBaseExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        int sizeBefore = mDb.videoDao().getAllVideos().size();
                        Log.e(TAG, "mDb.clearAllTables(): sizeBefore " + sizeBefore);
                        mDb.clearAllTables();
                        int sizeAfter = mDb.videoDao().getAllVideos().size();
                        Log.e(TAG, "mDb.clearAllTables(): sizeAfter " + sizeAfter);
                    }
                });
                break;
            case R.id.sign_out:
                AuthUI.getInstance().signOut(this);
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
