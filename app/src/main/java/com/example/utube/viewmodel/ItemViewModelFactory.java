package com.example.utube.viewmodel;

import android.app.Application;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

public class ItemViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application application;
    private String query;

    public ItemViewModelFactory(Application application, String query) {
        this.application = application;
        this.query = query;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new ItemViewModel(application, query);
    }
}
