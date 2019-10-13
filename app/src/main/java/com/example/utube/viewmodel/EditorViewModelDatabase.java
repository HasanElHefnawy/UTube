package com.example.utube.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.utube.database.AppDatabase;
import com.example.utube.model.Videos;

public class EditorViewModelDatabase extends ViewModel {

    private LiveData<Videos.Item> videoItemLiveData;

    public EditorViewModelDatabase(AppDatabase mDb, int id) {
        videoItemLiveData = mDb.videoDao().getVideoById(id);
    }

    public LiveData<Videos.Item> getVideoItemLiveData() {
        return videoItemLiveData;
    }
}
