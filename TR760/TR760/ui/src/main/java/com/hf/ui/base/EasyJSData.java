package com.hf.ui.base;

/**
 * @author tx
 * @date 2023/6/28 15:40
 * @target this class will do...
 */
public class EasyJSData {
    public Object data="";
    public int code=0;
    public String message="";
    public String EVENT = "";

    public EasyJSData(int code,String msg,Object data,String event){
        this.code = code;
        this.data = data;
        this.message =msg;
        this.EVENT = event;
    }
}
