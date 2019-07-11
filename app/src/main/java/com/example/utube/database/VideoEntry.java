package com.example.utube.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import org.joda.time.DateTime;

@Entity(tableName = "video")
public class VideoEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "video_id")
    private String videoId;
    private String title;
    @ColumnInfo(name = "thumbnails_url")
    private String thumbnailsUrl;
    @ColumnInfo(name = "published_at")
    private DateTime publishedAt;
    private String duration;

    @Ignore
    public VideoEntry(String videoId, String title, String thumbnailsUrl, DateTime publishedAt, String duration) {
        this.videoId = videoId;
        this.title = title;
        this.thumbnailsUrl = thumbnailsUrl;
        this.publishedAt = publishedAt;
        this.duration = duration;
    }

    public VideoEntry(int id, String videoId, String title, String thumbnailsUrl, DateTime publishedAt, String duration) {
        this.id = id;
        this.videoId = videoId;
        this.title = title;
        this.thumbnailsUrl = thumbnailsUrl;
        this.publishedAt = publishedAt;
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailsUrl() {
        return thumbnailsUrl;
    }

    public void setThumbnailsUrl(String thumbnailsUrl) {
        this.thumbnailsUrl = thumbnailsUrl;
    }

    public DateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(DateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
