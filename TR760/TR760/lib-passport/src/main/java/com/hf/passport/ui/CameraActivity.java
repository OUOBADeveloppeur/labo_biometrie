package com.hf.passport.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.hf.passport.R;
import com.hf.passport.camera.CameraSource;
import com.hf.passport.camera.CameraSourcePreview;
import com.hf.passport.model.BacKeyParts;
import com.hf.passport.model.UserChipInfo;
import com.hf.passport.other.DocType;
import com.hf.passport.other.GraphicOverlay;
import com.hf.passport.text.TextRecognitionProcessor;
import com.hf.passport.util.ThreadManager;
import com.innovatrics.mrz.MrzRecord;

import org.jmrtd.lds.icao.MRZInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraActivity extends AppCompatActivity implements TextRecognitionProcessor.ResultListener {
    private static final String TAG = "CameraActivity";

    // status
    private TextView tv_pp_tip;
    private Button btn_pp_return;
    // user info
    private LinearLayout ll_pp_user_info;
    private ImageView iv_pp_photo;
    private TextView tv_pp_info_type;
    private TextView tv_pp_info_country_code;
    private TextView tv_pp_info_no;
    private TextView tv_pp_info_surname;
    private TextView tv_pp_info_given_name;
    private TextView tv_pp_info_date_of_birth;
    private TextView tv_pp_info_gender;
    private TextView tv_pp_info_nationality;
    private TextView tv_pp_info_date_of_expiry;
    private TextView tv_pp_info_optional_info;
    private TextView tv_pp_info_auth_result;
    private TextView tv_pp_info_mrz;

    private BacKeyParts bacKeyParts;
    private byte[] dg14Encoded;
    private boolean chipAuthSucceeded = false;
    private boolean passiveAuthSuccess = false;

    private ThreadManager threadManager;

    private UserChipInfo currentChipInfo;
    private Bitmap bitmapFromGallery = null;
    private Bitmap croppedBmp = null;

    private CameraSource cameraSource;
    private CameraSourcePreview sourcePreview;
    private GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pp_camera);

        initCamera();
        initView();
        initDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        scanCountDownTimer.cancel();
    }

    private void initCamera() {
        sourcePreview = findViewById(R.id.camera_source_preview);
        graphicOverlay = findViewById(R.id.graphics_overlay);

        createCameraSource();
        startCameraSource();
    }

    private void initView() {
        // status
        tv_pp_tip = findViewById(R.id.tv_camera_tip);
        btn_pp_return = findViewById(R.id.btn_camera_return);
        btn_pp_return.setVisibility(View.GONE);
        btn_pp_return.setOnClickListener(view -> {
            tv_pp_tip.setVisibility(View.VISIBLE);
            btn_pp_return.setVisibility(View.GONE);
            ll_pp_user_info.setVisibility(View.GONE);
            sourcePreview.setVisibility(View.VISIBLE);
            cameraSource.startProcess();
            bacKeyParts = null;
            clearUserInfo();
            setStep(1, getString(R.string.lib_pp_tip_step_6));
//            startScanCountDownTimer();
        });

        // user info
        ll_pp_user_info = findViewById(R.id.ll_camera_user_info);
        ll_pp_user_info.setVisibility(View.GONE);
        sourcePreview.setVisibility(View.VISIBLE);
        cameraSource.startProcess();
        iv_pp_photo = findViewById(R.id.iv_camera_photo);
        tv_pp_info_type = findViewById(R.id.tv_camera_info_type);
        tv_pp_info_country_code = findViewById(R.id.tv_camera_info_country_code);
        tv_pp_info_no = findViewById(R.id.tv_camera_info_no);
        tv_pp_info_surname = findViewById(R.id.tv_camera_info_surname);
        tv_pp_info_given_name = findViewById(R.id.tv_camera_info_given_name);
        tv_pp_info_date_of_birth = findViewById(R.id.tv_camera_info_date_of_birth);
        tv_pp_info_gender = findViewById(R.id.tv_camera_info_gender);
        tv_pp_info_nationality = findViewById(R.id.tv_camera_info_nationality);
        tv_pp_info_date_of_expiry = findViewById(R.id.tv_camera_info_date_of_expiry);
        tv_pp_info_optional_info = findViewById(R.id.tv_camera_info_optional_info);
        tv_pp_info_auth_result = findViewById(R.id.tv_camera_info_auth_result);
        tv_pp_info_mrz = findViewById(R.id.tv_camera_info_mrz);

        setStep(1, getString(R.string.lib_pp_tip_step_6));

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bg_passport);
        if (bitmapFromGallery == null) {
            bitmapFromGallery = bitmap;
        }
    }

    private void initDate() {
        threadManager = new ThreadManager();
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        sourcePreview.stop();
    }

    private void createCameraSource() {
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
        }

        cameraSource.setMachineLearningFrameProcessor(new TextRecognitionProcessor(DocType.OTHER, this));
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (sourcePreview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                sourcePreview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }


    private BacKeyParts parseMRZInfo(String mrzString) {
        try {
            MRZInfo mrzInfo = new MRZInfo(mrzString);
            return new BacKeyParts(mrzInfo.getDocumentNumber(), mrzInfo.getDateOfBirth(), mrzInfo.getDateOfExpiry(), mrzString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setStep(int step, String tip) {
        runOnUiThread(() -> {
            tv_pp_tip.setText(tip);
        });
    }

    @SuppressLint("SetTextI18n")
    private void setUserInfo(UserChipInfo chipInfo) {
        if (null == chipInfo) {
            return;
        }
        cameraSource.stopProcess();

        runOnUiThread(() -> {
            if (null != chipInfo.getPhoto()) {
                iv_pp_photo.setImageBitmap(chipInfo.getPhoto());
            }
            if (null != bacKeyParts) {
                if (chipInfo.getDocumentType() == 1) {
                    // 30 characters wide
                    tv_pp_info_mrz.setText(splitString(bacKeyParts.getMrzCode(), 30));
                } else if (chipInfo.getDocumentType() == 2) {
                    // 36 characters wide
                    tv_pp_info_mrz.setText(splitString(bacKeyParts.getMrzCode(), 36));
                } else if (chipInfo.getDocumentType() == 3) {
                    // 44 characters wide
                    tv_pp_info_mrz.setText(splitString(bacKeyParts.getMrzCode(), 44));
                } else {
                    tv_pp_info_mrz.setText(bacKeyParts.getMrzCode());
                }
            }
            tv_pp_info_type.setText(chipInfo.getDocumentCode());
            tv_pp_info_country_code.setText(chipInfo.getNationality());
            tv_pp_info_no.setText(chipInfo.getDocNum());
            tv_pp_info_surname.setText(chipInfo.getPrimaryId());
            tv_pp_info_given_name.setText(chipInfo.getSecondId());
            Log.e("IDAnalyser", "chipInfo.getSecondId()=" + chipInfo.getSecondId());
            tv_pp_info_date_of_birth.setText(chipInfo.getBirthDate());
            tv_pp_info_gender.setText(chipInfo.getGender());
            tv_pp_info_nationality.setText(chipInfo.getNationality());
            tv_pp_info_date_of_expiry.setText(chipInfo.getExpiryDate());
//            if (!chipInfo.getOptionalData1().isEmpty() || !chipInfo.getOptionalData2().isEmpty()) {
//                tv_pp_info_optional_info.setText(chipInfo.getOptionalData1() + " " + chipInfo.getOptionalData2());
//            } else {
            tv_pp_info_optional_info.setText("");
//            }

            if (null != chipInfo.getChipAuth()) {
                String result = getString(chipInfo.getChipAuth() ? R.string.lib_pp_auth_result_success : R.string.lib_pp_auth_result_failed);
                tv_pp_info_auth_result.setText(result);
            } else if (null != chipInfo.getPassiveAuth()) {
                String result = getString(chipInfo.getPassiveAuth() ? R.string.lib_pp_auth_result_success : R.string.lib_pp_auth_result_failed);
                tv_pp_info_auth_result.setText(result);
            } else {
                tv_pp_info_auth_result.setText("");
            }
            iv_pp_photo.setVisibility(View.VISIBLE);
            ll_pp_user_info.setVisibility(View.VISIBLE);
            sourcePreview.setVisibility(View.GONE);
        });
    }

    /**
     * 将字符串按指定长度增加回车换行符
     */
    private String splitString(String str, int length) {
        StringBuilder sb = new StringBuilder();
        int size = str.length() / length;
        if (str.length() % length != 0) {
            size += 1;
        }
        for (int i = 0; i < size; i++) {
            int start = i * length;
            int end = (i + 1) * length;
            if (end > str.length()) {
                end = str.length();
            }
            sb.append(str.substring(start, end));
            if (i != size - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private void clearUserInfo() {
        runOnUiThread(() -> {
            iv_pp_photo.setImageBitmap(null);
            tv_pp_info_mrz.setText("");
            tv_pp_info_type.setText("");
            tv_pp_info_country_code.setText("");
            tv_pp_info_no.setText("");
            tv_pp_info_surname.setText("");
            tv_pp_info_given_name.setText("");
            tv_pp_info_date_of_birth.setText("");
            tv_pp_info_gender.setText("");
            tv_pp_info_nationality.setText("");
            tv_pp_info_date_of_expiry.setText("");
            tv_pp_info_optional_info.setText("");
            tv_pp_info_auth_result.setText("");

            iv_pp_photo.setVisibility(View.INVISIBLE);
            ll_pp_user_info.setVisibility(View.GONE);
            sourcePreview.setVisibility(View.VISIBLE);
            cameraSource.startProcess();
        });
    }

    private UserChipInfo transform(MRZInfo mrzInfo) {
        if (null == mrzInfo) {
            return null;
        }

        UserChipInfo userChipInfo = new UserChipInfo();
        userChipInfo.setDocumentType(mrzInfo.getDocumentType());
        userChipInfo.setDocumentCode(mrzInfo.getDocumentCode());
        userChipInfo.setPrimaryId(mrzInfo.getPrimaryIdentifier().replace("<", " "));
        userChipInfo.setSecondId(mrzInfo.getSecondaryIdentifier().replace("<", " "));
        userChipInfo.setGender(mrzInfo.getGender().toString());
        userChipInfo.setIssuingState(mrzInfo.getIssuingState());
        userChipInfo.setNationality(mrzInfo.getNationality());
        userChipInfo.setDocNum(mrzInfo.getDocumentNumber());
        userChipInfo.setBirthDate(mrzInfo.getDateOfBirth());
        userChipInfo.setExpiryDate(mrzInfo.getDateOfExpiry());
        userChipInfo.setOptionalData1(mrzInfo.getOptionalData1());
        userChipInfo.setOptionalData2(mrzInfo.getOptionalData2());
        userChipInfo.setPassiveAuth(null);
        userChipInfo.setChipAuth(null);
        userChipInfo.setPhoto(null);
        return userChipInfo;
    }

    private UserChipInfo transform(MrzRecord mrzRecord) {
        if (null == mrzRecord) {
            return null;
        }

        UserChipInfo userChipInfo = new UserChipInfo();
        if (mrzRecord.format.columns == 30) {
            userChipInfo.setDocumentType(1);
        } else if (mrzRecord.format.columns == 36) {
            userChipInfo.setDocumentType(2);
        } else if (mrzRecord.format.columns == 44) {
            userChipInfo.setDocumentType(3);
        }
        userChipInfo.setDocumentCode(mrzRecord.format.toString());
        userChipInfo.setPrimaryId(mrzRecord.surname);
        userChipInfo.setSecondId(mrzRecord.givenNames);
        userChipInfo.setGender(mrzRecord.sex.toString());
        userChipInfo.setIssuingState(mrzRecord.issuingCountry);
        userChipInfo.setNationality(mrzRecord.nationality);
        userChipInfo.setDocNum(mrzRecord.documentNumber);
        userChipInfo.setBirthDate(mrzRecord.dateOfBirth.toString());
        userChipInfo.setExpiryDate(mrzRecord.expirationDate.toString());
        userChipInfo.setOptionalData1("");
        userChipInfo.setOptionalData2("");
        userChipInfo.setPassiveAuth(null);
        userChipInfo.setChipAuth(null);
        userChipInfo.setPhoto(null);
        return userChipInfo;
    }

    /**
     * CountDownTimer 实现倒计时
     */
    private final CountDownTimer scanCountDownTimer = new CountDownTimer(300 * 1000, 500) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
        }
    };

    private void showCropPictureResult(Bitmap bitmap) {
        if (null == currentChipInfo) {
            return;
        }

        UserChipInfo chipInfo = currentChipInfo;
        chipInfo.setPhoto(bitmap);
        chipInfo.setChipAuth(null);
        chipInfo.setPassiveAuth(null);
        setUserInfo(chipInfo);
        setStep(5, getString(R.string.lib_pp_tip_step_5));
        boolean isAuthPassed = chipAuthSucceeded || passiveAuthSuccess;
        runOnUiThread(() -> {
            btn_pp_return.setVisibility(View.VISIBLE);
            tv_pp_tip.setVisibility(View.GONE);
        });
    }

    private void scanPhoto(Bitmap photo) {
        threadManager.execute(() -> {
            Bitmap bitmap = cropPhoto(photo);
            showCropPictureResult(bitmap);
        });
    }

    private Bitmap cropPhoto(Bitmap bmpPassport) {
        bitmapFromGallery = bmpPassport;
        final Bitmap tempBitmap = Bitmap.createBitmap(bitmapFromGallery.getWidth(), bitmapFromGallery.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(tempBitmap);
        canvas.drawBitmap(bitmapFromGallery, 0, 0, null);

        FaceDetector faceDetector = new FaceDetector.Builder(this.getApplicationContext()).setTrackingEnabled(false).setMode(FaceDetector.FAST_MODE).build();
        if (!faceDetector.isOperational()) {
            runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Face Detector Not Working", Toast.LENGTH_SHORT).show());
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(tempBitmap).build();
        SparseArray<Face> sparseArray = faceDetector.detect(frame);
        if (sparseArray.size() <= 0) {
            runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Face Detector Not find face", Toast.LENGTH_SHORT).show());
            return null;
        }
        int a = 1, b = 1;
        double sum = a + b;
        float centre_x, centre_y;
        int start_x, start_y, end_x, end_y;
        sortSparseArrayByAge(sparseArray);
        for (int i = 0; i < sparseArray.size(); i++) {
            Face face = sparseArray.valueAt(i);
            float x = face.getPosition().x;
            float y = face.getPosition().y;
            float w = face.getWidth();
            float h = face.getHeight();
            centre_x = x + (w / 2);
            centre_y = y + (h / 2);
            start_x = (int) (centre_x - 1.5 * (w / 2));
            start_y = (int) (centre_y - (21 * a) / (10 * (sum)) * (w));
            end_x = (int) (centre_x + 1.5 * (w / 2));
            end_y = (int) (centre_y + (21 * b) / (10 * (sum)) * (w));

//                    RectF rectF = new RectF(start_x, start_y, end_x, end_y);
//                    canvas.drawRoundRect(rectF, 2, 2, boxPaint);


            Rect rect = new Rect(start_x, start_y, end_x, end_y);
            assert (rect.left < rect.right && rect.top < rect.bottom);
            croppedBmp = Bitmap.createBitmap(rect.right - rect.left, rect.bottom - rect.top, Bitmap.Config.ARGB_8888);
            new Canvas(croppedBmp).drawBitmap(tempBitmap, -rect.left, -rect.top, null);
        }
        return croppedBmp;
    }

    @Override
    public void onSuccess(MRZInfo mrzInfo, Bitmap bitmap) {
        cameraSource.stopProcess();
        currentChipInfo = transform(mrzInfo);
        UserChipInfo chipInfo = currentChipInfo;
        chipInfo.setPhoto(bitmap);
        chipInfo.setChipAuth(null);
        chipInfo.setPassiveAuth(null);
        setUserInfo(chipInfo);
    }

    @Override
    public void onError(Exception exp) {

    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        scanPhoto(bitmap);
    }

    public static void sortSparseArrayByAge(SparseArray<Face> sparseArray) {
        // 将SparseArray转换为列表
        List<Face> faces = new ArrayList<>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++) {
            faces.add(sparseArray.valueAt(i));
        }

        // 对Face对象列表进行排序，假设我们根据年龄进行排序
        Collections.sort(faces, new Comparator<Face>() {
            @Override
            public int compare(Face face1, Face face2) {
                return Integer.compare((int) face1.getWidth(), (int) face2.getWidth());
            }
        });

        // 清空原始SparseArray
        sparseArray.clear();

        // 将排序后的Face对象重新放入SparseArray
        for (Face face : faces) {
            sparseArray.put(face.getId(), face);
        }
    }

}
