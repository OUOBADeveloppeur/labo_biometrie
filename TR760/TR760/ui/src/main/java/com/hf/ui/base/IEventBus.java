package com.hf.ui.base;


/**
 * @author tx
 * @date 2023/1/16 10:07
 * @target 实现EVENTBUS消息发送和接受
 */
public interface IEventBus {
    //发消息接口
    void postEvent(EasyEvent msg);

    //接消息接口
    void onMessageRecieve(String name,EasyEvent msg);
}
