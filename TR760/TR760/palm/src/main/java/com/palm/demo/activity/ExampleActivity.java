package com.palm.demo.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hibory.CommonApi;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.api.stream.Device;
import com.api.stream.Frame;
import com.api.stream.Frames;
import com.api.stream.ICapturePalmCallback;
import com.api.stream.IDevice;
import com.api.stream.IOpenCallback;
import com.api.stream.IStream;
import com.api.stream.StreamType;
import com.api.stream.bean.BBox;
import com.api.stream.bean.CaptureFrame;
import com.api.stream.bean.ClientPalmOutput;
import com.api.stream.bean.DeviceInfo;
import com.api.stream.bean.ExtraFrameInfo;
import com.api.stream.bean.ExtractOutput;
import com.api.stream.bean.ImageInstance;
import com.api.stream.enumclass.Hint;
import com.api.stream.manager.DtUsbDevice;
import com.api.stream.manager.DtUsbManager;
import com.api.stream.manager.UsbMapTable;
import com.api.stream.veinshine.IVeinshine;
import com.palm.common.opengl.GLDisplay;
import com.palm.common.opengl.GLFrameSurface;
import com.palm.demo.R;
import com.palm.demo.custom.DtRectRoiView;
import com.palm.demo.util.BitmapUtils;
import com.palm.demo.util.FileUtils;
import com.palm.demo.util.IOUtils;
import com.palm.demo.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ExampleActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    private GLFrameSurface mGLIrView, mGLRgbView;

    GLDisplay rgbDisPlay, irDisPlay;

    private Button mBtnOpen, mBtnEnable, mBtnCapture, mBtnCaptureOnce, mBtnStopCapture;
    private Button mBtnCreatePalmClient, mBtnRegisterToServer, mBtnDeleteId, mBtnQueryFromServer;
    private TextView mTvDeviceInfo;
    private Switch mSwitchStartStream;
    private Spinner mSpinnerStreamMode;
    private ExecutorService deviceThread1 = Executors.newSingleThreadExecutor();
    private ExecutorService workServices = Executors.newSingleThreadExecutor();
    private volatile IDevice mDevice = null;
    private Handler mainHandler;
    private ArrayAdapter<StreamType> mAdapterStreamType;
    private final List<StreamType> mListStreamType = new ArrayList<>();
    private StreamType currentStreamType = StreamType.INVALID_STREAM_TYPE;
    private volatile boolean mIsRunning;
    private volatile boolean mIsOpenCamera;

    private byte[] rgbFrameData1 = null;
    private byte[] irFrameData1 = null;
    private ExtraFrameInfo irFrameExtraInfo = null;
    private ExtraFrameInfo rgbFrameExtraInfo = null;
    private int irFrameW1;
    private int irFrameH1;
    private int rgbFrameW1;
    private int rgbFrameH1;

    public DtRectRoiView mRectRoiRgbView;
    public DtRectRoiView mRectRoiIrView;

    protected Bitmap mRgbBitmap, mIrBitmap, mDepthBitmap;
    private ImageView rgbImage, irImage, depthImage;
    private TextView tvResult;

    private DeviceInfo deviceInfo;

    private String dir = Environment.getExternalStorageDirectory() + File.separator + "HeyStar";
    private ExecutorService callbackServices = Executors.newSingleThreadExecutor();


    @EnableAlgorithmStatus
    private volatile int algoStatus = EnableAlgorithmStatus.DISABLE;


    @IntDef({EnableAlgorithmStatus.DISABLE, EnableAlgorithmStatus.ENABLE, EnableAlgorithmStatus.INITIALIZING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EnableAlgorithmStatus {
        int DISABLE = 0;
        int ENABLE = 1;
        int INITIALIZING = 2;
    }

    private volatile boolean mIsCreatePalmClient;
    private CommonApi mCommonApi = new CommonApi();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        setUsbHost(true);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        checkPermission();
        initView();
        rgbDisPlay = new GLDisplay();
        irDisPlay = new GLDisplay();
        mainHandler = new Handler();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 关闭当前Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * check permission
     */
    private void checkPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, 10);
        }
    }

    private void initView() {
        mGLRgbView = findViewById(R.id.gl_rgb);
        mGLIrView = findViewById(R.id.gl_ir);
        mGLIrView.post(() -> {
            mGLRgbView.setDisplay(mGLRgbView.getWidth(), mGLRgbView.getWidth() * 1024 / 720);
            mGLIrView.setDisplay(mGLIrView.getWidth(), mGLIrView.getWidth() * 1024 / 720);
        });

        mAdapterStreamType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mListStreamType);

        mBtnOpen = findViewById(R.id.btn_open);
        mBtnEnable = findViewById(R.id.btn_enable);
        mBtnCapture = findViewById(R.id.btn_capture);
        mBtnCaptureOnce = findViewById(R.id.btn_capture_once);
        mBtnStopCapture = findViewById(R.id.btn_stop_capture);

        mBtnCreatePalmClient = findViewById(R.id.btn_create_palm_client);
        mBtnQueryFromServer = findViewById(R.id.btn_query);
        mBtnDeleteId = findViewById(R.id.btn_delete);
        mBtnRegisterToServer = findViewById(R.id.btn_register);

        mTvDeviceInfo = findViewById(R.id.tv_device_info);
        mSwitchStartStream = findViewById(R.id.switch_stream);
        mSpinnerStreamMode = findViewById(R.id.spinner_stream_mode);
        mSpinnerStreamMode.setAdapter(mAdapterStreamType);
        rgbImage = findViewById(R.id.rgb_image);
        irImage = findViewById(R.id.ir_image);
        depthImage = findViewById(R.id.depth_image);
        mRectRoiRgbView = findViewById(R.id.rv_rectRgbPicView);
        mRectRoiRgbView.resizeSource(720, 1024);
        mRectRoiIrView = findViewById(R.id.rv_rectIrPicView);
        mRectRoiIrView.resizeSource(720, 1024);

        IOUtils.createFolder(dir);
        IOUtils.createFolder(dir + File.separator + "models");
        copyAssetsFile();

        initListener();
    }

    private void copyAssetsFile() {
        String model = dir + File.separator + "models/palm_models_1.3.5.bin";
        if (FileUtils.isFileExists(model)) {
            return;
        }
        callbackServices.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ResourceUtils.copyFileFromAssets("models", dir + File.separator + "models");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExampleActivity.this, R.string.activity_example_model_copy_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initListener() {
        mBtnOpen.setOnClickListener(view -> openDevice());
        mSwitchStartStream.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!mIsOpenCamera && isChecked) {
                showToast(getString(R.string.activity_example_camera_not_turn_on));
                mSwitchStartStream.setChecked(false);
                return;
            }
            if (mSpinnerStreamMode != null) {
                mSpinnerStreamMode.setEnabled(!isChecked);
            }
            if (!isChecked && mIsRunning) {
                mIsRunning = false;
                mainHandler.postDelayed(this::clearFrame, 200);
                return;
            }
            if (mIsOpenCamera && isChecked) {
                startStream();
            }

        });
        mSpinnerStreamMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStreamType = mListStreamType.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBtnEnable.setOnClickListener(view -> enableDimPalm());
        mBtnCaptureOnce.setOnClickListener(view -> captureOnce());
        mBtnCapture.setOnClickListener(view -> capture());
        mBtnStopCapture.setOnClickListener(view -> stopCapture());
        mTvDeviceInfo.setOnLongClickListener(view -> writeLicense());

        mBtnCreatePalmClient.setOnClickListener(view -> showCreatePalmClientDialog());
        mBtnQueryFromServer.setOnClickListener(view -> showQueryDialog());
        mBtnDeleteId.setOnClickListener(view -> showDeleteDialog());
        mBtnRegisterToServer.setOnClickListener(view -> showRegisterDialog());
    }

    private void showCreatePalmClientDialog() {
        if (!mIsOpenCamera) {
            showToast(getString(R.string.activity_example_camera_not_turn_on));
            return;
        }
        if (algoStatus == EnableAlgorithmStatus.DISABLE) {
            showToast(getString(R.string.activity_example_algo_not_init));
            return;
        }
        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_create_palm_client, null);
        EditText edCompanyId = customView.findViewById(R.id.ed_company_id);
        EditText edSn = customView.findViewById(R.id.ed_sn);
        EditText edIp = customView.findViewById(R.id.ed_ip);
        EditText edPort = customView.findViewById(R.id.ed_port);
        EditText edHostName = customView.findViewById(R.id.ed_host_name);

        {
            String companyId = "100000001";//Company ID
            String sn = deviceInfo.getSerial_num();//SN
            String ip = "3.28.252.50";//IP
            String port = "8888";//PORT
            String hostName = "meta-eye.com";//DOMAIN

            edCompanyId.setText(companyId);
            edSn.setText(sn);
            edIp.setText(ip);
            edPort.setText(port);
            edHostName.setText(hostName);
        }

        if (mDevice != null) {
            edSn.setText(((IVeinshine) mDevice).getDeviceInfo().serial_num);
        }
        builder.setView(customView);
        builder.setPositiveButton(R.string.activity_example_btn_create, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.activity_example_btn_cancel, (dialog, which) -> {
        });

        // 创建并显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    // 处理确定按钮的点击事件
                    String companyId = edCompanyId.getText().toString();
                    String sn = edSn.getText().toString();
                    String ip = edIp.getText().toString();
                    String port = edPort.getText().toString();
                    String hostName = edHostName.getText().toString();
                    if (TextUtils.isEmpty(companyId)
                            || TextUtils.isEmpty(ip)
                            || TextUtils.isEmpty(port)
                            || TextUtils.isEmpty(sn)) {
                        Toast.makeText(this, getString(R.string.activity_example_info_not_be_empty), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (mDevice != null) {
                        mIsCreatePalmClient = ((IVeinshine) mDevice).createPalmClient(companyId, sn, ip, port, hostName);
                        showToast("createPalmClient " + (mIsCreatePalmClient ? "success" : "fail"));
                    }
                    alertDialog.dismiss();
                }));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void showQueryDialog() {
        if (!mIsOpenCamera) {
            showToast(getString(R.string.activity_example_camera_not_turn_on));
            return;
        }
        if (algoStatus == EnableAlgorithmStatus.DISABLE) {
            showToast(getString(R.string.activity_example_algo_not_init));
            return;
        }
        if (!mIsCreatePalmClient) {
            showToast(getString(R.string.activity_example_palm_client_not_create));
            return;
        }
        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_regiter_palm_pic_path, null);
        tvResult = customView.findViewById(R.id.tv_result);
        EditText edRgbPath = customView.findViewById(R.id.ed_rgb_path);
        EditText edIrPath = customView.findViewById(R.id.ed_ir_path);
        builder.setView(customView);
        builder.setPositiveButton(R.string.activity_example_btn_query, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.activity_example_btn_cancel, (dialog, which) -> {
        });

        // 创建并显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    // 处理确定按钮的点击事件
                    tvResult.setText("");
                    String rgbPath = edRgbPath.getText().toString();
                    String irPath = edIrPath.getText().toString();
                    if (mDevice != null) {
                        // 不是单IR模组需要检查RGB图片路径是否正常
                        if (((IVeinshine) mDevice).getDeviceInfo().pid != 0x2009) {
                            if (TextUtils.isEmpty(rgbPath)) {
                                Toast.makeText(this, R.string.activity_example_input_correct_path, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            File rgbFile = new File(rgbPath);
                            if (!rgbFile.exists()) {
                                Toast.makeText(ExampleActivity.this, R.string.activity_example_no_image, Toast.LENGTH_SHORT).show();
                                Log.i("LBC", getString(R.string.activity_example_no_image));
                                return;
                            }
                        }
                    }
                    if (TextUtils.isEmpty(irPath)) {
                        Toast.makeText(this, getString(R.string.activity_example_enter_correct_path), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File irFile = new File(irPath);
                    if (!irFile.exists()) {
                        Toast.makeText(ExampleActivity.this, R.string.activity_example_no_image, Toast.LENGTH_SHORT).show();
                        Log.i("LBC", getString(R.string.activity_example_no_image));
                        return;
                    }

                    doQueryFromServer(rgbPath, irPath);
                }));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void doQueryFromServer(String rgbPath, @NonNull String irPath) {
        // 子线程处理,这里演示用图片提取的特征值再去云服务查询
        new Thread(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;

            Bitmap bitmap;
            ImageInstance rgbImageInstance = null;
            if (!TextUtils.isEmpty(rgbPath)) {
                bitmap = BitmapFactory.decodeFile(rgbPath, options);
                int rgbImageWidth = options.outWidth;
                int rgbImageHeight = options.outHeight;
                //    saveBitmap(bitmap, "/sdcard/palm/java_rgbSrc_bitmap.jpg")
                byte[] rgbData = convertJpegDataToRgb888(bitmap, rgbImageWidth, rgbImageHeight);
                rgbImageInstance =
                        new ImageInstance(rgbImageWidth, rgbImageHeight, rgbData, ImageInstance.ImageFormat.IMG_3C8BIT);
            }
            bitmap = BitmapFactory.decodeFile(irPath, options);
            int irImageWidth = options.outWidth;
            int irImageHeight = options.outHeight;
            //    saveBitmap(bitmap, "/sdcard/palm/java_irSrc_bitmap.jpg")
            byte[] irData = convertJpegDataToGray(bitmap, irImageWidth, irImageHeight);

            ImageInstance irImageInstance =
                    new ImageInstance(irImageWidth, irImageHeight, irData, ImageInstance.ImageFormat.IMG_1C8BIT);

            if (mDevice != null) {
                // 从图像提取特征值
                ExtractOutput extractOutput =
                        ((IVeinshine) mDevice).extractPalmFeaturesFromImg(rgbImageInstance, irImageInstance);
                if (extractOutput == null || extractOutput.result != 0) {
                    runOnUiThread(() -> {
                        if (tvResult != null) {
                            tvResult.setText(getString(R.string.activity_example_extra_feature_failed));
                        }
                    });
                    return;
                }

                ClientPalmOutput clientPalmOutput =
                        ((IVeinshine) mDevice).queryFeatureIdFromServer(
                                rgbImageInstance, extractOutput.rgbFeature,
                                irImageInstance, extractOutput.irFeature);
                runOnUiThread(() -> {
                    if (tvResult != null) {
                        tvResult.setText(clientPalmOutput.toString());
                    }
                });

            }

        }).start();
    }

    private void showDeleteDialog() {
        if (!mIsOpenCamera) {
            showToast(getString(R.string.activity_example_camera_not_turn_on));
            return;
        }
        if (algoStatus == EnableAlgorithmStatus.DISABLE) {
            showToast(getString(R.string.activity_example_algo_not_init));
            return;
        }
        if (!mIsCreatePalmClient) {
            showToast(getString(R.string.activity_example_palm_client_not_create));
            return;
        }
        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_id, null);
        tvResult = customView.findViewById(R.id.tv_result);
        EditText edFeatureId = customView.findViewById(R.id.ed_feature_id);
        builder.setView(customView);
        builder.setPositiveButton(R.string.activity_example_btn_delete, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.activity_example_btn_cancel, (dialog, which) -> {
        });

        // 创建并显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    // 处理确定按钮的点击事件
                    tvResult.setText("");
                    int featureId = Integer.parseInt(edFeatureId.getText().toString());
                    if (featureId <= 0) {
                        Toast.makeText(this, getString(R.string.activity_example_enter_correct_feature_id), Toast.LENGTH_SHORT).show();
                        Log.i("LBC", getString(R.string.activity_example_enter_correct_feature_id));
                        return;
                    }
                    if (mDevice != null) {
                        int ret = ((IVeinshine) mDevice).deleteId(featureId);
                        runOnUiThread(() -> {
                            if (tvResult != null) {
                                tvResult.setText("delete [" + featureId + "] " + (ret == 0 ? "success" : "fail"));
                            }
                        });
                    }

                }));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void showRegisterDialog() {
        if (!mIsOpenCamera) {
            showToast(getString(R.string.activity_example_camera_not_turn_on));
            return;
        }
        if (algoStatus == EnableAlgorithmStatus.DISABLE) {
            showToast(getString(R.string.activity_example_algo_not_init));
            return;
        }
        if (!mIsCreatePalmClient) {
            showToast(getString(R.string.activity_example_palm_client_not_create));
            return;
        }
        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View customView = LayoutInflater.from(this).inflate(R.layout.dialog_regiter_palm_pic_path, null);
        tvResult = customView.findViewById(R.id.tv_result);
        EditText edRgbPath = customView.findViewById(R.id.ed_rgb_path);
        EditText edIrPath = customView.findViewById(R.id.ed_ir_path);
        builder.setView(customView);
        builder.setPositiveButton(R.string.activity_example_btn_register, (dialog, which) -> {
        });
        builder.setNegativeButton(R.string.activity_example_btn_cancel, (dialog, which) -> {
        });

        // 创建并显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog ->
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    // 处理确定按钮的点击事件
                    tvResult.setText("");
                    String rgbPath = edRgbPath.getText().toString();
                    String irPath = edIrPath.getText().toString();
                    if (mDevice != null) {
                        // 不是单IR模组需要检查RGB图片路径是否正常
                        if (((IVeinshine) mDevice).getDeviceInfo().pid != 0x2009) {
                            if (TextUtils.isEmpty(rgbPath)) {
                                Toast.makeText(this, R.string.activity_example_input_correct_path, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            File rgbFile = new File(rgbPath);
                            if (!rgbFile.exists()) {
                                Toast.makeText(ExampleActivity.this, R.string.activity_example_no_image, Toast.LENGTH_SHORT).show();
                                Log.i("LBC", getString(R.string.activity_example_no_image));
                                return;
                            }
                        }
                    }
                    if (TextUtils.isEmpty(irPath)) {
                        Toast.makeText(this, getString(R.string.activity_example_enter_correct_path), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    File irFile = new File(irPath);
                    if (!irFile.exists()) {
                        Toast.makeText(ExampleActivity.this, R.string.activity_example_no_image, Toast.LENGTH_SHORT).show();
                        Log.i("LBC", getString(R.string.activity_example_no_image));
                        return;
                    }
                    doRegisterToServer(rgbPath, irPath);

                }));
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void doRegisterToServer(String rgbPath, @NonNull String irPath) {
        // 子线程处理,这里演示用图片提取的特征值再去注册到云服务
        new Thread(() -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;

            Bitmap bitmap;
            ImageInstance rgbImageInstance = null;
            if (!TextUtils.isEmpty(rgbPath)) {
                bitmap = BitmapFactory.decodeFile(rgbPath, options);
                int rgbImageWidth = options.outWidth;
                int rgbImageHeight = options.outHeight;
                //    saveBitmap(bitmap, "/sdcard/palm/java_rgbSrc_bitmap.jpg")
                byte[] rgbData = convertJpegDataToRgb888(bitmap, rgbImageWidth, rgbImageHeight);
                rgbImageInstance =
                        new ImageInstance(rgbImageWidth, rgbImageHeight, rgbData, ImageInstance.ImageFormat.IMG_3C8BIT);
            }
            bitmap = BitmapFactory.decodeFile(irPath, options);
            int irImageWidth = options.outWidth;
            int irImageHeight = options.outHeight;
            //    saveBitmap(bitmap, "/sdcard/palm/java_irSrc_bitmap.jpg")
            byte[] irData = convertJpegDataToGray(bitmap, irImageWidth, irImageHeight);

            ImageInstance irImageInstance =
                    new ImageInstance(irImageWidth, irImageHeight, irData, ImageInstance.ImageFormat.IMG_1C8BIT);

            if (mDevice != null) {
                // 从图像提取特征值
                ExtractOutput extractOutput =
                        ((IVeinshine) mDevice).extractPalmFeaturesFromImg(rgbImageInstance, irImageInstance);
                if (extractOutput == null || extractOutput.result != 0) {
                    runOnUiThread(() -> {
                        if (tvResult != null) {
                            tvResult.setText(getString(R.string.activity_example_extra_feature_failed));
                        }
                    });
                    return;
                }
                Log.d("LBC", extractOutput.toString());

                ClientPalmOutput clientPalmOutput =
                        ((IVeinshine) mDevice).registerToServer(
                                rgbImageInstance, extractOutput.rgbFeature,
                                irImageInstance, extractOutput.irFeature);
                runOnUiThread(() -> {
                    if (tvResult != null) {
                        tvResult.setText(clientPalmOutput.toString());
                    }
                });

            }

        }).start();
    }

    private byte[] convertJpegDataToRgb888(Bitmap bitmap, int width, int height) {
        int[] pixelsData = new int[width * height];
        bitmap.getPixels(pixelsData, 0, width, 0, 0, width, height);

        // 创建一个字节数组来存储RGB888格式的数据
        byte[] imageData = new byte[pixelsData.length * 3]; // 一个像素占3个字节，分别代表红、绿、蓝通道

        // 将RGB888格式的数据转换为字节数组
        for (int i = 0; i < pixelsData.length; i++) {
            int pixelValue = pixelsData[i];
            imageData[i * 3 + 2] = (byte) ((pixelValue >> 16) & 0xFF); // 红色通道
            imageData[i * 3 + 1] = (byte) ((pixelValue >> 8) & 0xFF); // 绿色通道
            imageData[i * 3] = (byte) (pixelValue & 0xFF); // 蓝色通道
        }
        return imageData;
    }

    private byte[] convertJpegDataToGray(Bitmap bitmap, int width, int height) {
        int[] grayData = new int[width * height];
        bitmap.getPixels(grayData, 0, width, 0, 0, width, height);

        byte[] grayByteArray = new byte[width * height];
        for (int i = 0; i < grayData.length; i++) {
            int grayValue = (int) (0.299 * ((grayData[i] >> 16) & 0xFF)
                    + 0.587 * ((grayData[i] >> 8) & 0xFF)
                    + 0.114 * (grayData[i] & 0xFF));
            grayByteArray[i] = (byte) grayValue;
        }

        return grayByteArray;
    }


    private boolean writeLicense() {
        File file = new File(getExternalCacheDir() + File.separator + "license.bin");
        if (!file.exists()) {
            Toast.makeText(ExampleActivity.this, getString(R.string.activity_example_license_not_exist), Toast.LENGTH_SHORT).show();
            return true;
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            int available = fis.available();
            byte[] buffer = new byte[available];
            fis.read(buffer);
            fis.close();

            // 处理二进制数据
            String content = new String(buffer, StandardCharsets.ISO_8859_1);
            File file1 = new File(getExternalCacheDir() + File.separator + "out1.bin");
            FileOutputStream outputStream = new FileOutputStream(file1);
            outputStream.write(content.getBytes(StandardCharsets.ISO_8859_1));
            outputStream.flush();
            outputStream.close();
            Log.e("LBC", "license:" + content);
            if (mDevice != null && mIsOpenCamera) {
                int ret = ((IVeinshine) mDevice).writeLicense(content);
                Toast.makeText(ExampleActivity.this,
                        "license burn " + (ret == 0 ? "success" : "failure"), Toast.LENGTH_SHORT).show();
                Log.e("LBC", "readLicense:" + ((IVeinshine) mDevice).readLicense());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void showToast(String string) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(ExampleActivity.this, string, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(() -> Toast.makeText(ExampleActivity.this, string, Toast.LENGTH_SHORT).show());
        }
    }

    private void stopCapture() {
        if (algoStatus != EnableAlgorithmStatus.ENABLE) {
            showToast(getString(R.string.activity_example_algo_enable_first));
            return;
        }
        if (mDevice != null) {
            IVeinshine veinshine = (IVeinshine) mDevice;
            int ret = veinshine.stopPalmCapture();
            Log.e("LBC", "stopPalmCapture ret:" + ret);
            if (ret != 0) {
                showToast(getString(R.string.activity_example_stop_capture_failed) + ret);
            } else {
                showToast(getString(R.string.activity_example_stop_capture_success));
            }
            mainHandler.post(() -> {
                rgbImage.setWillNotDraw(true);
                hideRect(mRectRoiRgbView);
                irImage.setWillNotDraw(true);
                hideRect(mRectRoiIrView);
            });
        }
    }

    private void capture() {
        if (algoStatus != EnableAlgorithmStatus.ENABLE) {
            showToast(getString(R.string.activity_example_algo_enable_first));
            return;
        }
        if (mDevice != null) {
            IVeinshine veinshine = (IVeinshine) mDevice;
            int ret = veinshine.capturePalm(mCapturePalmCallback, 15000, false);
            Log.e("LBC", "capturePalm ret:" + ret);
            if (ret != 0) {
                showToast(getString(R.string.activity_example_capture_continuously_failed) + ret);
            }
        }
    }

    private void captureOnce() {
        if (algoStatus != EnableAlgorithmStatus.ENABLE) {
            showToast(getString(R.string.activity_example_algo_enable_first));
            return;
        }
        if (mDevice != null) {
            IVeinshine veinshine = (IVeinshine) mDevice;
            int ret = veinshine.capturePalmOnce(mCapturePalmCallback, 15000, false);
            Log.e("LBC", "capturePalmOnce ret:" + ret);
            if (ret != 0) {
                showToast(getString(R.string.activity_example_capture_one_failed) + ret);
            }
        }
    }

    private String path = "";

    private void enableDimPalm() {
        if (algoStatus == EnableAlgorithmStatus.ENABLE) {
            showToast(getString(R.string.activity_example_algo_already_enable));
            return;
        }
        if (algoStatus == EnableAlgorithmStatus.INITIALIZING) {
            showToast(getString(R.string.activity_example_initializing));
            return;
        }
        showPathDialog();
    }

    private void registerPalm(CaptureFrame frame) {
        //Build an ImageInstance
        ImageInstance instanceRgb = new ImageInstance(frame.rgbCols, frame.rgbRows, frame.rgbData, ImageInstance.ImageFormat.IMG_3C8BIT);
        ImageInstance instanceIr = new ImageInstance(frame.irCols, frame.irRows, frame.irData, ImageInstance.ImageFormat.IMG_1C8BIT);
        Log.e(TAG, "instanceRgb>>width=" + instanceRgb.width + ",height=" + instanceRgb.height + ",imgData=" + instanceRgb.imgData.length + ",format=" + instanceRgb.format);
        Log.e(TAG, "instanceIr>>width=" + instanceIr.width + ",height=" + instanceIr.height + ",imgData=" + instanceIr.imgData.length + ",format=" + instanceIr.format);

        //You can start by querying featureId of the current swiper
        ClientPalmOutput queryPalmOutput = ((IVeinshine) mDevice).queryFeatureIdFromServer(instanceRgb, frame.rgbFeature, instanceIr, frame.irFeature);
        Log.d(TAG, "query code:" + queryPalmOutput.resultCode + ",msg:" + queryPalmOutput.resultMsg + ",featureId:" + queryPalmOutput.featureId);

        ClientPalmOutput registerPalmOutput = ((IVeinshine) mDevice).registerToServer(instanceRgb, frame.rgbFeature, instanceIr, frame.irFeature);
        Log.d(TAG, "registerToServer code:" + registerPalmOutput.resultCode + ",msg:" + registerPalmOutput.resultMsg + ",featureId:" + registerPalmOutput.featureId);

        //30011 Try to register RGB features which has exits
        //30012 Try to register IR features which has exits
        //That means the palm is registered with a cloud service
        if (registerPalmOutput.resultCode == 30011 || registerPalmOutput.resultCode == 30012) {
            //You can look for a matching queryPalmOutput.featureId in the User list
            //If a matching user is found, a message is displayed indicating that the user is registered
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleActivity.this, "Palm print already exists, featureId:" + queryPalmOutput.featureId, Toast.LENGTH_SHORT).show();
                }
            });
            //If you don't find the user information using queryPalmOutput.featureId to register
            //use_featureId_to_register_the_user(queryPalmOutput.featureId)
        }

        if (registerPalmOutput.isOk()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleActivity.this, "Successfully registered, featureId:" + queryPalmOutput.featureId, Toast.LENGTH_SHORT).show();
                }
            });
            //This indicates that you have successfully registered the eigenvalue to the cloud server
            //You need to put the registerPalmOutput.featureId and bind your user information registration
            //use_featureId_to_register_the_user(registerPalmOutput.featureId)
        } else {
            //You will need to refer to the documentation for other error codes
        }
    }

    private void recognizePalm(CaptureFrame frame) {
        //Build an ImageInstance
        ImageInstance instanceRgb = new ImageInstance(frame.rgbCols, frame.rgbRows, frame.rgbData, ImageInstance.ImageFormat.IMG_3C8BIT);
        ImageInstance instanceIr = new ImageInstance(frame.irCols, frame.irRows, frame.irData, ImageInstance.ImageFormat.IMG_1C8BIT);
        Log.e(TAG, "instanceRgb>>width=" + instanceRgb.width + ",height=" + instanceRgb.height + ",imgData=" + instanceRgb.imgData.length + ",format=" + instanceRgb.format);
        Log.e(TAG, "instanceIr>>width=" + instanceIr.width + ",height=" + instanceIr.height + ",imgData=" + instanceIr.imgData.length + ",format=" + instanceIr.format);

        //You can start by querying featureId of the current swiper
        ClientPalmOutput queryPalmOutput = ((IVeinshine) mDevice).queryFeatureIdFromServer(instanceRgb, frame.rgbFeature, instanceIr, frame.irFeature);
        Log.d(TAG, "query code:" + queryPalmOutput.resultCode + ",msg:" + queryPalmOutput.resultMsg + ",featureId:" + queryPalmOutput.featureId);

        //10004 Query Score not pass,
        //Indicates that no matching featureId is found on the server,
        //Indicates that the palm has not been registered on the cloud server
        if (queryPalmOutput.resultCode == 10004) {
            //That means a stranger.
        }

        //isOk() indicates that the ClientPalmOutput object is found normally
        if (queryPalmOutput.isOk()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ExampleActivity.this, "Match successful, featureId:" + queryPalmOutput.featureId, Toast.LENGTH_SHORT).show();
                }
            });
            //If your user list is empty, you don't have any user information
            //Then all the people who brush their hands are unregistered and strangers
