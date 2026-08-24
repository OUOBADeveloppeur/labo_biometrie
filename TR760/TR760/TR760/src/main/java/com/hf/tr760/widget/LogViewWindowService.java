package com.hf.tr760.widget;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hf.tr760.R;
import com.hf.ui.DPPX;

import java.lang.reflect.Field;

public class LogViewWindowService extends Service implements View.OnTouchListener {

    private WindowManager windowManager;
    private View floatingView;

    private WindowManager.LayoutParams mWindowParams;

    private float mInViewX= 0;
    private float mInViewY= 0;
    private float mDownInScreenX;
    private float mDownInScreenY;
    private float mInScreenX;
    private float mInScreenY;

    private LogView logView;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return floatLayoutTouch(motionEvent);
    }

    public abstract class LogInfoReceiver extends BroadcastReceiver{

        abstract void getData(String t,String d);

        public final static String LOG_INFO_ACTION = "LOG_INFO_ACTION";
        public final static String LOG_INFO_TYPE = "LOG_INFO_TYPE";
        public final static String LOG_INFO_DATA = "LOG_INFO_DATA";
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(LOG_INFO_ACTION)){
                String type = intent.getStringExtra(LOG_INFO_TYPE);
                String data = intent.getStringExtra(LOG_INFO_DATA);
                getData(type,data);
            }
        }
    }

    public static void sendLogInfoError(Context context,String data){
        Intent intent = new Intent();
        intent.setAction(LogInfoReceiver.LOG_INFO_ACTION);
        intent.putExtra(LogInfoReceiver.LOG_INFO_TYPE,"error");
        intent.putExtra(LogInfoReceiver.LOG_INFO_DATA,data);
        context.sendBroadcast(intent);
    }

    public static void sendLogInfoAppend(Context context,String data){
        Intent intent = new Intent();
        intent.setAction(LogInfoReceiver.LOG_INFO_ACTION);
        intent.putExtra(LogInfoReceiver.LOG_INFO_TYPE,"append");
        intent.putExtra(LogInfoReceiver.LOG_INFO_DATA,data);
        context.sendBroadcast(intent);
    }

    public static void sendLogInfoNotification(Context context,String data){
        Intent intent = new Intent();
        intent.setAction(LogInfoReceiver.LOG_INFO_ACTION);
        intent.putExtra(LogInfoReceiver.LOG_INFO_TYPE,"notification");
        intent.putExtra(LogInfoReceiver.LOG_INFO_DATA,data);
        context.sendBroadcast(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化悬浮窗视图
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_window, null);
        logView = floatingView.findViewById(R.id.log_view);
        floatingView.setOnTouchListener(this);

        // 获取WindowManager服务
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 创建悬浮窗参数
        mWindowParams = new WindowManager.LayoutParams(
                DPPX.dip2px(this,200),
                DPPX.dip2px(this,200),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowParams.gravity = Gravity.START | Gravity.TOP;
        // 设置窗体显示类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mWindowParams.type =WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // 添加悬浮窗
        windowManager.addView(floatingView, mWindowParams);

        registerReceiver(new LogInfoReceiver() {
            @Override
            void getData(String t, String d) {
                switch (t){
                    case "append":{
                        logView.showBroadcast(d, LogView.LOG_TYPE.INFO);
                        break;
                    }
                    case "error":{
                        logView.showBroadcast(d, LogView.LOG_TYPE.ERROR);
                        break;
                    }
                    case "notification":{
                        logView.showBroadcast(d, LogView.LOG_TYPE.NOTIFICATION);
                        break;
                    }
                }
            }
        },new IntentFilter(LogInfoReceiver.LOG_INFO_ACTION));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除悬浮窗
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean floatLayoutTouch(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 获取相对View的坐标，即以此View左上角为原点
                mInViewX = motionEvent.getX();
                mInViewY = motionEvent.getY();
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                mDownInScreenX = motionEvent.getRawX();
                mDownInScreenY = motionEvent.getRawY() - getSysBarHeight(this);
                mInScreenX = motionEvent.getRawX();
                mInScreenY = motionEvent.getRawY() - getSysBarHeight(this);
                break;
            case MotionEvent.ACTION_MOVE:
                // 更新浮动窗口位置参数
                mInScreenX = motionEvent.getRawX();
                mInScreenY = motionEvent.getRawY() - getSysBarHeight(this);
                mWindowParams.x = (int) (mInScreenX- mInViewX);
                mWindowParams.y = (int) (mInScreenY - mInViewY);
                // 手指移动的时候更新小悬浮窗的位置
                windowManager.updateViewLayout(floatingView, mWindowParams);
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (mDownInScreenX  == mInScreenX && mDownInScreenY == mInScreenY){

                }
                break;
        }
        return true;
    }

    public void showFloatWindow(){
        if (floatingView.getParent() == null){
            DisplayMetrics metrics = new DisplayMetrics();
            // 默认固定位置，靠屏幕右边缘的中间
            windowManager.getDefaultDisplay().getMetrics(metrics);
            mWindowParams.x = metrics.widthPixels;
            mWindowParams.y = metrics.heightPixels/2 - getSysBarHeight(this);
            windowManager.addView(floatingView, mWindowParams);
        }
    }

    public void hideFloatWindow(){
        if (floatingView.getParent() != null)
            windowManager.removeView(floatingView);
    }

    public void setFloatLayoutAlpha(boolean alpha){
        if (alpha)
            floatingView.setAlpha((float) 0.5);
        else
            floatingView.setAlpha(1);
    }

    // 获取系统状态栏高度
    public static int getSysBarHeight(Context contex) {
        Class<?> c;
        Object obj;
        Field field;
        int x;
        int sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = contex.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

}
