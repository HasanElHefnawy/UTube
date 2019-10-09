package com.example.utube.di;

import com.example.utube.UTubeApplication;
import com.example.utube.database.AppDatabase;
import com.example.utube.viewmodel.ItemViewModel;
import com.example.utube.viewmodel.ItemViewModelFactory;

import java.util.concurrent.Executor;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

@Component(modules = {
        NetworkModule.class,            // Provides RetrofitApiService in ItemViewModel
        DatabaseModule.class,           // Provides AppDatabase & Executor in ItemViewModel & EditorActivity
        ApplicationModule.class,        // Provides Application for ItemViewModelFactory
        ActivityModule.class,           // Provides AndroidInjector
        AndroidSupportInjectionModule.class})
@SingletonApplicationScope
public interface ApplicationComponent {

    AppDatabase getAppDatabase();                   // inject AppDatabase through MainFragmentComponent in MainFragment

    Executor getExecutor();                         // inject Executor through MainFragmentComponent in MainFragment

    ItemViewModelFactory getItemViewModelFactory(); // inject ItemViewModelFactory through MainFragmentComponent in MainFragment

    void inject(ItemViewModel itemViewModel);       // inject RetrofitApiService & AppDatabase & Executor in ItemViewModel

    void inject(UTubeApplication uTubeApplication); // inject AppDatabase & Executor in EditorActivity
}
