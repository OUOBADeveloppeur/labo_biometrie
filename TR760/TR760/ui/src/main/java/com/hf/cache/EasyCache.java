package com.hf.cache;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;

import java.util.HashMap;
import java.util.List;

/**
 * @author tx
 * @date 2023/5/30 10:17
 * @target 简单的数据缓存
 */
public class EasyCache {

    SharedPreferences sharedPreferences;

    public EasyCache(Context context){
        sharedPreferences = context.getSharedPreferences(context.getPackageName(),Context.MODE_PRIVATE);
    }


    public EasyCache(Context context,String name){
        sharedPreferences = context.getSharedPreferences(context.getPackageName()+"-"+name,Context.MODE_PRIVATE);
    }

    public HashMap<String, String> getAll(){
        return (HashMap<String,String>)sharedPreferences.getAll();
    }


    public void putString(String key,String str){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, str).commit();
    }

    public void putObject(String key,Object object){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, JSON.toJSONString(object)).commit();
    }

    public String getString(String key){
        return sharedPreferences.getString(key,"");
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,-1);
    }

    public void pubInt(String key,int value){
        sharedPreferences.edit().putInt(key,value).commit();
    }

    public <T>T getObject(String key,Class<T> tClass){
        String objectString =  sharedPreferences.getString(key,"{}");
        return JSON.parseObject(objectString,tClass);
    }

    public <T> List<T> getListObject(String key, Class<T> tClass){
        String objectListString =  sharedPreferences.getString(key,"[]");
        return JSON.parseArray(objectListString,tClass);
    }

    public void clear(){
        sharedPreferences.edit().clear().commit();
    }
}
