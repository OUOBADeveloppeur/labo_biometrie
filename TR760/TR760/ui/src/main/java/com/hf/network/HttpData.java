package com.hf.network;

/**
 * @author tx
 * @date 2023/5/27 14:25
 * @target 根据服务端的接口定义最外层的数据结构，可自定义修改
 */
public class HttpData{
    public String result ="";
    public int code = 0;
    public String message="";

    public boolean ok(){
        return code==200;
    }
}