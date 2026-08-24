package com.hf.tr760.fingerprint;

import static com.fingers.fap60.bean.EnumPrintType.ROLL;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.fingerprint.algorithm.FingerAlgAPI;
import com.fingers.fap60.FingerApi;
import com.fingers.fap60.bean.CaptureConfig;
import com.fingers.fap60.bean.EnumDeviceState;
import com.fingers.fap60.bean.EnumFingerPosition;
import com.fingers.fap60.bean.EnumPrintType;
import com.fingers.fap60.bean.common.MxImage;
import com.fingers.fap60.bean.common.MxResult;
import com.fingers.fap60.driver.fap6002.FapInitParam;
import com.hf.cache.EasyCache;
import com.hf.tr760.R;
import com.hf.tr760.databinding.ActivityFp442Binding;
import com.hf.tr760.utils.ProgressDialogUtils;
import com.hf.tr760.utils.TR760Manager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Fingerprint442Activity extends AppCompatActivity implements DialogInterface.OnDismissListener {

    private ActivityFp442Binding binding;
    private final FingerApi mFingerApi = FingerApi.getInstance();
    private final FingerAlgAPI mFingerAlgAPI = new FingerAlgAPI();

    public static final int TEMPLATE_LENGTH = 1024;

    public static final int MIN_MINUTIAE_COUNT = 12;
    private int nfiq;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private HashMap<String, String> tempMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TR760Manager.getInstance().fingerprintPower(true);
        binding = ActivityFp442Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.quit.setOnClickListener(v -> finish());
        mFingerApi.init(getApplicationContext(), FapInitParam.LOG_OPEN, state -> {
            if (state == EnumDeviceState.HAVE_DEVICE || state == EnumDeviceState.DEVICE_ATTACHED) {
                openDevice();
            } else if (state == EnumDeviceState.DEVICE_DETACHED) {
                closeDevice();
            }
        });
        binding.nfiqSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nfiq = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        nfiq = binding.nfiqSeekbar.getProgress();
        binding.stop.setOnClickListener(v -> {
            mFingerApi.stopCapture();
        });
        binding.testLeftFour.setOnClickListener(v -> test(EnumPrintType.LEFT_FOUR));
        binding.testRightFour.setOnClickListener(v -> test(EnumPrintType.RIGHT_FOUR));
        binding.testBothThumb.setOnClickListener(v -> test(EnumPrintType.BOTH_THUMB));
        binding.testSingle.setOnClickListener(v -> test(EnumPrintType.SINGLE));
        binding.testRoll.setOnClickListener(v -> test(EnumPrintType.ROLL));
        binding.testThree.setOnClickListener(v -> test(EnumPrintType.PREVIEW));
        binding.enrollAll.setOnClickListener(v -> {
            if (tempMap.isEmpty()) {
                Toast.makeText(Fingerprint442Activity.this, "press finger first", Toast.LENGTH_SHORT).show();
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(Fingerprint442Activity.this);
            builder.setTitle("input your name");
            // 设置对话框的视图为一个EditText
            final EditText input = new EditText(Fingerprint442Activity.this);
            builder.setView(input);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String inputText = input.getText().toString();
                if (!TextUtils.isEmpty(inputText)) {
                    EasyCache easyCache = new EasyCache(Fingerprint442Activity.this, "fp");
                    for (String key : tempMap.keySet()) {
                        easyCache.putString(inputText + "-" + key, tempMap.get(key));
                    }
                    Toast.makeText(Fingerprint442Activity.this, getString(R.string.enroll_success), Toast.LENGTH_SHORT).show();
                    clearImgAndMap();
                } else {
                    Toast.makeText(Fingerprint442Activity.this, "name is null", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("CANCEL", (dialog, which) -> dialog.cancel());
            builder.create().show();
        });
        View.OnClickListener startVideo = v -> {
            String stopText = "stop";
            if (stopText.equals(binding.video.getText().toString())) {
                mFingerApi.stopCapture();
//                enableButton(true);
//                binding.video.setText("video mode");
                return;
            }
            enableButton(false);
            binding.video.setText(stopText);
            binding.video.setEnabled(true);
            executor.execute(() -> {
                int re = mFingerApi.video((image, mxError) -> {
                    if (image != null) {
                        byte[] imageDate = new byte[image.width * image.height + 1078];
                        mFingerAlgAPI.convertRawToBMP(image.data, image.width, image.height, imageDate);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageDate, 0, imageDate.length);
                        runOnUiThread(() -> binding.unsegmented.setImageBitmap(bitmap));
                    }
                });
                runOnUiThread(() -> {
                    enableButton(true);
                    binding.video.setText("video mode");
                });

            });
        };
        binding.video.setOnClickListener(startVideo);
    }

    private byte[] hostEnroll(MxImage.RawInfo rawInfo) {
        byte[] tempFeature = new byte[TEMPLATE_LENGTH];
        int[] templateLength = new int[1];
        int result = createTemplate(rawInfo.imageData, rawInfo.width, rawInfo.height,
                tempFeature, templateLength);
        if (result < 0) {
            return null;
        }
        return Arrays.copyOf(tempFeature, templateLength[0]);
    }

    private int createTemplate(byte[] data, int width, int height, byte[] newTemplate, int[] length) {
        return mFingerAlgAPI.createTemplateISO(data, width, height, MIN_MINUTIAE_COUNT, newTemplate, true, length);
    }


    private void test(EnumPrintType captureType) {
        enableButton(false);
        ProgressDialogUtils.showProgressDialog(Fingerprint442Activity.this,
                "Press finger...", Fingerprint442Activity.this);
       executor.execute(() -> {
           MxResult<MxImage> capture = getFinalImage(captureType);
           runOnUiThread(() -> {
               if (MxResult.isSuccess(capture)) {
                   Toast.makeText(Fingerprint442Activity.this, "Success", Toast.LENGTH_SHORT).show();
               } else {
                   Toast.makeText(Fingerprint442Activity.this, "Failed:" + capture.getMsg(), Toast.LENGTH_SHORT).show();
               }
               ProgressDialogUtils.dismissProgressDialog();
               enableButton(true);
           });
       });
    }

    private void openDevice() {
        executor.execute(() -> {
            int result = mFingerApi.openDevice();
            runOnUiThread(() -> {
                if (result == 0) {
                    binding.testLeftFour.setVisibility(View.VISIBLE);
                    binding.testRightFour.setVisibility(View.VISIBLE);
                    binding.testBothThumb.setVisibility(View.VISIBLE);
                    binding.testSingle.setVisibility(View.VISIBLE);
                    binding.testRoll.setVisibility(View.VISIBLE);
//                    binding.testThree.setVisibility(View.VISIBLE);
                    binding.enrollAll.setVisibility(View.VISIBLE);
                    binding.video.setVisibility(View.VISIBLE);
                } else {
                    String s = "DEVICE OPEN FAILED:" + result;
                    Toast.makeText(Fingerprint442Activity.this, s, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }


    @WorkerThread
    private MxResult<MxImage> getFinalImage(EnumPrintType captureType) {
        ArrayList<EnumFingerPosition> missingFingers = new ArrayList<>();
        CaptureConfig.Builder captureConfigBuilder = new CaptureConfig.Builder()
                .setEnumPrintType(captureType)
                .setMissingFinger(missingFingers)
                .setAreaScore(CaptureConfig.DEFAULT_AREA_SCORE)
                .setNfiqLevel(nfiq)
                .setPreviewCallBack((mxImage, mxError) -> {
                    if (mxImage != null) {
                        byte[] imageDate = new byte[mxImage.width * mxImage.height + 1078];
                        mFingerAlgAPI.convertRawToBMP(mxImage.data, mxImage.width, mxImage.height, imageDate);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imageDate, 0, imageDate.length);
                        runOnUiThread(() -> {
                            binding.unsegmented.setImageBitmap(bitmap);
                        });
                    }
                });
        if (captureType == ROLL) captureConfigBuilder.setAreaScore(CaptureConfig.DEFAULT_ROLL_AREA_SCORE);
        CaptureConfig captureConfig = captureConfigBuilder.build();
        MxResult<MxImage> capture = mFingerApi.getImage(captureConfig);
        MxImage templateMxImage = capture.getData();
        if (templateMxImage != null) {
            showFingerImage(captureType,-1, templateMxImage.data, templateMxImage.width,
                    templateMxImage.height, 0, null);
            MxImage.RawInfo[] singleFingerImages = templateMxImage.singleFingerImages;
            if (singleFingerImages != null && singleFingerImages.length > 0) {
                for (int i = 0; i < templateMxImage.fingerNum; i++) {
                    MxImage.RawInfo rawInfo = singleFingerImages[i];
                    String key = showFingerImage(captureType,i, rawInfo.imageData, rawInfo.width, rawInfo.height,
                            rawInfo.nfiqLevel, rawInfo.position);
                    byte[] template = hostEnroll(rawInfo);
                    if (key != null && template != null) {
                        runOnUiThread(() -> tempMap.put(key, new String(template, StandardCharsets.ISO_8859_1)));
                    }
                }
            }
        }
        return capture;
    }

    private String showFingerImage(EnumPrintType captureType, int index, byte[] data, int width, int height, int nfiqLevel,
                                   EnumFingerPosition fingerPosition) {
        int add = width % 4;
        if (add > 0) {
            add = 4 - add;
            add = add * height;
        }
        byte[] imageData = new byte[width * height + 1078 + add];
        mFingerAlgAPI.convertRawToBMP(data, width, height, imageData);
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        ImageView imageView;
        String key;
        if (fingerPosition == null) {
            switch (captureType) {
                case LEFT_FOUR:
                    switch (index) {
                        case 0:
                            imageView = binding.lFp1;
                            key = "lfp1";
                            break;
                        case 1:
                            imageView = binding.lFp2;
                            key = "lfp2";
                            break;
                        case 2:
                            imageView = binding.lFp3;
                            key = "lfp3";
                            break;
                        case 3:
                            imageView = binding.lFp4;
                            key = "lfp4";
                            break;
                        default:
                            imageView = binding.unsegmented;
                            key = null;
                    }
                    break;
                case RIGHT_FOUR:
                    switch (index) {
                        case 0:
                            imageView = binding.rFp1;
                            key = "rfp1";
                            break;
                        case 1:
                            imageView = binding.rFp2;
                            key = "rfp2";
                            break;
                        case 2:
                            imageView = binding.rFp3;
                            key = "rfp3";
                            break;
                        case 3:
                            imageView = binding.rFp4;
                            key = "rfp4";
                            break;
                        default:
                            imageView = binding.unsegmented;
                            key = null;
                    }
                    break;
                case BOTH_THUMB:
                    switch (index) {
                        case 0:
                            imageView = binding.tFp1;
                            key = "tfp1";
                            break;
                        case 1:
                            imageView = binding.tFp2;
                            key = "tfp2";
                            break;
                        default:
                            imageView = binding.unsegmented;
                            key = null;
                    }
                    break;
                default:
                    imageView = binding.unsegmented;
                    key = null;
            }
        } else {
            switch (fingerPosition) {
                case LEFT_THUMB:
                    imageView = binding.tFp1;
                    key = "tfp1";
                    break;
                case RIGHT_THUMB:
                    imageView = binding.tFp2;
                    key = "tfp2";
                    break;
                case LEFT_INDEX:
                    imageView = binding.lFp1;
                    key = "lfp1";
                    break;
                case RIGHT_INDEX:
                    imageView = binding.rFp1;
                    key = "rfp1";
                    break;
                case LEFT_MIDDLE:
                    imageView = binding.lFp2;
                    key = "lfp2";
                    break;
                case RIGHT_MIDDLE:
                    imageView = binding.rFp2;
                    key = "rfp2";
                    break;
                case LEFT_RING:
                    imageView = binding.lFp3;
                    key = "lfp3";
                    break;
                case RIGHT_RING:
                    imageView = binding.rFp3;
                    key = "rfp3";
                    break;
                case LEFT_LITTLE:
                    imageView = binding.lFp4;
                    key = "lfp4";
                    break;
                case RIGHT_LITTLE:
                    imageView = binding.rFp4;
                    key = "rfp4";
                    break;
                default:
                    imageView = binding.unsegmented;
                    key = null;
                    break;
            }
        }
        runOnUiThread(() -> {
            imageView.setImageBitmap(bitmap);
        });
        return key;
    }

    private void closeDevice() {
        mFingerApi.closeDevice();
        mFingerAlgAPI.free();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDevice();
        TR760Manager.getInstance().fingerprintPower(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFingerApi.stopCapture();
    }

    public void enableButton(boolean enable) {
        List<Button> buttonList = Arrays.asList(binding.testLeftFour, binding.testRightFour,
                binding.testBothThumb, binding.testSingle, binding.testRoll, binding.testThree,
                binding.enrollAll, binding.video);
        for (Button b : buttonList) {
            b.setEnabled(enable);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mFingerApi.stopCapture();
    }

    public void clearImgAndMap() {
        binding.lFp1.setImageResource(R.drawable.ic_fingerprint);
        binding.lFp2.setImageResource(R.drawable.ic_fingerprint);
        binding.lFp3.setImageResource(R.drawable.ic_fingerprint);
        binding.lFp4.setImageResource(R.drawable.ic_fingerprint);
        binding.rFp1.setImageResource(R.drawable.ic_fingerprint);
        binding.rFp2.setImageResource(R.drawable.ic_fingerprint);
        binding.rFp3.setImageResource(R.drawable.ic_fingerprint);
        binding.rFp4.setImageResource(R.drawable.ic_fingerprint);
        binding.tFp1.setImageResource(R.drawable.ic_fingerprint);
        binding.tFp2.setImageResource(R.drawable.ic_fingerprint);
        binding.unsegmented.setImageResource(0);
        tempMap.clear();
    }
}
