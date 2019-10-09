package com.example.utube.di;

import com.example.utube.ui.EditorActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector()
    abstract EditorActivity contributeEditorActivity();
}