package com.hf.tr760.facex.facepass;

import android.hibory.CommonApi;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class DoorApi {
    private static final String  TAG = "DoorApi";
    private CommonApi commonApi;
    private static int GPIO_DOOR_POWER_EN  = 24;
    private static int GPIO_DOOR_POWER_CTL = 52;

    private static int GPIO_TAMPER_DETECT  = 42;
    private static int GPIO_EXIT_BUTTON_DETECT  = 27;

    public static final String HBYAPI_ACTION_EXIT_BUTTON = "hbyapi.intent.action_exit_button";
    public static final String HBYAPI_ACTION_TAMPER_ALARM = "hbyapi.intent.action_tamper_alarm";

    public static void quickOpenDoor(){
        DoorApi doorApi = new DoorApi();
        doorApi.doorInit();
        doorApi.doorOpen(5000);
    }


    public DoorApi(){
        commonApi = new CommonApi();
    }

    /**
     * 防拆开关状态
     * @return state=1 开，0=关
     */
    public int doorTamperDetect(){
        commonApi.setGpioDir(GPIO_TAMPER_DETECT,0);
        return commonApi.getGpioIn(GPIO_TAMPER_DETECT);
    }

    /**
     * 门禁开关状态
     * @return  state=1 按下，0=抬起
     */
    public int doorExitButtonDetect(){
        commonApi.setGpioDir(GPIO_EXIT_BUTTON_DETECT,0);
        return commonApi.getGpioIn(GPIO_EXIT_BUTTON_DETECT);
    }

    public void doorInit(){
        //commonApi.setGpioDir(GPIO_DOOR_POWER_EN,1);
        //commonApi.setGpioOut(GPIO_DOOR_POWER_EN,1);
    }

    public void doorRelayOn(){
        commonApi.setGpioDir(107,1);
        commonApi.setGpioOut(107,1);
    }

    public void doorRelayOff(){
        commonApi.setGpioDir(107,1);
        commonApi.setGpioOut(107,0);
    }


    public void doorOpen(int timeout){
        if(!Build.MODEL.contains("X05")){
            return;
        }
        Log.d(TAG,"doorOpen---> timeout:"+timeout);
        //commonApi.setGpioOut(107,1);
        doorRelayOn();
        if(timeout > 0 && timeout <10*1000){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(commonApi!=null){
                        //doorClose();
                        doorRelayOff();
                    }

                }
            },timeout);
        }
    }



    public void doorClose(){
        if(commonApi!=null){
            Log.d(TAG,"doorClose");
            commonApi.setGpioOut(107,0);
        }

    }



    public void doorDeinit(){
        if(commonApi!=null){
            commonApi = null;
        }
    }
}
