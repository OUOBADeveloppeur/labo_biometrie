package com.hf.tr760.utils;

import static java.lang.Thread.sleep;

import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeviceUtils {
    public static void checkDevice(Activity activity) {
        new Thread(new Runnable() {
            boolean running = true;

            @Override
            public void run() {
                while (running) {
                    String res = shellExec("lsusb");
                    if (res.indexOf("0400:c35a") > 0) {
                        String list = shellExec("ls /dev");
                    }
                    try {
                        sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static String shellExec(String command) {
        String result = "";
        Runtime mRuntime = Runtime.getRuntime();
        try {
            //Process中封装了返回的结果和执行错误的结果
            Process mProcess = mRuntime.exec(command);
            BufferedReader mReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
            StringBuffer mRespBuff = new StringBuffer();
            char[] buff = new char[1024];
            int ch = 0;
            while ((ch = mReader.read(buff)) != -1) {
                mRespBuff.append(buff, 0, ch);
            }
            mReader.close();
            result = mRespBuff.toString();
        } catch (IOException e) {
            e.printStackTrace();
            result = "Execution error:" + e.toString();
        }
        return result;
    }
}
