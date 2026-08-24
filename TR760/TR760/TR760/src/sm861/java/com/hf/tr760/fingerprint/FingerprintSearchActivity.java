package com.hf.tr760.fingerprint;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.hf.cache.EasyCache;
import com.hf.tr760.databinding.ActivityFingerPrintSearchBinding;
import com.hf.tr760.utils.ProgressDialogUtils;
import com.hf.ui.base.EasyActivity;
import com.hf.ui.base.EasyEvent;
import com.hfteco.finger.FingerSDK;
import com.hfteco.finger.OnCaptureBytesListener;
import com.hfteco.finger.OnSdkInitListener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FingerprintSearchActivity extends EasyActivity {

    ActivityFingerPrintSearchBinding fingerPrintSearchBinding;

    FingerSDK fingerSDK;

    OnCaptureBytesListener onCaptureBytesListener;

    boolean pause = false;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void create() {
        fingerPrintSearchBinding = ActivityFingerPrintSearchBinding.inflate(getLayoutInflater());
        setContentView(fingerPrintSearchBinding.getRoot());

        fingerPrintSearchBinding.actionbar.backTitleStyle("Fingerprint Search");
        fingerPrintSearchBinding.logView.setLogSizeDp(22);

        EasyCache easyCache = new EasyCache(FingerprintSearchActivity.this,"fp");
        HashMap<String,String> fpMap = easyCache.getAll();

        onCaptureBytesListener = new OnCaptureBytesListener() {
            @Override
            public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1, Bitmap bitmap) {
                if(pause){
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fingerPrintSearchBinding.fingerprintImg.setImageBitmap(bitmap);
                    }
                });
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.SINGLE,onCaptureBytesListener);
                if(i == FingerSDK.RESULT_OK){
                    for(String n:fpMap.keySet()) {
                       int score = fingerSDK.compareTemplateBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005,bytes1[0],fpMap.get(n).getBytes(StandardCharsets.ISO_8859_1));
                       if(score>80){
                           runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   fingerPrintSearchBinding.logView.notification("MATCHED: your finger is "+n);
                               }
                           });
                           return;
                       }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fingerPrintSearchBinding.logView.append("NO MATCHED USER");
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fingerPrintSearchBinding.logView.error("CAPTURE FAILED");
                        }
                    });
                }
            }

            @Override
            public void preview(Bitmap bitmap) {

            }
        };
    }

    @Override
    public void restart() {

    }

    @Override
    public void onstart() {

    }

    @Override
    public void resume() {
        fingerSDK = new FingerSDK(FingerprintSearchActivity.this, 86, new OnSdkInitListener() {
            @Override
            public void initResult(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtils.dismissProgressDialog();
                        if (i == FingerSDK.RESULT_OK) {
                            pause = false;
                            fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.SINGLE,onCaptureBytesListener);
                        } else {
                            pause = true;
                            Toast.makeText(FingerprintSearchActivity.this, s, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onOpticalSensorInterrupt() {

            }

            @Override
            public void onOpticalSensorLost() {

            }
        });
        fingerSDK.launch();
    }

    @Override
    public void pause() {
        pause = true;
        if (fingerSDK != null) {
            fingerSDK.stopCapture();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fingerSDK.release();
                    fingerSDK = null;
                }
            });
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void destroy() {
    }

}