//            if (users == null || users.size() == 0) {
//                //That means a stranger.
//            } else {
//                //At this point, you can use featureId to find out the person information
//            }
        } else {
            //You will need to refer to the documentation for other error codes
        }
    }

    private void showPathDialog() {
        final EditText inputEdit = new EditText(ExampleActivity.this);
        // 可以放在sdcard的私有目录下或者其他有权限的目录下,这里演示放在sdcard自定义目录下
        inputEdit.setText("/sdcard/HeyStar/models/");
        AlertDialog.Builder builder = new AlertDialog.Builder(ExampleActivity.this);
        builder.setTitle("Model Path:")
//        .setIcon(android.R.drawable.ic_dialog_info)
                .setView(inputEdit)
                .setNegativeButton(R.string.activity_example_btn_cancel, null);
        builder.setPositiveButton(getString(R.string.activity_example_btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                path = inputEdit.getText().toString();
                if (TextUtils.isEmpty(path)) {
                    showToast(getString(R.string.activity_example_please_input_path));
                    return;
                }
                workServices.execute(() -> {
                    if (algoStatus != EnableAlgorithmStatus.DISABLE) {
                        return;
                    }
                    if (mDevice != null) {
                        IVeinshine veinshine = (IVeinshine) mDevice;
                        algoStatus = EnableAlgorithmStatus.INITIALIZING;
                        int ret = veinshine.enableDimPalm(path);
                        if (ret == 0) {
                            algoStatus = EnableAlgorithmStatus.ENABLE;
                            showToast(getString(R.string.activity_example_algo_enable_success));
                            Log.e(TAG, "algo version:" + veinshine.getAlgorithmVersion());
                        } else {
                            showToast(getString(R.string.activity_example_algo_enable_failed) + ret);
                            algoStatus = EnableAlgorithmStatus.DISABLE;
                        }
                    }
                });
            }
        });
        builder.show();
    }


    protected void showRgbImage(ImageView image, byte[] data, int cols, int rows) {
        buildRgbBitmap(data, cols, rows);
        if (mRgbBitmap != null) {
            image.setWillNotDraw(false);
            image.setImageBitmap(mRgbBitmap);
        }
    }

    protected void showIrImage(ImageView image, byte[] data, int cols, int rows) {
        buildIrBitmap(data, cols, rows);
        if (mIrBitmap != null) {
            image.setWillNotDraw(false);
            image.setImageBitmap(mIrBitmap);
        }
    }

    protected void showRect(DtRectRoiView picView, CaptureFrame frame) {
        BBox box = new BBox(0, 0, 0, 0);
//    Log.i(TAG, "frame.palmRectX = " + frame.palmRectX);
//    Log.i(TAG, "frame.palmRectY = " + frame.palmRectY);
//    Log.i(TAG, "frame.palmRectW = " + frame.palmRectW);
//    Log.i(TAG, "frame.palmRectH = " + frame.palmRectH);
        box.x = frame.palmRectX;
        box.y = frame.palmRectY;
        box.w = frame.palmRectW;
        box.h = frame.palmRectH;
        picView.setRect(box, 0);
    }

    protected void hideRect(DtRectRoiView picView) {
        BBox box = new BBox(0, 0, 0, 0);
        picView.setRect(box, 0);
    }

    protected void buildRgbBitmap(byte[] data, int width, int height) {
        try {
            // RGBA 数组
            byte[] Bits = new byte[data.length / 3 * 4];
            int i;
            for (i = 0; i < data.length / 3; i++) {
                // 原理：4个字节表示一个灰度，则BGR  = 灰度值，最后一个Alpha = 0xff;
                Bits[i * 4] = data[i * 3 + 2];
                Bits[i * 4 + 1] = data[i * 3 + 1];
                Bits[i * 4 + 2] = data[i * 3];
                Bits[i * 4 + 3] = -1; // 0xff
            }
            // Bitmap.Config.ARGB_8888 表示：图像模式为8位
            if (mRgbBitmap == null) {
                Log.i(TAG, "buildRgbBitmap createBitmap");
                mRgbBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mRectRoiRgbView.resizeSource(width, height);
            } else {
                if (mRgbBitmap.getHeight() != height || mRgbBitmap.getWidth() != width) {
                    mRgbBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mRectRoiRgbView.resizeSource(width, height);
                }
            }
            Buffer buffer = ByteBuffer.wrap(Bits);
            buffer.rewind();
            mRgbBitmap.copyPixelsFromBuffer(buffer);

            String rgbPath = "/sdcard/palm/rgb.jpg";
            FileUtils.delete(rgbPath);
            BitmapUtils.save(mRgbBitmap, rgbPath, Bitmap.CompressFormat.JPEG, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void buildIrBitmap(byte[] data, int width, int height) {
        try {
            byte[] Bits = new byte[data.length * 4]; // RGBA 数组
            int i;
            for (i = 0; i < data.length; i++) {
                // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
                Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = data[i];
                Bits[i * 4 + 3] = -1; // 0xff
            }
            // Bitmap.Config.ARGB_8888 表示：图像模式为8位
            if (mIrBitmap == null) {
                mIrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                mRectRoiIrView.resizeSource(width, height);
            } else {
                if (mIrBitmap.getHeight() != height || mIrBitmap.getWidth() != width) {
                    mIrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    mRectRoiIrView.resizeSource(width, height);
                }
            }
            mIrBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

            String irPath = "/sdcard/palm/ir.jpg";
            FileUtils.delete(irPath);
            BitmapUtils.save(mIrBitmap, irPath, Bitmap.CompressFormat.JPEG, 100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected byte[] raw16ToRaw8(byte[] inputData) {
        byte[] outputData = new byte[inputData.length / 2];
        for (int i = 0; i < outputData.length; i++) {
            if (inputData[2 * i + 1] > 0) {
                outputData[i] = (byte) (255);
            } else {
                outputData[i] = inputData[2 * i];
            }
        }
        return outputData;
    }


    protected void convert8bit(byte[] data, int width, int height) {
        try {
            // RGBA 数组
            byte[] Bits = new byte[data.length * 4];
            int i;
            for (i = 0; i < data.length; i++) {
                // 原理：4个字节表示一个灰度，则RGB  = 灰度值，最后一个Alpha = 0xff;
                Bits[i * 4] = Bits[i * 4 + 1] = Bits[i * 4 + 2] = data[i];
                Bits[i * 4 + 3] = -1; // 0xff
            }
            // Bitmap.Config.ARGB_8888 表示：图像模式为8位
            if (mDepthBitmap == null) {
                mDepthBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            }
            mDepthBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ICapturePalmCallback mCapturePalmCallback = new ICapturePalmCallback() {
        @Override
        public void onCaptureFrame(CaptureFrame frame) {
            Log.e(TAG, "onCaptureFrame()");

            if (frame != null) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (frame.rgbData != null) {
                            showRgbImage(rgbImage, frame.rgbData, frame.rgbCols, frame.rgbRows);
                            showRect(mRectRoiRgbView, frame);
                        } else {
                            rgbImage.setWillNotDraw(true);
                            hideRect(mRectRoiRgbView);
                        }
                        if (frame.irData != null) {
                            showIrImage(irImage, frame.irData, frame.irCols, frame.irRows);
                            showRect(mRectRoiIrView, frame);
                        } else {
                            irImage.setWillNotDraw(true);
                            hideRect(mRectRoiIrView);
                        }
                    }
                });
            }

        }

        @Override
        public void onCapturePalmHint(Hint hint, HashMap<Integer, Float> hashMap) {

            if (hashMap != null) {
                if (hashMap.isEmpty()) {
                    Log.e(TAG, "test---- onCapturePalmHint hashMap is empty ");
                } else {
                    for (Map.Entry<Integer, Float> entry : hashMap.entrySet()) {
                        Log.i(TAG, "test onCapturePalmHint score ---- " + entry.getKey() + ",  " + entry.getValue());
                    }
                }
            } else {
                Log.e(TAG, "test---- onCapturePalmHint hashMap is null ");
            }

//      Log.e(TAG, "onCapturePalmHint()");
            if (hint == Hint.NO_PALM_DETECTED) {
                mainHandler.post(() -> {
                    rgbImage.setWillNotDraw(true);
                    hideRect(mRectRoiRgbView);
//          faceHintTextView.setText(hint.value);
                    irImage.setWillNotDraw(true);
                    hideRect(mRectRoiIrView);
                });
            }
            if (hint == Hint.TIMEOUT) {
                runOnUiThread(() -> Toast.makeText(ExampleActivity.this, getString(R.string.activity_example_capture_timeout), Toast.LENGTH_SHORT).show());
            }
        }
    };

    private void openDevice() {
        if (mIsOpenCamera) {
            Toast.makeText(ExampleActivity.this, getString(R.string.activity_example_camera_is_opened), Toast.LENGTH_SHORT).show();
            return;
        }
        Device.create(ExampleActivity.this, new Device.DeviceListener() {
            @Override
            public void onDeviceCreatedSuccess(IDevice device, int deviceIndex, Map<Long, IDevice> runningDevice, UsbMapTable.DeviceType deviceType) {
                Log.i(TAG, "onDeviceCreate, deviceType:" + deviceType + " deviceIndex:" + deviceIndex);
                deviceThread1.execute(() -> open(device, deviceIndex, runningDevice, deviceType));
            }

            @Override
            public void onDeviceCreateFailed(IDevice device) {
                Log.i(TAG, "onDeviceCreateFailed()");
            }

            @Override
            public void onDeviceDestroy(IDevice device) {
                Log.i(TAG, "onDeviceDestroy()");
                if (mDevice != null) {
                    mDevice = null;
                }
                mIsOpenCamera = false;
                algoStatus = EnableAlgorithmStatus.DISABLE;
                mIsCreatePalmClient = false;
                mIsRunning = false;
                rgbFrameData1 = null;
                irFrameData1 = null;
                if (!mListStreamType.isEmpty()) {
                    mListStreamType.clear();
                    mainHandler.post(() -> {
                        mAdapterStreamType.notifyDataSetChanged();
                    });
                    mainHandler.postDelayed(() -> clearFrame(), 200);
                }

            }
        }, new DtUsbManager.DeviceStateListener() {
            @Override
            public void onDevicePermissionGranted(DtUsbDevice dtUsbDevice) {

            }

            @Override
            public void onDevicePermissionDenied(DtUsbDevice dtUsbDevice) {

            }

            @Override
            public void onAttached(DtUsbDevice dtUsbDevice) {
                Log.e(TAG, "onAttached()");

            }

            @Override
            public void onDetached(DtUsbDevice dtUsbDevice) {
                Log.e(TAG, "onDetached()");

                mIsOpenCamera = false;
                algoStatus = EnableAlgorithmStatus.DISABLE;
                mIsCreatePalmClient = false;
                mainHandler.post(() -> {
                    if (mTvDeviceInfo != null) {
                        mTvDeviceInfo.setText("Device Detached!");
                    }
                    if (mSwitchStartStream != null) {
                        mSwitchStartStream.setChecked(false);
                    }
                });

            }
        });
    }

    private void startStream() {
        if (mDevice != null) {
            Thread thread = generateStreamThread(mDevice, 1);
            thread.start();
        }
    }

    private Thread generateStreamThread(final IDevice device, int deviceIndex) {
        Thread streamThread = new Thread(() -> {
            if (currentStreamType != StreamType.INVALID_STREAM_TYPE) {
                IStream stream = device.createStream(currentStreamType);
                if (stream == null) {
                    mainHandler.post(() -> Toast.makeText(ExampleActivity.this, getString(R.string.activity_example_stream_create_failed), Toast.LENGTH_SHORT).show());
                    return;
                }
                Frames frames = stream.allocateFrames();
                int ret = stream.start();
                if (ret == 0) {
                    mIsRunning = true;
                } else {
                    mIsRunning = false;
                    return;
                }
                while (mIsRunning) {
                    int res = stream.getFrames(frames, 2000);
                    if (res != 0) {
                        Log.e(TAG, "get getFrame code: " + Integer.toHexString(res) + "deviceIndex:" + deviceIndex);
                        continue;
                    }

                    Frame frame1 = frames.getFrame(0);
                    Frame frame2 = frames.getFrame(1);

                    //渲染图像帧
                    onDrawFrame(frame1, frame2, deviceIndex);

                }
                if (mDevice != null) {
                    stream.stop();
                    device.destroyStream(stream);
                }
                Log.e("LBC", "getFrame thread exit");
            }
        });
        return streamThread;
    }

    private void onDrawFrame(Frame frame1, Frame frame2, int deviceIndex) {
        if (frame1 != null) {
            switch (frame1.getFrameType()) {
                case RGB_FRAME:
                    if (deviceIndex == 1) {
                        rgbFrameW1 = frame1.getWidth();
                        rgbFrameH1 = frame1.getHeight();
                        rgbFrameData1 = frame1.getRawData();
                        rgbFrameExtraInfo = frame1.getExtraInfo();
                    }
                    break;
                case IR_FRAME:
                    if (deviceIndex == 1) {
                        irFrameW1 = frame1.getWidth();
                        irFrameH1 = frame1.getHeight();
                        irFrameData1 = frame1.getRawData();
                        irFrameExtraInfo = frame1.getExtraInfo();
                    }
                    break;
                default:
            }
        }
        if (frame2 != null) {
            switch (frame2.getFrameType()) {
                case RGB_FRAME:
                    if (deviceIndex == 1) {
                        rgbFrameW1 = frame2.getWidth();
                        rgbFrameH1 = frame2.getHeight();
                        rgbFrameData1 = frame2.getRawData();
                        rgbFrameExtraInfo = frame2.getExtraInfo();
                    }
                    break;
                case IR_FRAME:
                    if (deviceIndex == 1) {
                        irFrameW1 = frame2.getWidth();
                        irFrameH1 = frame2.getHeight();
                        irFrameData1 = frame2.getRawData();
                        irFrameExtraInfo = frame2.getExtraInfo();
                    }
                    break;
                default:
            }
        }
        if (deviceIndex == 1) {
            if (null != irFrameData1 && null != irDisPlay && null != mGLIrView) {
                irDisPlay.render(mGLIrView,
                        0,
                        false,
                        irFrameData1,
                        irFrameW1,
                        irFrameH1,
                        2,
                        new int[]{irFrameExtraInfo.palmRoi[0],
                                irFrameExtraInfo.palmRoi[1],
                                irFrameExtraInfo.palmRoi[2],
                                irFrameExtraInfo.palmRoi[3]});
            }
            if (null != rgbFrameData1 && null != rgbDisPlay && null != mGLRgbView) {
                rgbDisPlay.render(mGLRgbView,
                        0,
                        false,
                        rgbFrameData1,
                        rgbFrameW1,
                        rgbFrameH1,
                        1,
                        new int[]{rgbFrameExtraInfo.palmRoi[0],
                                rgbFrameExtraInfo.palmRoi[1],
                                rgbFrameExtraInfo.palmRoi[2],
                                rgbFrameExtraInfo.palmRoi[3]});
            }
        }

    }

    private void open(IDevice device, int deviceIndex, Map<Long, IDevice> mRunningDevice, UsbMapTable.DeviceType deviceType) {
        device.open(new IOpenCallback() {
            @Override
            public void onDownloadPrepare() {
            }

            @Override
            public void onDownloadProgress(int progress) {

            }

            @Override
            public void onDownloadSuccess() {
            }

            @Override
            public void onOpenSuccess() {
                mIsOpenCamera = true;
                deviceInfo = ((IVeinshine) device).getDeviceInfo();
                Log.d("LBC", "firmware version:" + deviceInfo.firmware_version);
                mainHandler.post(() -> {
                    if (mTvDeviceInfo != null) {
                        mTvDeviceInfo.setText("DeviceName:" + deviceInfo.device_name);
                    }
                });

                if (deviceIndex == 1) {
                    Log.e("LBC", "mDevice1");
                    mDevice = device;

                    List<StreamType> deviceSupportStreamTypeList = device.getDeviceSupportStreamType();
                    if (!deviceSupportStreamTypeList.isEmpty()) {
                        mListStreamType.clear();
                        mListStreamType.addAll(deviceSupportStreamTypeList);
                        mainHandler.post(() -> mAdapterStreamType.notifyDataSetChanged());
                    }
                }
            }

            @Override
            public void onOpenFail(int errorCode) {
                mainHandler.post(() -> Toast.makeText(ExampleActivity.this, "open device error:" + errorCode, Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void clearFrame() {
        Log.e(TAG, "-------cleanFrame()------");
        if (null != rgbDisPlay && null != mGLRgbView) {
            rgbDisPlay.render(mGLRgbView, 0, false, new byte[rgbFrameW1 * rgbFrameH1 * 3],
                    rgbFrameW1, rgbFrameH1, 1);
        }
        if (null != irDisPlay && null != mGLIrView) {
            irDisPlay.render(mGLIrView, 0, false, new byte[irFrameW1 * irFrameH1],
                    irFrameW1, irFrameH1, 2);
        }
        rgbFrameData1 = null;
        irFrameData1 = null;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice != null) {
            mDevice.close();
        }
        mGLRgbView.onPause();
        mGLIrView.onPause();
        rgbDisPlay.release();
        irDisPlay.release();
        rgbDisPlay = null;
        irDisPlay = null;
        setUsbHost(false);
    }

    private void setUsbHost(boolean trigger) {
        this.mCommonApi.setUsbHost(trigger ? 1 : 0);
    }

}
