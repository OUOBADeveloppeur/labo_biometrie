package com.hf.passport.util;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.lang.reflect.Field;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 线程管理类
 */
public final class ThreadManager {

    /**
     * AsyncTask的默认线程池Executor. 负责长时间的任务(网络访问) 默认5个线程
     */
    public Executor NETWORK_EXECUTOR;

    public ThreadManager() {
        NETWORK_EXECUTOR = initNetworkExecutor();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Executor initNetworkExecutor() {
        Executor result = null;

        try {
            // 3.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                result = AsyncTask.THREAD_POOL_EXECUTOR;
            } else {
                // 3.0以下, 反射获取
                Executor tmp;
                try {
                    Field field = AsyncTask.class.getDeclaredField("sExecutor");
                    field.setAccessible(true);
                    tmp = (Executor) field.get(null);
                } catch (Exception e) {
                    // 反射失败
                    tmp = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
                }
                result = tmp;
            }

            if (result instanceof ThreadPoolExecutor) {
                // core size为3个
                ((ThreadPoolExecutor) result).setCorePoolSize(3);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }

    public void execute(Runnable run) {
        try {
            NETWORK_EXECUTOR.execute(run);
        } catch (Throwable e) {
            e.printStackTrace();
            NETWORK_EXECUTOR = initNetworkExecutor();
        }
    }

}
