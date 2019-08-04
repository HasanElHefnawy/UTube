package com.example.utube.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.utube.model.Videos;

import java.util.List;

@Dao
public interface VideoDao {

    @Query("SELECT * FROM video ORDER BY idPrimaryKey")
    LiveData<List<Videos.Item>> getAllVideos();

    @Insert
    void insertVideo(Videos.Item videoItem);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateVideo(Videos.Item videoItem);

    @Delete
    void deleteVideo(Videos.Item videoItem);

    @Query("SELECT * FROM video WHERE idPrimaryKey = :id")
    LiveData<Videos.Item> getVideoById(int id);
}
