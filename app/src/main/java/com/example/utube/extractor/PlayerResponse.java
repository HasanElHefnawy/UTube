package com.example.utube.extractor;

class PlayerResponse {

    private StreamingData streamingData;
    private YoutubeMeta videoDetails;

    StreamingData getStreamingData() {
        return streamingData;
    }

    YoutubeMeta getVideoDetails() {
        return videoDetails;
    }

    class StreamingData {

        private String hlsManifestUrl;

        String getHlsManifestUrl() {
            return hlsManifestUrl;
        }
    }
}


