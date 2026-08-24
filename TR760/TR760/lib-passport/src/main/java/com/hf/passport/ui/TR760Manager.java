package com.hf.passport.ui;

import android.hibory.CommonApi;
import android.os.Build;
import android.util.Log;


public class TR760Manager {
    private static String TAG = TR760Manager.class.getSimpleName();
    private static TR760Manager instance;
    private ReadThread mReadThread;
    private CommonApi mCommonApi = new CommonApi();
    private int mComFd = -1;
    private boolean isStart = false;
    private OnScannerListener scannerListener;

    public static TR760Manager getInstance() {
        if (null == instance) {
            instance = new TR760Manager();
        }

        return instance;
    }

    public TR760Manager() {
    }

    public void setScannerListener(TR760Manager.OnScannerListener scannerListener) {
        this.scannerListener = scannerListener;
    }

    public boolean BarcodeOpen() {
        String devName = Build.MODEL;
        Log.d(TAG, "BarcodeOpen:" + devName);
        this.mCommonApi.setGpioDir(152, 1);
        this.mCommonApi.setGpioOut(152, 1);
        this.mComFd = this.mCommonApi.openCom("/dev/ttyS0", 9600, 8, 'N', 1);
        if (this.mComFd < 0) {
            Log.d(TAG, "BarcodeOpen: open com fail");
            return false;
        } else {
            this.BarcodeTrigger(false);

            try {
                Thread.sleep(500L);
            } catch (Exception var3) {
                var3.printStackTrace();
            }

            byte[] serial = new byte[]{126, 0, 8, 1, 0, 13, -96, -85, -51};
            this.mCommonApi.writeCom(this.mComFd, serial, serial.length);
            this.isStart = true;
            this.mReadThread = new TR760Manager.ReadThread();
            this.mReadThread.start();
            return true;
        }
    }

    public void BarcodeClose() {
        this.isStart = false;
        String devName = Build.MODEL;
        Log.d(TAG, "BarcodeClose:" + devName);
        this.mCommonApi.setGpioDir(152, 1);
        this.mCommonApi.setGpioOut(152, 0);
        if (this.mCommonApi != null) {
            this.mCommonApi.closeCom(this.mComFd);
        }

    }

    public void BarcodeTrigger(boolean trigger) {
        String devName = Build.MODEL;
        Log.d(TAG, "BarcodeTrigger:" + devName + " trigger:" + trigger);
        this.mCommonApi.setGpioDir(150, 1);
        if (trigger) {
            this.mCommonApi.setGpioOut(150, 1);
        } else {
            this.mCommonApi.setGpioOut(150, 0);
        }
    }
    public interface OnScannerListener {
        void onScan(byte[] var1);
    }

    private class ReadThread extends Thread {
        private ReadThread() {
        }

        public void run() {
            super.run();

            while(TR760Manager.this.isStart) {
                try {
                    byte[] buffer = new byte[256];
                    int readLen = TR760Manager.this.mCommonApi.readComEx(TR760Manager.this.mComFd, buffer, 256, 0, 500000);
                    if (readLen > 0) {
                        byte[] realData = new byte[readLen];
                        System.arraycopy(buffer, 0, realData, 0, readLen);
                        String txt = new String(realData);
                        Log.d(TR760Manager.TAG, "recv:" + txt);
                        if (TR760Manager.this.scannerListener != null) {
                            TR760Manager.this.scannerListener.onScan(realData);
                        }
                    }
                } catch (Exception var5) {
                    var5.printStackTrace();
                    return;
                }
            }
            Log.d(TR760Manager.TAG, "ReadThread Finish!!!");
        }
    }

    public void IrPower(boolean trigger) {
        String devName = Build.MODEL;
        Log.d(TAG, "IrPower:" + devName + " open:" + trigger);
        this.mCommonApi.setGpioDir(87, 1);
        if (trigger) {
            this.mCommonApi.setGpioOut(87, 1);
            this.mCommonApi.setUsbHost(1);
        } else {
            this.mCommonApi.setGpioOut(87, 0);
            this.mCommonApi.setUsbHost(0);
        }
    }

    public void PSAMPower(boolean trigger) {
        String devName = Build.MODEL;
        Log.d(TAG, "IrPower:" + devName + " open:" + trigger);
        this.mCommonApi.setGpioDir(109, 1);
        if (trigger) {
            this.mCommonApi.setGpioOut(109, 1);
        } else {
            this.mCommonApi.setGpioOut(109, 0);
        }
    }

    public void WhiteLightPower(boolean trigger) {
        String devName = Build.MODEL;
        Log.d(TAG, "WhiteLightPower:" + devName + " open:" + trigger);
        this.mCommonApi.setGpioDir(165, 1);
        if (trigger) {
            this.mCommonApi.setGpioOut(165, 1);
        } else {
            this.mCommonApi.setGpioOut(165, 0);
        }
    }
}
