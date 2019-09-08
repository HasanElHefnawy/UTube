package com.example.utube.extractor;

class Response {

    private Args args;
    private Assets assets;

    Args getArgs() {
        return args;
    }

    Assets getAssets() {
        return assets;
    }

    public class Args {

        private String adaptive_fmts;
        private String player_response;
        private String url_encoded_fmt_stream_map;

        String getAdaptiveFmts() {
            return adaptive_fmts;
        }

        String getPlayerResponse() {
            return player_response;
        }

        String getUrlEncodedFmtStreamMap() {
            return url_encoded_fmt_stream_map;
        }
    }

    public class Assets {

        private String js;

        String getJs() {
            if (js.startsWith("http") && js.contains("youtube.com")) {
                return js;
            } else return "https://youtube.com" + js;
        }
    }
}
	
	
