package com.example.utube.datasource;

import android.arch.paging.PageKeyedDataSource;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.utube.database.AppDatabase;
import com.example.utube.database.VideoDao;
import com.example.utube.model.Videos;

import java.util.List;

public class DataBaseDataSource extends PageKeyedDataSource<Integer, Videos.Item> {
    private static final String TAG = "zzzz DataBaseDataSource";
    private VideoDao videoDao;
    private int lastVideoIdPrimaryKey;

    DataBaseDataSource(Context context) {
        videoDao = AppDatabase.getInstance(context).videoDao();
    }

    @Override
    public void loadInitial(@NonNull PageKeyedDataSource.LoadInitialParams<Integer> params,
                            @NonNull LoadInitialCallback<Integer, Videos.Item> callback) {
        Log.e(TAG, "loadInitial: params.requestedLoadSize " + params.requestedLoadSize);
        List<Videos.Item> videoItems = videoDao.getAllVideos(0, params.requestedLoadSize);
        for (Videos.Item videoItem : videoItems) {
            Log.e(TAG, "loadInitial: " + videoItem.getIdPrimaryKey() + " " + videoItem.getSnippet().getTitle());
            lastVideoIdPrimaryKey = videoItem.getIdPrimaryKey();
        }
        callback.onResult(videoItems, null, lastVideoIdPrimaryKey + 1);
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params,
                           @NonNull LoadCallback<Integer, Videos.Item> callback) {
        Log.e(TAG, "loadBefore: params.key " + params.key + "\tparams.requestedLoadSize " + params.requestedLoadSize);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params,
                          @NonNull LoadCallback<Integer, Videos.Item> callback) {
        Log.e(TAG, "loadAfter: params.key " + params.key + "\tparams.requestedLoadSize " + params.requestedLoadSize);
        List<Videos.Item> videoItems = videoDao.getAllVideos(params.key, params.requestedLoadSize);
        for (Videos.Item videoItem : videoItems) {
            Log.e(TAG, "loadAfter: " + videoItem.getIdPrimaryKey() + " " + videoItem.getSnippet().getTitle());
            lastVideoIdPrimaryKey = videoItem.getIdPrimaryKey();
        }
        int nextKey = lastVideoIdPrimaryKey + 1;
        callback.onResult(videoItems, nextKey);
    }
}
