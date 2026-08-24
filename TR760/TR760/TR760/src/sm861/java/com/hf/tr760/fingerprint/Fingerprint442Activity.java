package com.hf.tr760.fingerprint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hf.cache.EasyCache;
import com.hf.tr760.R;
import com.hf.tr760.utils.ProgressDialogUtils;
import com.hfteco.finger.FingerSDK;
import com.hfteco.finger.OnCaptureBytesListener;
import com.hfteco.finger.OnSdkInitListener;
import com.hfteco.finger.OnVideoListener;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Fingerprint442Activity extends Activity implements DialogInterface.OnDismissListener {

    FingerSDK fingerSDK;

    Button test_left_four;
    Button test_right_four;
    Button test_both_thumb;
    Button test_single;
    Button test_roll;
    Button test_three;

    Button enroll_all;

    Button video;

    SeekBar nfiq_seekbar;
//    ImageView preview_img;

    HashMap<String,String> tempMap = new HashMap<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fp_442);


        ImageView quit = findViewById(R.id.quit);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        test_left_four = findViewById(R.id.test_left_four);
        test_right_four = findViewById(R.id.test_right_four);
        test_both_thumb = findViewById(R.id.test_both_thumb);
        test_single = findViewById(R.id.test_single);
        test_roll = findViewById(R.id.test_roll);
        test_three = findViewById(R.id.test_three);
        enroll_all = findViewById(R.id.enroll_all);
        video = findViewById(R.id.video);
        nfiq_seekbar = findViewById(R.id.nfiq_seekbar);
        nfiq_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fingerSDK.setNfiq(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button stop = findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fingerSDK.stopCapture()){
                    Toast.makeText(Fingerprint442Activity.this,"stop SUCCESS",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(Fingerprint442Activity.this,"stop FAILED",Toast.LENGTH_SHORT).show();
                }
                enableButton(true);
            }
        });

        test_left_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Press finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.LEFT_FOUR, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                                if (bitmaps != null && bitmaps.length == 4) {
                                    ((ImageView) findViewById(R.id.l_fp1)).setImageBitmap(bitmaps[0]);
                                    ((ImageView) findViewById(R.id.l_fp2)).setImageBitmap(bitmaps[1]);
                                    ((ImageView) findViewById(R.id.l_fp3)).setImageBitmap(bitmaps[2]);
                                    ((ImageView) findViewById(R.id.l_fp4)).setImageBitmap(bitmaps[3]);
                                    tempMap.put("lfp1",new String(bytes1[0], StandardCharsets.ISO_8859_1));
                                    tempMap.put("lfp2",new String(bytes1[1], StandardCharsets.ISO_8859_1));
                                    tempMap.put("lfp3",new String(bytes1[2], StandardCharsets.ISO_8859_1));
                                    tempMap.put("lfp4",new String(bytes1[3], StandardCharsets.ISO_8859_1));
                                }
                            }
                        });
                    }
                });
            }
        });

        test_right_four.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Press finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.RIGHT_FOUR, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                                if (bitmaps != null && bitmaps.length == 4) {
                                    ((ImageView) findViewById(R.id.r_fp1)).setImageBitmap(bitmaps[0]);
                                    ((ImageView) findViewById(R.id.r_fp2)).setImageBitmap(bitmaps[1]);
                                    ((ImageView) findViewById(R.id.r_fp3)).setImageBitmap(bitmaps[2]);
                                    ((ImageView) findViewById(R.id.r_fp4)).setImageBitmap(bitmaps[3]);
                                    tempMap.put("rfp1",new String(bytes1[0], StandardCharsets.ISO_8859_1));
                                    tempMap.put("rfp2",new String(bytes1[1], StandardCharsets.ISO_8859_1));
                                    tempMap.put("rfp3",new String(bytes1[2], StandardCharsets.ISO_8859_1));
                                    tempMap.put("rfp4",new String(bytes1[3], StandardCharsets.ISO_8859_1));
                                }
                            }
                        });
                    }
                });
            }
        });

        test_both_thumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Press finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.BOTH_THUMB, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                                if (bitmaps != null && bitmaps.length == 2) {
                                    ((ImageView) findViewById(R.id.t_fp1)).setImageBitmap(bitmaps[0]);
                                    ((ImageView) findViewById(R.id.t_fp2)).setImageBitmap(bitmaps[1]);
                                    tempMap.put("tfp1",new String(bytes1[0], StandardCharsets.ISO_8859_1));
                                    tempMap.put("tfp2",new String(bytes1[1], StandardCharsets.ISO_8859_1));
                                }
                            }
                        });
                    }
                });
            }
        });

        test_single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Press finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.SINGLE, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        test_roll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Roll finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.ROLL, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        test_three.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,"Press finger...",Fingerprint442Activity.this);
                fingerSDK.captureBytes(FingerSDK.TEMPLEATES.ISO_19794_2_2005, FingerSDK.FingerType.FINGER_THREE, new OnCaptureBytesListener() {
                    @Override
                    public void preview(Bitmap b){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                            }
                        });
                    }
                    @Override
                    public void capture(int i, byte[][] bytes, Bitmap[] bitmaps, byte[][] bytes1 ,Bitmap b) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ProgressDialogUtils.dismissProgressDialog();
                                enableButton(true);
                                if(b!=null){
                                    ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(b);
                                }
                                if (i == FingerSDK.RESULT_OK) {
                                    Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Fingerprint442Activity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        enroll_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempMap.isEmpty()){
                    Toast.makeText(Fingerprint442Activity.this, "press finger first", Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Fingerprint442Activity.this);
                builder.setTitle("input your name");
                // 设置对话框的视图为一个EditText
                final EditText input = new EditText(Fingerprint442Activity.this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String inputText = input.getText().toString();
                        if (!TextUtils.isEmpty(inputText)) {
                            EasyCache easyCache = new EasyCache(Fingerprint442Activity.this,"fp");
                            for(String key:tempMap.keySet()){
                                easyCache.putString( inputText+"-"+key,tempMap.get(key));
                            }
                            Toast.makeText(Fingerprint442Activity.this, getString(R.string.enroll_success), Toast.LENGTH_SHORT).show();
                            clearImgAndMap();
                        } else {
                            Toast.makeText(Fingerprint442Activity.this, "name is null", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableButton(false);
                fingerSDK.video(new OnVideoListener() {
                    @Override
                    public void preview(Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) findViewById(R.id.unsegmented)).setImageBitmap(bitmap);
                            }
                        });
                    }
                });
            }
        });
    }

    public void enableButton(boolean enable){
        List<Button> buttonList = Arrays.asList(test_left_four,test_right_four,test_both_thumb,test_single,test_roll,test_three,enroll_all,video);
        for(Button b:buttonList) {
            b.setEnabled(enable);
        }
    }


    public void onResume() {
        super.onResume();
        test_left_four.setVisibility(View.GONE);
        test_right_four.setVisibility(View.GONE);
        test_both_thumb.setVisibility(View.GONE);
        test_single.setVisibility(View.GONE);
        test_roll.setVisibility(View.GONE);
        test_three.setVisibility(View.GONE);
        enroll_all.setVisibility(View.GONE);
        video.setVisibility(View.GONE);

        ProgressDialogUtils.showProgressDialogForce(Fingerprint442Activity.this, "LOADING...");

        fingerSDK = new FingerSDK(Fingerprint442Activity.this, 86, new OnSdkInitListener() {
            @Override
            public void initResult(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogUtils.dismissProgressDialog();
                        if (i == FingerSDK.RESULT_OK) {
                            test_left_four.setVisibility(View.VISIBLE);
                            test_right_four.setVisibility(View.VISIBLE);
                            test_both_thumb.setVisibility(View.VISIBLE);
                            test_single.setVisibility(View.VISIBLE);
                            test_roll.setVisibility(View.VISIBLE);
                            test_three.setVisibility(View.VISIBLE);
                            enroll_all.setVisibility(View.VISIBLE);
                            video.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(Fingerprint442Activity.this, s, Toast.LENGTH_SHORT).show();
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

    public void onPause() {
        super.onPause();
        if (fingerSDK != null) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    fingerSDK.release();
                }
            });
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (fingerSDK != null) {
            fingerSDK.stopCapture();
        }
    }

    public void clearImgAndMap(){
        ((ImageView) findViewById(R.id.l_fp1)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.l_fp2)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.l_fp3)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.l_fp4)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.r_fp1)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.r_fp2)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.r_fp3)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.r_fp4)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.t_fp1)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.t_fp2)).setImageResource(R.drawable.ic_fingerprint);
        ((ImageView) findViewById(R.id.unsegmented)).setImageResource(0);
        tempMap.clear();
    }
}
