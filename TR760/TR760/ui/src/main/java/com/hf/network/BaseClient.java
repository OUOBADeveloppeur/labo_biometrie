package com.hf.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author tx
 * @date 2023/5/26 15:10
 * @target OkHttpClient
 */
public class BaseClient {

    private OkHttpClient client;

    public OkHttpClient getClient(){
        return client;
    }

    public BaseClient(int connectTimeOut, int readTimeOut){
        client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)
                .build();
    }

    public BaseClient(){
        client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }
}
