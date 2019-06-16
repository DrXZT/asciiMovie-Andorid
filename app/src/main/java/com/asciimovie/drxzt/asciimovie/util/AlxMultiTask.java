package com.asciimovie.drxzt.asciimovie.util;

import android.os.AsyncTask;
import android.os.Build;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AlxMultiTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static ExecutorService photosThreadPool;//用于加载大图的线程池
    private final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private final int CORE_POOL_SIZE = CPU_COUNT + 1;

    public void executeDependSDK(Params... params) {
        if (photosThreadPool == null)
            photosThreadPool = Executors.newFixedThreadPool(CORE_POOL_SIZE);
        if (Build.VERSION.SDK_INT < 11) super.execute(params);
        else super.executeOnExecutor(photosThreadPool, params);
    }

}