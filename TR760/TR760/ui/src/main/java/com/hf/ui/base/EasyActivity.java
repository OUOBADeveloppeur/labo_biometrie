package com.hf.ui.base;

import static com.hf.ui.base.Constance.HEADER_MAP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hf.cache.EasyCache;
import com.hf.image.EasyImgLoader;
import com.hf.image.ICompressResult;
import com.hf.image.ISelectImgResult;
import com.hf.network.EasyRequester;
import com.hf.permission.EasyPermission;
import com.hf.ui.DPPX;
import com.hf.ui.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * @author tx
 * @date 2023/5/30 14:46
 * @target activity基类
 */
public abstract class EasyActivity extends AppCompatActivity implements ILifeCycle,IEventBus{

    //组件
    EasyRequester easyRequester;
    EasyPermission easyPermission;
    EasyCache easyCache;
    EasyImgLoader easyImgLoader;
    EasyLoading easyLoading;

    private static boolean allowQueue = true;
    private static Toast lastToast = null;

    public String getVersionName() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo == null ? "" : packageInfo.versionName;
    }

    public int getVersionCode() {
        PackageInfo packageInfo = getPackageInfo();
        return packageInfo.versionCode;
    }

    private PackageInfo getPackageInfo() {
        try {
            PackageManager e = getPackageManager();
            return e.getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException var1) {
            var1.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注册evetbus接收
        EventBus.getDefault().register(this);
        //沉浸式
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_OVERSCAN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //沉浸式

        //初始化组件
        easyRequester = new EasyRequester();
        easyRequester.setHeader(HEADER_MAP);
        easyPermission = new EasyPermission(this);
        easyCache = new EasyCache(this);
        easyImgLoader = new EasyImgLoader();
        easyLoading = new EasyLoading(this, new EasyLoading.ILoadSrc() {
            @Override
            public int srcLayout() {
                return R.layout.load_layout;
            }

            @Override
            public int srcImgId() {
                return R.id.load_img;
            }
        });
        //初始化组件

        create();
    }

    public void showLoading(){
        easyLoading.show();
    }

    public void hideLoading(){
        easyLoading.dismiss();
    }

    public void hideSystemKeyBoard() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    public void showSystemKeyBoard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        resume();
    }

    @Override
    public void onStart(){
        super.onStart();
        onstart();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        restart();
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
    public void postEvent(EasyEvent msg) {
        EventBus.getDefault().post(msg);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EasyEvent event) {
        onMessageRecieve(event.name, event);
    }

    public <T>void requestRetryMode(EasyRequester.EasyApi<T> easyApi){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading();
                easyRequester.setRetryMode(true);
                easyRequester.request(easyApi, new EasyRequester.IRequestDone() {
                    @Override
                    public void done() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                            }
                        });
                    }
                });
            }
        });
    }
    //请求HTTP
    public <T>void request(EasyRequester.EasyApi<T> easyApi){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading();
                easyRequester.request(easyApi, new EasyRequester.IRequestDone() {
                    @Override
                    public void done() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoading();
                            }
                        });
                    }
                });
            }
        });
    }

    public <T>void request(EasyRequester.EasyApi<T> easyApi,boolean showLoading){
        if(showLoading){
            request(easyApi);
        }else {
            easyRequester.request(easyApi,null);
        }
    }

    public void setHeader(Object object){
        easyRequester.setHeader(object);
    }

    public void setHeader(HashMap<String,String> headerMap){
        easyRequester.setHeader(headerMap);
    }
    //请求HTTP

    //获取权限请求组件
    public EasyPermission getEasyPermission(){
        return easyPermission;
    }

    //加载图片
    public void loadImg(String url, ImageView img){
        easyImgLoader.simple(url,img);
    }
    public void loadImg(File file, ImageView img){
        easyImgLoader.simple(file,img);
    }

    //压缩图片
    public void compressImg(File file, ICompressResult iCompressResult){
        easyImgLoader.compressImage(this,file,iCompressResult);
    }

    //压缩图片
    public void compressImg(File file,ImageView imageView,ICompressResult iCompressResult){
        easyImgLoader.compressImage(this,file,imageView,iCompressResult);
    }

    //图片选择
    public void selectImg(int max,ISelectImgResult iSelectImgResult){
        easyImgLoader.selectImage(this,max,iSelectImgResult);
    }

    //图片选择
    public void takePhoto(ISelectImgResult iSelectImgResult){
        easyImgLoader.takePhoto(this,iSelectImgResult);
    }

    //缓存String
    public void saveStringInfo(String key,String str){
        easyCache.putString(key,str);
    }

    //缓存Object
    public void saveObjectInfo(String key,Object object){
        easyCache.putObject(key,object);
    }

    //获取缓存String
    public String getSaveStringInfo(String key){
        return easyCache.getString(key);
    }

    //获取缓存Object
    public <T> T getSaveObjectInfo(String key,Class<T> tClass){
        return easyCache.getObject(key,tClass);
    }

    //获取缓存Object
    public <T> List<T> getSaveObjectListInfo(String key, Class<T> tClass){
        return easyCache.getListObject(key,tClass);
    }

    //短时间吐司
    public void showToast(String msg) {
        int showtime= Toast.LENGTH_SHORT;
        final Toast currentToast = Toast.makeText(this, "", showtime);
        final View toastLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toast_layout, null);
        TextView textView = toastLayout.findViewById(R.id.text);
        textView.setText(msg);
        currentToast.setView(toastLayout);
        if (!allowQueue) {
            if (lastToast != null)
                lastToast.cancel();
            lastToast = currentToast;
        }
        // Make sure to use default values for non-specified ones.
        currentToast.setGravity(
                Gravity.BOTTOM,
                0,
                DPPX.dip2px(this,200)
        );
        currentToast.show();
    }

    //长时间吐司
    public void showToastLong(String msg) {
        int showtime=Toast.LENGTH_LONG;
        final Toast currentToast = Toast.makeText(this, "", showtime);
        final View toastLayout = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.toast_layout, null);
        TextView textView = toastLayout.findViewById(R.id.text);
        textView.setText(msg);
        currentToast.setView(toastLayout);
        if (!allowQueue) {
            if (lastToast != null)
                lastToast.cancel();
            lastToast = currentToast;
        }
        // Make sure to use default values for non-specified ones.
        currentToast.setGravity(
                Gravity.BOTTOM,
                0,
                DPPX.dip2px(this,200)
        );
        currentToast.show();
    }
    @Override
    public void onDestroy() {

        destroy();

        super.onDestroy();

        //销毁evetbus接收
        EventBus.getDefault().unregister(this);
        //置空组件
        easyRequester = null;
        easyPermission = null;
        easyCache = null;
        easyImgLoader = null;
        //置空组件
    }


    //设置标题栏深色字体
    public void statusBarTextBlack() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    //设置标题栏浅色字体
    public void statusBarTextWhite() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
