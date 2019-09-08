package com.example.utube.extractor;

public class YoutubeMedia {

    private String Url;
    private String dechiperedSig;

    YoutubeMedia() {
    }

    void setDechiperedSig(String dechiperedSig) {
        this.dechiperedSig = dechiperedSig;
    }

    void setUrl(String url) {
        Url = url;
    }

    public String getUrl() {
        if (dechiperedSig != null) {
            if (Url.contains("&lsig=")) {
                return Url + "&sig=" + dechiperedSig;
            } else {
                return Url + "&signature=" + dechiperedSig;
            }
        } else {
            return Url;
        }
    }
}
