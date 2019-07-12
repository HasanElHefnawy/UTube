package com.example.utube.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.utube.database.AppDatabase;

public class EditorViewModelDatabaseFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int id;

    public EditorViewModelDatabaseFactory(AppDatabase mDb, int id) {
        this.mDb = mDb;
        this.id = id;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new EditorViewModelDatabase(mDb, id);
    }
}
