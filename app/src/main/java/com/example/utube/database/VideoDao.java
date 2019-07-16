package com.example.utube.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM video ORDER BY id")
    LiveData<List<VideoEntry>> getAllVideos();

    @Insert
    void insertVideo(VideoEntry videoEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateVideo(VideoEntry videoEntry);

    @Delete
    void deleteVideo(VideoEntry videoEntry);

    @Query("SELECT * FROM video WHERE id = :id")
    LiveData<VideoEntry> getVideoById(int id);
}
