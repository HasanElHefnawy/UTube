package com.example.utube.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.squareup.moshi.Json;

import java.util.List;

public class Videos {

    @Json(name = "kind")
    private String kind;
    @Json(name = "etag")
    private String etag;
    @Json(name = "nextPageToken")
    private String nextPageToken;
    @Json(name = "regionCode")
    private String regionCode;
    @Json(name = "pageInfo")
    private PageInfo pageInfo;
    @Json(name = "items")
    private List<Item> items = null;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class PageInfo {

        @Json(name = "totalResults")
        private Integer totalResults;
        @Json(name = "resultsPerPage")
        private Integer resultsPerPage;

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }

        public Integer getResultsPerPage() {
            return resultsPerPage;
        }

        public void setResultsPerPage(Integer resultsPerPage) {
            this.resultsPerPage = resultsPerPage;
        }

    }

    @Entity(tableName = "video", indices = {@Index(value = {"videoId"}, unique = true)})
    public static class Item {

        @PrimaryKey(autoGenerate = true)
        private int idPrimaryKey;
        @Json(name = "kind")
        private String kind;
        @Json(name = "etag")
        private String etag;
        @Embedded
        @Json(name = "id")
        private Id id;
        @Embedded
        @Json(name = "snippet")
        private Snippet snippet;
        private String duration;

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public int getIdPrimaryKey() {
            return idPrimaryKey;
        }

        public void setIdPrimaryKey(int idPrimaryKey) {
            this.idPrimaryKey = idPrimaryKey;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public Id getId() {
            return id;
        }

        public void setId(Id id) {
            this.id = id;
        }

        public Snippet getSnippet() {
            return snippet;
        }

        public void setSnippet(Snippet snippet) {
            this.snippet = snippet;
        }

        public boolean equals(Item item) {
            return (this == item);
        }

        public static class Id {

            @Ignore
            @Json(name = "kind")
            private String kind;
            @Json(name = "videoId")
            private String videoId;

            public String getKind() {
                return kind;
            }

            public void setKind(String kind) {
                this.kind = kind;
            }

            public String getVideoId() {
                return videoId;
            }

            public void setVideoId(String videoId) {
                this.videoId = videoId;
            }

        }

        public static class Snippet {

            @Json(name = "publishedAt")
            private String publishedAt;
            @Json(name = "channelId")
            private String channelId;
            @Json(name = "title")
            private String title;
            @Json(name = "description")
            private String description;
            @Json(name = "thumbnails")
            private Thumbnails thumbnails;
            @Json(name = "channelTitle")
            private String channelTitle;
            @Json(name = "liveBroadcastContent")
            private String liveBroadcastContent;

            public String getPublishedAt() {
                return publishedAt;
            }

            public void setPublishedAt(String publishedAt) {
                this.publishedAt = publishedAt;
            }

            public String getChannelId() {
                return channelId;
            }

            public void setChannelId(String channelId) {
                this.channelId = channelId;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public Thumbnails getThumbnails() {
                return thumbnails;
            }

            public void setThumbnails(Thumbnails thumbnails) {
                this.thumbnails = thumbnails;
            }

            public String getChannelTitle() {
                return channelTitle;
            }

            public void setChannelTitle(String channelTitle) {
                this.channelTitle = channelTitle;
            }

            public String getLiveBroadcastContent() {
                return liveBroadcastContent;
            }

            public void setLiveBroadcastContent(String liveBroadcastContent) {
                this.liveBroadcastContent = liveBroadcastContent;
            }

            public static class Thumbnails {

                @Json(name = "default")
                private Default _default;
                @Json(name = "medium")
                private Medium medium;
                @Json(name = "high")
                private High high;

                public Default getDefault() {
                    return _default;
                }

                public void setDefault(Default _default) {
                    this._default = _default;
                }

                public Medium getMedium() {
                    return medium;
                }

                public void setMedium(Medium medium) {
                    this.medium = medium;
                }

                public High getHigh() {
                    return high;
                }

                public void setHigh(High high) {
                    this.high = high;
                }

                public static class Default {

                    @Json(name = "url")
                    private String url;
                    @Json(name = "width")
                    private Integer width;
                    @Json(name = "height")
                    private Integer height;

                    public String getUrl() {
                        return url;
                    }

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public Integer getWidth() {
                        return width;
                    }

                    public void setWidth(Integer width) {
                        this.width = width;
                    }

                    public Integer getHeight() {
                        return height;
                    }

                    public void setHeight(Integer height) {
                        this.height = height;
                    }

                }

                public static class Medium {

                    @Json(name = "url")
                    private String url;
                    @Json(name = "width")
                    private Integer width;
                    @Json(name = "height")
                    private Integer height;

                    public String getUrl() {
                        return url;
                    }

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public Integer getWidth() {
                        return width;
                    }

                    public void setWidth(Integer width) {
                        this.width = width;
                    }

                    public Integer getHeight() {
                        return height;
                    }

                    public void setHeight(Integer height) {
                        this.height = height;
                    }

                }

                public static class High {

                    @Json(name = "url")
                    private String url;
                    @Json(name = "width")
                    private Integer width;
                    @Json(name = "height")
                    private Integer height;

                    public String getUrl() {
                        return url;
                    }

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public Integer getWidth() {
                        return width;
                    }

                    public void setWidth(Integer width) {
                        this.width = width;
                    }

                    public Integer getHeight() {
                        return height;
                    }

                    public void setHeight(Integer height) {
                        this.height = height;
                    }

                }

            }
        }
    }
}