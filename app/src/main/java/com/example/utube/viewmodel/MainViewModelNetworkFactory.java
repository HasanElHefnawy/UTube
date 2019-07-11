package com.example.utube.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.widget.EditText;

public class MainViewModelNetworkFactory extends ViewModelProvider.NewInstanceFactory {

    private final EditText editText;

    public MainViewModelNetworkFactory(EditText editText) {
        this.editText = editText;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new MainViewModelNetwork(editText);
    }
}
