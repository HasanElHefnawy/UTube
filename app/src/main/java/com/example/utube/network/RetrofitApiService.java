package com.example.utube.network;

import com.example.utube.model.Videos;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RetrofitApiService {

    @GET("youtube/v3/{path}")
    Observable<Videos> getAllVideos(
            @Path("path") String path,
            @Query("q") String query,
            @Query("part") String part,
            @Query("type") String type,
            @Query("maxResults") String maxResults,
            @Query("videoDuration") String videoDuration);
}
