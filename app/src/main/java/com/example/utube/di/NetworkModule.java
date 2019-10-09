package com.example.utube.di;

import com.example.utube.network.RetrofitApiService;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@Module
public class NetworkModule {

    private static final String BASE_URL = "https://www.googleapis.com/";

    @Provides
    @SingletonApplicationScope
    RetrofitApiService provideRetrofitApiService(Retrofit retrofit) {
        return retrofit.create(RetrofitApiService.class);
    }

    @Provides
    @SingletonApplicationScope
    Retrofit provideRetrofit(OkHttpClient okHttpClient,
                             RxJava2CallAdapterFactory rxJava2CallAdapterFactory,
                             MoshiConverterFactory moshiConverterFactory) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(rxJava2CallAdapterFactory)
                .addConverterFactory(moshiConverterFactory)
                .build();
    }

    @Provides
    @SingletonApplicationScope
    RxJava2CallAdapterFactory provideRxJava2CallAdapterFactory() {
        return RxJava2CallAdapterFactory.create();
    }

    @Provides
    @SingletonApplicationScope
    MoshiConverterFactory provideMoshiConverterFactory() {
        return MoshiConverterFactory.create();
    }

    @Provides
    @SingletonApplicationScope
    OkHttpClient provideOkHttpClient(HttpLoggingInterceptor httpLoggingInterceptor) {
        int REQUEST_TIMEOUT = 60;
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);
        httpClient.addInterceptor(httpLoggingInterceptor);
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            HttpUrl originalHttpUrl = original.url();
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", "AIzaSyAc8VIB-o1G1px3D27PvpnAF-6je3flzOU")
//                    .addQueryParameter("key", "AIzaSyB8Gp7tSeFeIfgRffnBA6BR2UOF9IZj_dg")
//                    .addQueryParameter("key", "AIzaSyBshu2vqOrJr3OZzuShPu3ZncuXBfLvPqY")
//                    .addQueryParameter("key", "AIzaSyA5MQOEdLD_8zTUsAFdREy-eEhiu_6E1sI")
                    .build();
            Request.Builder requestBuilder = original.newBuilder().url(url);
            Request request = requestBuilder.build();
            return chain.proceed(request);
        });
        return httpClient.build();
    }

    @Provides
    @SingletonApplicationScope
    HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return httpLoggingInterceptor;
    }
}
