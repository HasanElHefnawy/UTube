package com.example.utube.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM video ORDER BY id")
    List<VideoEntry> getAllVideos();

    @Insert
    void insertVideo(VideoEntry videoEntry);
}
