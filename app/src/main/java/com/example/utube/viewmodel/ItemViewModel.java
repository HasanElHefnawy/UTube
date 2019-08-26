package com.example.utube.viewmodel;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.utube.R;
import com.example.utube.datasource.DataBaseDataSourceFactory;
import com.example.utube.datasource.NetworkDataSourceFactory;
import com.example.utube.model.Videos;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ItemViewModel extends ViewModel {
    private static final String TAG = "zzzz ItemViewModel";
    private LiveData<PagedList<Videos.Item>> networkPagedList;
    private LiveData<PagedList<Videos.Item>> databasePagedList;

    public ItemViewModel(Application application, String query) {
        Log.e(TAG, "ItemViewModel: ");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        String sizeString = sharedPreferences.getString(application.getString(R.string.setting_max_results_key), "5");
        int size = 5;
        if (sizeString != null)
            size = Integer.valueOf(sizeString);
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(size)
                .setPageSize(size)
                .build();
        NetworkDataSourceFactory networkDataSourceFactory = new NetworkDataSourceFactory(application, query);
        DataBaseDataSourceFactory databaseDataSourceFactory = new DataBaseDataSourceFactory(application);
        Executor executor = Executors.newFixedThreadPool(3);
        networkPagedList = new LivePagedListBuilder<>(networkDataSourceFactory, config)
                .setFetchExecutor(executor)
                .build();
        databasePagedList = new LivePagedListBuilder<>(databaseDataSourceFactory, config)
                .setFetchExecutor(executor)
                .build();
    }

    public LiveData<PagedList<Videos.Item>> getNetworkPagedList() {
        Log.e(TAG, "getNetworkPagedList: networkPagedList " + networkPagedList);
        Log.e(TAG, "getNetworkPagedList: networkPagedList.getValue() " + networkPagedList.getValue());
        return networkPagedList;
    }

    public LiveData<PagedList<Videos.Item>> getDatabasePagedList() {
        Log.e(TAG, "getDatabasePagedList: databasePagedList " + databasePagedList);
        Log.e(TAG, "getDatabasePagedList: databasePagedList.getValue() " + databasePagedList.getValue());
        return databasePagedList;
    }
}
