package com.example.utube.extractor;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeStreamExtractor extends AsyncTask<String, Void, Void> {
    private Map<String, String> Headers = new HashMap<>();
    private List<YoutubeMedia> adaptiveMedia = new ArrayList<>();
    private List<YoutubeMedia> muxedMedia = new ArrayList<>();
    private String regexUrl = ("(?<=url=).*");
    private String regexType = "(?<=type=).*";
    private ExtractorListner listener;
    private ExtractorException Ex;
    private List<String> reasonUnavialable = Arrays.asList("This video is unavailable on this device.", "Content Warning", "who has blocked it on copyright grounds.");
    private YoutubeMeta YTVideoMeta;
    private Response response;
    private boolean isLive;
    private boolean useCipher;

    public YoutubeStreamExtractor(ExtractorListner EL) {
        this.listener = EL;
        Headers.put("Accept-Language", "en");
    }

    public void Extract(String VideoId) {
        this.execute(VideoId);
    }

    @Override
    protected void onPostExecute(Void result) {
        if (Ex != null) {
            listener.onExtractionGoesWrong(Ex);
        } else
            listener.onExtractionDone(adaptiveMedia, muxedMedia, YTVideoMeta);
    }

    @Override
    protected void onPreExecute() {
        Ex = null;
        adaptiveMedia.clear();
        muxedMedia.clear();
    }

    @Override
    protected void onCancelled() {
        if (Ex != null) {
            listener.onExtractionGoesWrong(Ex);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected Void doInBackground(String[] ids) {
        String Videoid = Utils.extractVideoID(ids[0]);
        String jsonBody;
        try {
            String body = HTTPUtility.downloadPageSource("https://www.youtube.com/watch?v=" + Videoid + "&has_verified=1&bpctr=9999999999", Headers);
            jsonBody = parsePlayerConfig(body);
            parseJson(jsonBody);
            parseUrls();
        } catch (Exception e) {
            Ex = new ExtractorException("Error While getting Youtube Data:" + e.getMessage());
            this.cancel(true);
        }
        return null;
    }

    private String parsePlayerConfig(String body) throws ExtractorException {
        String regexFindReason = "(?<=(class=\"message\">)).*?(?=<)";
        if (Utils.isListContain(reasonUnavialable, RegexUtils.matchGroup(regexFindReason, body))) {
            throw new ExtractorException(RegexUtils.matchGroup(regexFindReason, body));
        }
        if (body.contains("ytplayer.config")) {
            String regexPlayerJson = "(?<=ytplayer.config\\s=).*?((\\}(\n|)\\}(\n|))|(\\}))(?=;)";
            return RegexUtils.matchGroup(regexPlayerJson, body);
        } else {
            throw new ExtractorException("This Video is unavialable");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseUrls() {
        try {
            if (!isLive) {
                parseAdaptiveUrls();
                parseMuxedUrls();
            }
        } catch (IOException e) {
            Ex = new ExtractorException(e.getMessage());
            this.cancel(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseMuxedUrls() throws IOException {
        String url_encoded_fmt_stream_map = response.getArgs().getUrlEncodedFmtStreamMap();
        if (url_encoded_fmt_stream_map.equals("")) return;
        String[] rawUrls = url_encoded_fmt_stream_map.split(",");
        for (String rawUrl : rawUrls) {
            YoutubeMedia media = new YoutubeMedia();
            String[] Decodedurl = rawUrl.split("&");
            for (String part : Decodedurl) {

                if (part.startsWith("url=")) {
                    media.setUrl(URLDecoder.decode(RegexUtils.matchGroup(regexUrl, part), "UTF-8"));
                }
                if (part.startsWith("s=") & useCipher) {
                    media.setDechiperedSig(CipherManager.dechiperSig(URLDecoder.decode(part.replace("s=", ""), "UTF-8"), response.getAssets().getJs()));
                }
                if (part.startsWith("type=")) {
                    URLDecoder.decode(RegexUtils.matchGroup(regexType, part), "UTF-8");
                }
            }
            muxedMedia.add(media);
        }
        muxedMedia = Utils.filterInvalidLinks(muxedMedia);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseAdaptiveUrls() throws IOException {
        String adaptive_fmts = response.getArgs().getAdaptiveFmts();
        String[] rawUrls = adaptive_fmts.split(",");
        for (String rawUrl : rawUrls) {
            YoutubeMedia media = new YoutubeMedia();
            String[] Decodedurl = rawUrl.split("&");
            for (String part : Decodedurl) {
                if (part.startsWith("url=")) {
                    media.setUrl(URLDecoder.decode(RegexUtils.matchGroup(regexUrl, part), "UTF-8"));
                }
                if (part.startsWith("s=") & useCipher) {
                    media.setDechiperedSig(CipherManager.dechiperSig(URLDecoder.decode(part.replace("s=", ""), "UTF-8"), response.getAssets().getJs()));
                }
                if (part.startsWith("type=")) {
                    URLDecoder.decode(RegexUtils.matchGroup(regexType, part), "UTF-8");
                }
            }
            adaptiveMedia.add(media);
        }
        adaptiveMedia = Utils.filterInvalidLinks(adaptiveMedia);
    }

    private void parseJson(String body) throws Exception {
        JsonParser parser = new JsonParser();
        response = new GsonBuilder().serializeNulls().create().fromJson(parser.parse(body), Response.class);
        PlayerResponse playerResponse = new GsonBuilder().serializeNulls().create().fromJson(response.getArgs().getPlayerResponse(), PlayerResponse.class);
        YTVideoMeta = playerResponse.getVideoDetails();
        if (YTVideoMeta.getisLive() || YTVideoMeta.getIsLiveContent()) {
            isLive = true;
        }
        useCipher = YTVideoMeta.getUseChiper();
        if (isLive) parseLiveUrls(playerResponse.getStreamingData());
    }

    private void parseLiveUrls(PlayerResponse.StreamingData data) throws Exception {
        if (data.getHlsManifestUrl() == null) {
            throw new ExtractorException("No link for hls video");
        }
        String hlsPageSource = HTTPUtility.downloadPageSource(data.getHlsManifestUrl());
        String regexhlsLinks = "(https://manifest.googlevideo.com/).*?((?=\\#)|\\z| )";
        List<String> Livelinks = RegexUtils.getAllMatches(regexhlsLinks, hlsPageSource);
        for (String s : Livelinks) {
            YoutubeMedia media = new YoutubeMedia();
            media.setUrl(s);
            muxedMedia.add(media);
        }
    }

    public interface ExtractorListner {
        void onExtractionGoesWrong(ExtractorException e);

        void onExtractionDone(List<YoutubeMedia> adaptiveStream, List<YoutubeMedia> muxedStream, YoutubeMeta meta);
    }
}
