package com.example.utube.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.utube.database.AppDatabase;
import com.example.utube.model.Videos;

public class EditorViewModelDatabase extends ViewModel {

    private static final String TAG = "zzzzz " + EditorViewModelDatabase.class.getSimpleName();
    private LiveData<Videos.Item> videoItemLiveData;

    public EditorViewModelDatabase(AppDatabase mDb, int id) {
        videoItemLiveData = mDb.videoDao().getVideoById(id);
        Log.e(TAG, "Actively retrieving video item from DataBase");
    }

    public LiveData<Videos.Item> getVideoItemLiveData() {
        return videoItemLiveData;
    }
}
