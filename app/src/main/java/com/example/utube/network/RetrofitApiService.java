package com.example.utube.network;

import com.example.utube.model.Videos;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitApiService {

    @GET("youtube/v3/")
    Observable<Videos> getAllVideos(@Query("q") String query);
}
