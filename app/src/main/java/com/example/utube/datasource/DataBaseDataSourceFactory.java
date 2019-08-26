package com.example.utube.datasource;

import android.arch.paging.DataSource;
import android.content.Context;
import android.util.Log;

import com.example.utube.model.Videos;

public class DataBaseDataSourceFactory extends DataSource.Factory<Integer, Videos.Item> {
    private static final String TAG = "zzzzz " + DataBaseDataSourceFactory.class.getSimpleName();

    private DataBaseDataSource dataBaseDataSource;
    private Context context;

    public DataBaseDataSourceFactory(Context context) {
        Log.e(TAG, "DataBaseDataSourceFactory: ");
        this.context = context;
    }

    @Override
    public DataSource<Integer, Videos.Item> create() {
        Log.e(TAG, "create: ");
        if (dataBaseDataSource == null) {
            dataBaseDataSource = new DataBaseDataSource(context);
        }
        return dataBaseDataSource;
    }
}
