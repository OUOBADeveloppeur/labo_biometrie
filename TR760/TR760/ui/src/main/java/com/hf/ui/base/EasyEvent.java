package com.hf.ui.base;

/**
 * @author tx
 * @date 2023/6/8 10:21
 * @target 消息对象
 */
public class EasyEvent {
    public String name="";
    public String info="";
    public Object data="";

    public EasyEvent(){

    }

    public EasyEvent(String name){
        super();
        this.name = name;
    }

    public EasyEvent(String name,String info){
        super();
        this.name = name;
        this.info = info;
    }

    public EasyEvent(String name,String info,Object data){
        super();
        this.name = name;
        this.info = info;
        this.data = data;
    }
}
