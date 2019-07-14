package com.example.utube;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static final Object LOCK = new Object();
    private static AppExecutor sInstance;
    private final Executor dataBaseExecutor;

    private AppExecutor(Executor dataBaseExecutor) {
        this.dataBaseExecutor = dataBaseExecutor;
    }

    public static AppExecutor getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new AppExecutor(Executors.newSingleThreadExecutor());
            }
        }
        return sInstance;
    }

    public Executor dataBaseExecutor() {
        return dataBaseExecutor;
    }

}
