package com.example.utube.ui;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.utube.CustomLinearLayoutManager;
import com.example.utube.ItemClickSupport;
import com.example.utube.R;
import com.example.utube.UTubeApplication;
import com.example.utube.database.AppDatabase;
import com.example.utube.databinding.FragmentMainBinding;
import com.example.utube.di.DaggerMainFragmentComponent;
import com.example.utube.di.MainFragmentComponent;
import com.example.utube.di.MainFragmentModule;
import com.example.utube.model.Videos;
import com.example.utube.util;
import com.example.utube.viewmodel.ItemViewModel;
import com.example.utube.viewmodel.ItemViewModelFactory;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.example.utube.di.DatabaseModule.DATABASE_NAME;

public class MainFragment extends Fragment implements ItemViewModel.BoundaryCallbackListener {
    @Inject VideoAdapter adapter;
    private CompositeDisposable disposable = new CompositeDisposable();
    private FragmentMainBinding binding;
    @Inject Executor dataBaseExecutor;
    @Inject AppDatabase mDb;
    private SharedPreferences sharedPreferences;
    @Inject ItemViewModelFactory itemViewModelFactory;
    private ItemViewModel itemViewModel;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private String userName;
    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    private FirebaseStorage firebaseStorage;
    private StorageReference videoStorageReference;
    private Uri databaseUri;
    VideoItemClickListener videoItemClickListener;
    private CustomLinearLayoutManager customLinearLayoutManager;

    public interface VideoItemClickListener {
        void onVideoItemClicked(String videoId);
    }

    // Override onAttach to make sure that the container activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            videoItemClickListener = (VideoItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement VideoItemClickListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        final View rootView = binding.getRoot();

