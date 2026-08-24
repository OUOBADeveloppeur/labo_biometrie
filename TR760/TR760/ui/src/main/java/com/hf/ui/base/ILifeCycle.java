package com.hf.ui.base;

/**
 * @author tx
 * @date 2023/6/7 14:53
 * @target 生命周期接口
 */
public interface ILifeCycle {

    void create();

    void restart();

    void onstart();

    void resume();

    void pause();

    void stop();

    void destroy();
}
