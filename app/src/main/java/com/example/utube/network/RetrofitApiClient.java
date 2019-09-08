package com.example.utube.network;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class RetrofitApiClient {

    private static Retrofit retrofit = null;
    private static OkHttpClient okHttpClient;
    private static final String BASE_URL = "https://www.googleapis.com/";

    public static Retrofit getClient() {
        if (okHttpClient == null)
            initOkHttp();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static void initOkHttp() {
        int REQUEST_TIMEOUT = 60;
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(interceptor);

        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", "AIzaSyAc8VIB-o1G1px3D27PvpnAF-6je3flzOU")
//                        .addQueryParameter("key", "AIzaSyB8Gp7tSeFeIfgRffnBA6BR2UOF9IZj_dg")
//                        .addQueryParameter("key", "AIzaSyBshu2vqOrJr3OZzuShPu3ZncuXBfLvPqY")
//                        .addQueryParameter("key", "AIzaSyA5MQOEdLD_8zTUsAFdREy-eEhiu_6E1sI")
                    .build();
            Request.Builder requestBuilder = original.newBuilder()
                    .url(url);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
        okHttpClient = httpClient.build();
    }
}
