package com.example.utube.di;

import com.example.utube.ui.MainFragment;

import dagger.Component;

@Component(modules = MainFragmentModule.class, dependencies = ApplicationComponent.class)
@SingletonActivityScope
public interface MainFragmentComponent {

    void inject(MainFragment mainFragment);
}
