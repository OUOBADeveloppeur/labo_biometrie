package com.hf.tr760.demo;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.hf.tr760.databinding.ActivityBarcodeBinding;
import com.hf.tr760.utils.TR760Manager;
import com.hf.ui.base.EasyActivity;
import com.hf.ui.base.EasyEvent;

public class BarcodeActivity extends EasyActivity implements TR760Manager.OnScannerListener {
    ActivityBarcodeBinding barcodeBinding;

    ToneGenerator tonePlayer;

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void create() {
        barcodeBinding = ActivityBarcodeBinding.inflate(getLayoutInflater());
        setContentView(barcodeBinding.getRoot());

        barcodeBinding.actionbar.backTitleStyle("BARCODE");

        TR760Manager.getInstance().setScannerListener(this);

        tonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);


        boolean opened = TR760Manager.getInstance().BarcodeOpen();
        if (opened) {

        } else {
            showToast("OPEN FAILED");
            barcodeBinding.logView.error("OPEN FAILED, CAN NOT SCAN");
            return;
        }

        barcodeBinding.scan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP: {
                        TR760Manager.getInstance().BarcodeTrigger(false);
                        break;
                    }
                    case MotionEvent.ACTION_DOWN: {
                        TR760Manager.getInstance().BarcodeTrigger(true);
                        break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        Log.e("F1F2",keyCode+"/"+event.getAction());
        if(keyCode == KeyEvent.KEYCODE_F1||keyCode == KeyEvent.KEYCODE_F2){
            TR760Manager.getInstance().BarcodeTrigger(true);
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        Log.e("F1F2",keyCode+"/"+event.getAction());
        if(keyCode == KeyEvent.KEYCODE_F1||keyCode == KeyEvent.KEYCODE_F2){
            TR760Manager.getInstance().BarcodeTrigger(false);
        }
        return super.onKeyUp(keyCode,event);
    }

    @Override
    public void restart() {

    }

    @Override
    public void onstart() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
        TR760Manager.getInstance().BarcodeClose();
        if (tonePlayer!=null){
            tonePlayer.stopTone();
            tonePlayer.release();
        }
    }

    StringBuilder stringBuilder = new StringBuilder();
    long lastScanTime = 0;
    long lastShowTime = 0;
    @Override
    public void onScan(byte[] bytes) {
        if(lastScanTime == 0 || (System.currentTimeMillis() - lastScanTime) < 300){
            lastScanTime = System.currentTimeMillis();
        }else {
            stringBuilder = new StringBuilder();
            lastScanTime = System.currentTimeMillis();
        }
        if (bytes != null) {
            stringBuilder.append(new String(bytes));
        }
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(lastShowTime == 0 || System.currentTimeMillis()-lastShowTime>1000) {
                    lastShowTime = System.currentTimeMillis();
                    String result = new String(stringBuilder.toString());
                    barcodeBinding.logView.append(result);
                    tonePlayer.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
                    TR760Manager.getInstance().BarcodeTrigger(false);
                }
            }
        },1000);

    }
}
