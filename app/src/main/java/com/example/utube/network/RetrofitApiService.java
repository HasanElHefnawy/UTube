package com.example.utube.network;

import com.example.utube.model.Video;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitApiService {

    @GET("youtube/v3/")
    Observable<Video> getAllVideos(@Query("q") String query);
}
