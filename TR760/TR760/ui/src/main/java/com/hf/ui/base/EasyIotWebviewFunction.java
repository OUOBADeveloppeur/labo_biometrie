package com.hf.ui.base;

import android.webkit.JavascriptInterface;

import com.alibaba.fastjson.JSON;
import com.hf.image.ISelectImgResult;
import com.hf.permission.EasyPermission;

import java.util.List;

/**
 * @author tx
 * @date 2023/6/28 13:18
 * @target IOT接口
 */
public abstract class EasyIotWebviewFunction extends EasyWebActivity.EasyWebviewFunction{

    public EasyIotWebviewFunction(){

    }

    public EasyIotWebviewFunction(EasyActivity easyActivity){
        super(easyActivity);
    }

    @JavascriptInterface
    public void takePhoto(){
        activity.getEasyPermission().camera(new EasyPermission.IPermissionResult() {
            @Override
            public void result(boolean allGranted) {
                if(!allGranted){
                    activity.showToast("未获得权限");
                    return;
                }
                activity.takePhoto(new ISelectImgResult() {
                    @Override
                    public void result(List<String> pathList) {
                        sendMessage(JSON.toJSONString(new EasyJSData(200,"成功",pathList.get(0),"takePhoto")));
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void selectImage(int maxNumber){
        activity.getEasyPermission().storage(new EasyPermission.IPermissionResult() {
            @Override
            public void result(boolean allGranted) {
                if(!allGranted){
                    activity.showToast("未获得权限");
                    return;
                }
                activity.selectImg(maxNumber,new ISelectImgResult() {
                    @Override
                    public void result(List<String> pathList) {
                        sendMessage(JSON.toJSONString(new EasyJSData(200,"成功",pathList,"selectImage")));
                    }
                });
            }
        });
    }

    @JavascriptInterface
    public void scan(){

    }

    @JavascriptInterface
    public void setNFCReadEnable(boolean readNfc){

    }

    @JavascriptInterface
    public void searchBle(){

    }

    @JavascriptInterface
    public void locationOnce(){

    }

    @JavascriptInterface
    public void locationStart(){

    }

    @JavascriptInterface
    public void locationStop(){

    }
}
