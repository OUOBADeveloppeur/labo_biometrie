package com.hf.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author tx
 * @date 2023/6/8 9:30
 * @target 简单使用Activity的功能
 */
public abstract class EasyFragment extends Fragment implements ILifeCycle,IEventBus{

    public abstract View buildView();

    public EasyActivity getEasyActivity(){
        return (EasyActivity) getActivity();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EasyEvent event) {
        onMessageRecieve(event.name, event);
    }

    @Override
    public void postEvent(EasyEvent msg) {
        EventBus.getDefault().post(msg);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        return buildView();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        create();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        resume();
    }

    @Override
    public void onPause(){
        super.onPause();
        pause();
    }

    @Override
    public void onStop(){
        super.onStop();
        stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();

        EventBus.getDefault().unregister(this);
    }
}