        FirebaseApp.initializeApp(Objects.requireNonNull(getContext()));
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build());
            if (user != null) {
                // User is signed in
                userName = user.getDisplayName();
            } else {
                // User is signed out
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
        };
        String databasePath = Objects.requireNonNull(getContext()).getDatabasePath(DATABASE_NAME).getAbsolutePath();
        databaseUri = Uri.fromFile(new File(databasePath));

        MainFragmentComponent mainFragmentComponent = DaggerMainFragmentComponent
                .builder()
                .mainFragmentModule(new MainFragmentModule(Objects.requireNonNull(getContext())))
                .applicationComponent(((UTubeApplication) Objects.requireNonNull(getActivity()).getApplicationContext()).getApplicationComponent())
                .build();
        mainFragmentComponent.inject(this);
        binding.recyclerView.setAdapter(adapter);
        customLinearLayoutManager = new CustomLinearLayoutManager(Objects.requireNonNull(getContext()));
        binding.recyclerView.setLayoutManager(customLinearLayoutManager);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getContext()));
        final String query = sharedPreferences.getString("query", "");
        binding.searchEditText.setText(query);

        util.checkNetworkConnection(Objects.requireNonNull(getContext()));
        addWinkToEmptyListTextView();
        itemViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), itemViewModelFactory).get(ItemViewModel.class);
        dataBaseExecutor.execute(() -> {
            if (mDb.videoDao().getAllVideos().size() != 0) {
                itemViewModel.setQuery(query);
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> getVideosFromDatabase(itemViewModel));
            }
        });

        binding.searchEditText.setOnClickListener(v -> prepareLoadingVideosFromDatabase());

        ItemClickSupport.addTo(binding.recyclerView)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    if (adapter.getCurrentList() != null) {
                        Videos.Item item = adapter.getCurrentList().get(position);
                        if (item != null) {
                            String videoId = item.getId().getVideoId();
                            videoItemClickListener.onVideoItemClicked(videoId);
                        }
                    }
                })
                .setOnItemLongClickListener(
                        (recyclerView, position, v) -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                            View.inflate(Objects.requireNonNull(getContext()), R.layout.dialog, null);
                            CharSequence[] dialogButtons = new CharSequence[]{
                                    getString(R.string.update),
                                    getString(R.string.delete),
                                    getString(android.R.string.cancel)};
                            builder.setItems(dialogButtons,
                                    (dialog, which) -> {
                                        switch (which) {
                                            case 0:
                                                Intent editorIntent = new Intent(Objects.requireNonNull(getContext()), EditorActivity.class);
                                                editorIntent.putExtra("idPrimaryKey", (int) v.getTag());
                                                startActivity(editorIntent);
                                                break;
                                            case 1:
                                                dataBaseExecutor.execute(() -> {
                                                    if (adapter.getCurrentList() != null) {
                                                        Videos.Item item = adapter.getCurrentList().get(position);
                                                        if (item != null) {
                                                            mDb.videoDao().deleteVideo(item);
                                                        }
                                                    }
                                                });
                                                break;
                                            case 2:
                                                break;
                                        }
                                    });
                            builder.create().show();
                            return true;
                        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                dataBaseExecutor.execute(() -> {
                    int position = viewHolder.getAdapterPosition();
                    if (adapter.getCurrentList() != null) {
                        Videos.Item item = adapter.getCurrentList().get(position);
                        if (item != null) {
                            mDb.videoDao().deleteVideo(item);
                        }
                    }
                });
            }
        }).attachToRecyclerView(binding.recyclerView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(firebaseAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(Objects.requireNonNull(getContext()), "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(Objects.requireNonNull(getContext()), "Sign in cancelled", Toast.LENGTH_SHORT).show();
                Objects.requireNonNull(getActivity()).finish();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent settingsIntent = new Intent(Objects.requireNonNull(getContext()), SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.upload_database:
                uploadDatabaseToFirebase();
                break;
            case R.id.download_database:
                Objects.requireNonNull(getContext()).deleteDatabase(DATABASE_NAME);
                downloadDatabaseFromFirebase();
                break;
            case R.id.clear_database:
                dataBaseExecutor.execute(() -> mDb.clearAllTables());
                break;
            case R.id.sign_out:
                AuthUI.getInstance().signOut(Objects.requireNonNull(getContext()));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemAtEndLoaded() {
        customLinearLayoutManager.setScrollEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingNewItemsCompleted() {
        customLinearLayoutManager.setScrollEnabled(true);
        binding.progressBar.setVisibility(View.GONE);
    }

    private void prepareLoadingVideosFromDatabase() {
        disposable.clear();
        disposable.add(RxTextView.textChangeEvents(binding.searchEditText)
                .skipInitialValue()
                .debounce(1000, TimeUnit.MILLISECONDS)
                .switchMap((Function<TextViewTextChangeEvent, ObservableSource<TextViewTextChangeEvent>>) textViewTextChangeEvent -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("query", textViewTextChangeEvent.text().toString());
                    editor.putString("nextPageToken", "");
                    editor.apply();
                    InputMethodManager imm = (InputMethodManager) Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(binding.searchEditText.getWindowToken(), 0);
                    return Observable.just(textViewTextChangeEvent);
                })
                .subscribeWith(new DisposableObserver<TextViewTextChangeEvent>() {
                    @Override
                    public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                        itemViewModel.setQuery(textViewTextChangeEvent.text().toString());
                        Objects.requireNonNull(getActivity()).runOnUiThread(() -> getVideosFromDatabase(itemViewModel));
                    }

                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onComplete() {
                    }
                }));
    }

    private void getVideosFromDatabase(ItemViewModel itemViewModel) {
        itemViewModel.setBoundaryCallbackListener(this);
        itemViewModel.getVideosLiveDataPagedList().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> adapter.submitList(items));
                if (items.size() == 0) {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> binding.emptyList.setVisibility(View.VISIBLE));
                } else {
                    Objects.requireNonNull(getActivity()).runOnUiThread(() -> binding.emptyList.setVisibility(View.GONE));
                }
            }
        });
    }

    private void addWinkToEmptyListTextView() {
        Html.ImageGetter imageGetter = source -> {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_wink);
            drawable.setBounds(0, -5, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        };
        Spanned spanned = Html.fromHtml(
                getString(R.string.empty_list) + " <img src='" + getResources().getDrawable(R.drawable.ic_wink) + "'/>",
                imageGetter,
                null
        );
        binding.emptyList.setText(spanned);
    }

    private void uploadDatabaseToFirebase() {
        videoStorageReference.putFile(databaseUri).continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return videoStorageReference.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(Objects.requireNonNull(getContext()), "Upload completed successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadDatabaseFromFirebase() {
        videoStorageReference.getFile(databaseUri).continueWithTask((Continuation<FileDownloadTask.TaskSnapshot, Task<Uri>>) task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            Intent intent = new Intent(Objects.requireNonNull(getContext()), MainActivity.class);
            startActivity(intent);
            System.exit(0);
            return null;
        });
    }
}
