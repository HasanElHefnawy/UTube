package com.example.utube;

import android.app.Application;

import com.example.utube.di.ApplicationComponent;
import com.example.utube.di.ApplicationModule;
import com.example.utube.di.ContextModule;
import com.example.utube.di.DaggerApplicationComponent;
import com.example.utube.di.DatabaseModule;
import com.example.utube.di.NetworkModule;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

public class UTubeApplication extends Application implements HasAndroidInjector {
    private ApplicationComponent applicationComponent;
    @Inject DispatchingAndroidInjector<Object> dispatchingAndroidInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationComponent = DaggerApplicationComponent
                .builder()
                .networkModule(new NetworkModule())             // inject RetrofitApiService in ItemViewModel
                .databaseModule(new DatabaseModule())           // inject AppDatabase & Executor in ItemViewModel & EditorActivity
                .contextModule(new ContextModule(this))         // inject Context for AppDatabase through MainFragmentComponent in MainFragment
                .applicationModule(new ApplicationModule(this)) // inject Application for ItemViewModelFactory through MainFragmentComponent in MainFragment
                .build();
        applicationComponent.inject(this);
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return dispatchingAndroidInjector;
    }
}
