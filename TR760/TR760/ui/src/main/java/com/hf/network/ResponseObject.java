package com.hf.network;

import java.util.List;

/**
 * @author tx
 * @date 2023/5/26 16:26
 * @target 这个类存放了数据请求的结果，并提供回调接口。
 */
public abstract class ResponseObject<T> {
    public BaseRequester.IRequestDone iRequestDone = null;
    //如果为对象存为T
    public T t;
    //如果为列表存为List<T>
    public List<T> tList;

    //需要转化实例的类的class
    private Class<T> tClass;

    //初始化
    public ResponseObject(Class<T> tClass){
        this.tClass = tClass;
    }

    //获取转化实例的类的class
    public Class<T> getT(){
        return tClass;
    }

    //回调对象
    public abstract void result(T t);

    //回调列表
    public abstract void resultList(List<T> tList);

    //回调失败
    public abstract void failed(int code,String httpReason);
}
