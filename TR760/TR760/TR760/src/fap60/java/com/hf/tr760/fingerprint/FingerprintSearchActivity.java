package com.hf.tr760.fingerprint;

import static com.fingers.fap60.bean.EnumPrintType.SINGLE;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.appcompat.app.AppCompatActivity;

import com.fingerprint.algorithm.FingerAlgAPI;
import com.fingers.fap60.FingerApi;
import com.fingers.fap60.bean.CaptureConfig;
import com.fingers.fap60.bean.EnumDeviceState;
import com.fingers.fap60.bean.common.MxError;
import com.fingers.fap60.bean.common.MxImage;
import com.fingers.fap60.bean.common.MxResult;
import com.fingers.fap60.callback.FpPreviewCallBack;
import com.fingers.fap60.driver.fap6002.FapInitParam;
import com.hf.cache.EasyCache;
import com.hf.tr760.databinding.ActivityFingerPrintSearchBinding;
import com.hf.tr760.utils.TR760Manager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class FingerprintSearchActivity extends AppCompatActivity {

    private ActivityFingerPrintSearchBinding binding;
    private final FingerApi mFingerApi = FingerApi.getInstance();
    private final FingerAlgAPI mFingerAlgAPI = new FingerAlgAPI();

    private final AtomicBoolean isOpen = new AtomicBoolean(false);

    private final AtomicBoolean isResume = new AtomicBoolean(false);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private HashMap<String, String> fpMap;

    private final boolean isSearch = true;

    private byte[] allTemplates;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TR760Manager.getInstance().fingerprintPower(true);
        binding = ActivityFingerPrintSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EasyCache easyCache = new EasyCache(this, "fp");
        fpMap = easyCache.getAll();
        if (isSearch) {
            allTemplates = loadAllTemplate();
        }
        mFingerApi.init(getApplicationContext(), FapInitParam.LOG_OPEN, state -> {
            if (state == EnumDeviceState.HAVE_DEVICE || state == EnumDeviceState.DEVICE_ATTACHED) {
                openDevice();
            } else if (state == EnumDeviceState.DEVICE_DETACHED) {
//                Toast.makeText(FingerprintSearchActivity.this, "DEVICE DETACHED", Toast.LENGTH_SHORT).show();
                closeDevice();
            }else {
//                Toast.makeText(FingerprintSearchActivity.this, "NO_DEVICE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDevice() {
        executor.execute(() -> {
            int result = mFingerApi.openDevice();
            if (result == 0) {
                isOpen.set(true);
                if (isResume.get()) {
                    startCapture();
                }
            }else {
                isOpen.set(false);
                String s = "DEVICE OPEN FAILED";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(FingerprintSearchActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        isResume.set(false);
        if (isOpen.get()) {
            mFingerApi.stopCapture();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (isResume.compareAndSet(false, true)) {
            if (isOpen.get()) {
                executor.execute(this::startCapture);
            }
        }
        super.onResume();
    }

    @WorkerThread
    private void startCapture() {
        while (isResume.get()) {
            CaptureConfig.Builder captureConfigBuilder = new CaptureConfig.Builder()
                    .setEnumPrintType(SINGLE)
                    .setAreaScore(CaptureConfig.DEFAULT_AREA_SCORE)
                    .setPreviewCallBack(previewCallBack);
            CaptureConfig captureConfig = captureConfigBuilder.build();
            MxResult<MxImage> capture = mFingerApi.getImage(captureConfig);
            MxImage templateMxImage = capture.getData();
            if (!capture.isSuccess()) {
                runOnUiThread(() -> binding.logView.error("CAPTURE FAILED"));
                continue;
            }
            byte[] tempFeature = new byte[Fingerprint442Activity.TEMPLATE_LENGTH];
            MxImage.RawInfo rawInfo = templateMxImage.singleFingerImages[0];
            int result = createTemplate(rawInfo.imageData, rawInfo.width, rawInfo.height, tempFeature, null);
            if (result < 0) {
                runOnUiThread(() -> binding.logView.error("CAPTURE FAILED"));
                continue;
            }
            String userId;
            if (isSearch) {
                int count = userNameList.size();
                int index = searchTemplates(tempFeature, count, allTemplates);
                if (index >= 0 && index < count) {
                    userId = userNameList.get(index);
                } else {
                    userId = null;
                }
            } else {
                for (String n : fpMap.keySet()) {
                    String v = fpMap.get(n);
                    if (v == null) {
                        continue;
                    }
                    int score = compareTemplates(v.getBytes(StandardCharsets.ISO_8859_1), tempFeature);
                    if (score > 80) {
                        userId = n;
                        break;
                    }
                }
            }
            if (userId != null) {
                runOnUiThread(() -> binding.logView.notification("MATCHED: your finger is " + userId));
                continue;
            }
            runOnUiThread(() -> binding.logView.append("NO MATCHED USER"));
        }
    }


    private final List<String> userNameList = new ArrayList<>();

    public byte[] loadAllTemplate() {
        userNameList.clear();
        if (fpMap == null || fpMap.isEmpty()) return new byte[0];
        int templateSize = 0;
        for (String key : fpMap.keySet()) {
            if (key == null) {
                continue;
            }
            String value = fpMap.get(key);
            if (value == null) {
                continue;
            }
            templateSize++;
        }
        byte[] allTemplate = new byte[templateSize * Fingerprint442Activity.TEMPLATE_LENGTH];
        int index = 0;

        for (String key : fpMap.keySet()) {
            if (key == null) {
                continue;
            }
            String value = fpMap.get(key);
            if (value == null) {
                continue;
            }
            byte[] fp = value.getBytes(StandardCharsets.ISO_8859_1);
            int copyLen = Math.min(fp.length, Fingerprint442Activity.TEMPLATE_LENGTH);
            System.arraycopy(fp, 0, allTemplate, index * Fingerprint442Activity.TEMPLATE_LENGTH, copyLen);
            userNameList.add(key);
            index++;
        }
        return Arrays.copyOfRange(allTemplate, 0, index * Fingerprint442Activity.TEMPLATE_LENGTH);
    }


    private int createTemplate(byte[] data, int width, int height, byte[] newTemplate, int[] length) {
        return mFingerAlgAPI.createTemplateISO(data, width, height, Fingerprint442Activity.MIN_MINUTIAE_COUNT, newTemplate, true, length);
    }

    private int compareTemplates(byte[] data, byte[] dataAnother) {
        return mFingerAlgAPI.compareTemplatesISO(data, dataAnother);
    }

    private int searchTemplates(byte[] templateToSearch, int numberOfDbTemplates, byte[] arrayOfDbTemplates) {
        return mFingerAlgAPI.searchTemplatesISO(templateToSearch, numberOfDbTemplates, arrayOfDbTemplates);
    }

    private final FpPreviewCallBack previewCallBack = new FpPreviewCallBack() {
        @Override
        public void onFrame(MxImage mxImage, MxError mxError) {
            if (mxImage != null) {
                byte[] imageDate = new byte[mxImage.width * mxImage.height + 1078];
                mFingerAlgAPI.convertRawToBMP(mxImage.data, mxImage.width, mxImage.height, imageDate);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageDate, 0, imageDate.length);
                runOnUiThread(() -> {
                    binding.fingerprintImg.setImageBitmap(bitmap);
                });
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDevice();
        TR760Manager.getInstance().fingerprintPower(false);
    }

    private void closeDevice() {
        mFingerApi.closeDevice();
        mFingerAlgAPI.free();
    }


}
