package com.example.utube.datasource;

import android.arch.paging.DataSource;
import android.content.Context;
import android.util.Log;

import com.example.utube.model.Videos;

public class NetworkDataSourceFactory extends DataSource.Factory<String, Videos.Item> {
    private static final String TAG = "zzzzz " + NetworkDataSourceFactory.class.getSimpleName();

    private NetworkDataSource networkDataSource;
    private Context context;
    private String query;

    public NetworkDataSourceFactory(Context context, String query) {
        Log.e(TAG, "NetworkDataSourceFactory: ");
        this.context = context;
        this.query = query;
    }

    @Override
    public DataSource<String, Videos.Item> create() {
        Log.e(TAG, "create: ");
        if (networkDataSource == null) {
            networkDataSource = new NetworkDataSource(context, query);
        }
        return networkDataSource;
    }
}
