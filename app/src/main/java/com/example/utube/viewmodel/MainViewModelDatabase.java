package com.example.utube.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.utube.database.AppDatabase;
import com.example.utube.model.Videos;

import java.util.List;

public class MainViewModelDatabase extends AndroidViewModel {

    private static final String TAG = "zzzzz " + MainViewModelDatabase.class.getSimpleName();
    private LiveData<List<Videos.Item>> videoItemsListLiveData;

    public MainViewModelDatabase(Application application) {
        super(application);
        AppDatabase mDb = AppDatabase.getInstance(this.getApplication());
        videoItemsListLiveData = mDb.videoDao().getAllVideos();
        Log.e(TAG, "Actively retrieving video items from DataBase");
    }

    public LiveData<List<Videos.Item>> getVideoItemsListLiveData() {
        return videoItemsListLiveData;
    }
}
