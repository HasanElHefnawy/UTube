package com.example.utube.model;

import com.squareup.moshi.Json;

import java.util.List;

public class Video {

    @Json(name = "kind")
    private String kind;
    @Json(name = "etag")
    private String etag;
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

    public static class Item {

        @Json(name = "kind")
        private String kind;
        @Json(name = "etag")
        private String etag;
        @Json(name = "id")
        private String id;
        @Json(name = "contentDetails")
        private ContentDetails contentDetails;

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ContentDetails getContentDetails() {
            return contentDetails;
        }

        public void setContentDetails(ContentDetails contentDetails) {
            this.contentDetails = contentDetails;
        }

        public static class ContentDetails {

            @Json(name = "duration")
            private String duration;
            @Json(name = "dimension")
            private String dimension;
            @Json(name = "definition")
            private String definition;
            @Json(name = "caption")
            private String caption;
            @Json(name = "licensedContent")
            private Boolean licensedContent;
            @Json(name = "projection")
            private String projection;

            public String getDuration() {
                return duration;
            }

            public void setDuration(String duration) {
                this.duration = duration;
            }

            public String getDimension() {
                return dimension;
            }

            public void setDimension(String dimension) {
                this.dimension = dimension;
            }

            public String getDefinition() {
                return definition;
            }

            public void setDefinition(String definition) {
                this.definition = definition;
            }

            public String getCaption() {
                return caption;
            }

            public void setCaption(String caption) {
                this.caption = caption;
            }

            public Boolean getLicensedContent() {
                return licensedContent;
            }

            public void setLicensedContent(Boolean licensedContent) {
                this.licensedContent = licensedContent;
            }

            public String getProjection() {
                return projection;
            }

            public void setProjection(String projection) {
                this.projection = projection;
            }

        }
    }
}
