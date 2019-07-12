package com.example.utube.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoEntry;

public class EditorViewModelDatabase extends ViewModel {

    private static final String TAG = "zzzzz " + EditorViewModelDatabase.class.getSimpleName();
    private LiveData<VideoEntry> videoEntry;

    public EditorViewModelDatabase(AppDatabase mDb, int id) {
        videoEntry = mDb.videoDao().getVideoById(id);
        Log.e(TAG, "Actively retrieving video entry from DataBase");
    }

    public LiveData<VideoEntry> getVideoEntry() {
        return videoEntry;
    }
}
