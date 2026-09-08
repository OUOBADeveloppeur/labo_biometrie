package com.hf.tr760.fingerprint;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import androidx.annotation.NonNull;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.eyecool.fragment.IrisFragment;
import com.eyecool.iris.api.IrisSDK;
import com.fingerprint.algorithm.FingerAlgAPI;
import com.fingers.fap60.FingerApi;
import com.fingers.fap60.bean.CaptureConfig;
import com.fingers.fap60.bean.EnumDeviceState;
import com.fingers.fap60.bean.EnumPrintType;
import com.fingers.fap60.bean.common.MxImage;
import com.fingers.fap60.bean.common.MxResult;
import com.fingers.fap60.driver.fap6002.FapInitParam;
import com.hf.cache.EasyCache;
import com.hf.tr760.R;
import com.hf.tr760.facex.App;
import com.hf.tr760.facex.facepass.InitFacePassHandler;
import com.hf.tr760.facex.facepass.camera.CameraActivity;
import com.hf.tr760.facex.facepass.db.User;
import com.hf.tr760.utils.ProgressDialogUtils;
import com.hf.tr760.utils.TR760Manager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;
import mcv.facepass.types.FacePassAddFaceResult;
import static com.hf.tr760.facex.facepass.InitFacePassHandler.group_name;

public class MultimodalEnrollmentActivity extends AppCompatActivity {

    private String mUserName = "";
    private String mPrenom = "";
    private String mSex = "";
    private String mDateNaissance = "";
    private String mLocalite = "";
    private final HashMap<String, String> mFingerMap = new HashMap<>();
    private byte[] mIrisLeft = null;
    private byte[] mIrisRight = null;
    private String mFaceToken = "";
    private String mPhotoFaceBase64 = "";
    private final HashMap<String, String> mFingerImageMap = new HashMap<>();

    // UI
    private LinearLayout step1, step2, step3, step4;
    private TextView stepTitle, tvFaceResult, irisHintTv, tvFpHint, tvFingerScore;
    private EditText etUserName, etPrenom, etSex, etAge, etVille;
    private ImageView fpPreview;
    private FrameLayout irisFragmentContainer;

    // Fingerprint
    private final FingerApi mFingerApi = FingerApi.getInstance();
    private final FingerAlgAPI mFingerAlgAPI = new FingerAlgAPI();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Iris
    private IrisSDK mIrisSDK;
    private IrisFragment mIrisFragment;

    private ProgressDialog mProgressDialog;

    // Face Launcher
    private final ActivityResultLauncher<Intent> faceResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == 200 && result.getData() != null) {
                    String path = result.getData().getStringExtra("data");
                    processFaceImage(path);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimodal_enrollment);

        mProgressDialog = new ProgressDialog(this);

        stepTitle = findViewById(R.id.stepTitle);
        step1 = findViewById(R.id.step1_user_info);
        step2 = findViewById(R.id.step2_fingerprint);
        step3 = findViewById(R.id.step3_iris);
        step4 = findViewById(R.id.step4_face);

        etUserName = findViewById(R.id.etUserName);
        etPrenom = findViewById(R.id.etPrenom);
        etSex = findViewById(R.id.etSex);
        etAge = findViewById(R.id.etAge);
        etVille = findViewById(R.id.etVille);
        fpPreview = findViewById(R.id.fpPreview);
        irisFragmentContainer = findViewById(R.id.irisFragmentContainer);
        tvFaceResult = findViewById(R.id.tvFaceResult);
        irisHintTv = findViewById(R.id.irisHintTv);

        // Power cycle to reset state in case of previous crash
        TR760Manager.getInstance().fingerprintPower(false);
        try {
            Thread.sleep(300);
        } catch (Exception e) {
        }
        TR760Manager.getInstance().fingerprintPower(true);

        initFingerprint();

        setupStep1();
        setupStep2();
        setupStep3();
        setupStep4();

        showStep(1);
    }

    private void showStep(int step) {
        step1.setVisibility(step == 1 ? View.VISIBLE : View.GONE);
        step2.setVisibility(step == 2 ? View.VISIBLE : View.GONE);
        step3.setVisibility(step == 3 ? View.VISIBLE : View.GONE);
        step4.setVisibility(step == 4 ? View.VISIBLE : View.GONE);

        switch (step) {
            case 1:
                stepTitle.setText("Étape 1 : Informations Civiles");
                break;
            case 2:
                String currentTitle = stepTitle.getText().toString();
                if (!currentTitle.contains("prêt") && !currentTitle.contains("FAILED")
                        && !currentTitle.contains("Detached") && !currentTitle.contains("Initialisation")) {
                    stepTitle.setText("Étape 2 : Empreintes (442)");
                }
                break;
            case 3:
                stepTitle.setText("Étape 3 : Iris");
                break;
            case 4:
                stepTitle.setText("Étape 4 : Visage et Fin");
                break;
        }
    }

    private void setupStep1() {
        findViewById(R.id.btnNextToStep2).setOnClickListener(v -> {
            String name = etUserName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(this, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show();
                return;
            }
            mUserName = name;
            mPrenom = etPrenom.getText().toString().trim();
            mSex = etSex.getText().toString().trim();
            mDateNaissance = etAge.getText().toString().trim();
            mLocalite = etVille.getText().toString().trim();
            showStep(2);
        });
    }

    private void initFingerprint() {
        runOnUiThread(() -> stepTitle.setText("Étape 2 : Initialisation du capteur..."));

        mFingerApi.init(getApplicationContext(), FapInitParam.LOG_OPEN, state -> {
            if (state == EnumDeviceState.HAVE_DEVICE || state == EnumDeviceState.DEVICE_ATTACHED) {
                executor.execute(() -> {
                    int result = mFingerApi.openDevice();
                    runOnUiThread(() -> {
                        if (result == 0) {
                            stepTitle.setText("Étape 2 : Capteur prêt !");
                            stepTitle.setOnClickListener(null); // Remove retry listener
                            Button btnCapture = findViewById(R.id.btnCaptureFp);
                            btnCapture.setEnabled(true);
                            updateFpUI(btnCapture, true);
                            findViewById(R.id.btnNextToStep3).setEnabled(true);
                            Toast.makeText(MultimodalEnrollmentActivity.this, "Fingerprint device opened!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            stepTitle.setText("Step 2: FAILED to open: " + result + " (Tap to retry)");
                            stepTitle.setOnClickListener(v -> initFingerprint());
                        }
                    });
                });
            } else {
                runOnUiThread(() -> {
                    stepTitle.setText("Step 2: Detached: " + state + " (Tap to retry)");
                    stepTitle.setOnClickListener(v -> initFingerprint());
                });
            }
        });
    }

    private int mFpCaptureStep = 0;

    private void setupStep2() {
        fpPreview = findViewById(R.id.fpPreview);
        tvFpHint = findViewById(R.id.tvFpHint);
        tvFingerScore = findViewById(R.id.tvFingerScore);

        Button btnCapture = findViewById(R.id.btnCaptureFp);
        btnCapture.setEnabled(false); // Initially disabled until init succeeds
        btnCapture.setText("EN ATTENTE DU CAPTEUR...");
        findViewById(R.id.btnNextToStep3).setEnabled(false);

        btnCapture.setOnClickListener(v -> {
            if (mFingerApi == null)
                return;
            String text = btnCapture.getText().toString();
            if (text.equals("VOIR LA CAMERA")) {
                if (mFpCaptureStep == 0)
                    btnCapture.setText("CAPTURER MAIN GAUCHE");
                else if (mFpCaptureStep == 1)
                    btnCapture.setText("CAPTURER MAIN DROITE");
                else
                    btnCapture.setText("CAPTURER POUCES");

                new Thread(() -> {
                    mFingerApi.video((mxImage, mxError) -> {
                        if (mxImage != null) {
                            byte[] bmpData = new byte[mxImage.width * mxImage.height + 1078];
                            mFingerAlgAPI.convertRawToBMP(mxImage.data, mxImage.width, mxImage.height, bmpData);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
                            runOnUiThread(() -> fpPreview.setImageBitmap(bitmap));
                        } else {
                            runOnUiThread(() -> Toast
                                    .makeText(MultimodalEnrollmentActivity.this,
                                            "Video error: " + (mxError != null ? mxError : "null"), Toast.LENGTH_SHORT)
                                    .show());
                        }
                    });
                }).start();
            } else {
                btnCapture.setText("PRÉPARATION...");
                btnCapture.setEnabled(false);
                mFingerApi.stopCapture();

                // Le capteur physique a besoin d'un instant pour fermer le flux vidéo
                // avant de pouvoir ouvrir le flux de capture haute résolution.
                v.postDelayed(() -> {
                    btnCapture.setEnabled(true);
                    captureFingerprint();
                }, 800);
            }
        });

        findViewById(R.id.btnNextToStep3).setOnClickListener(v -> {
            if (mFingerMap.isEmpty()) {
                Toast.makeText(this, "Please capture at least one fingerprint", Toast.LENGTH_SHORT).show();
                return;
            }
            // Stop and close fingerprint properly before cutting power
            mFingerApi.stopCapture();
            v.postDelayed(() -> {
                mFingerApi.closeDevice();
                initIris();
                showStep(3);
            }, 500);
        });
    }

    private void updateFpUI(Button btn, boolean isFirst) {
        if (mFpCaptureStep < 3) {
            btn.setText("VOIR LA CAMERA");
            btn.setEnabled(true); // Réactiver le bouton pour la main suivante
            tvFingerScore.setText("Score: N/A");
        }

        if (mFpCaptureStep == 0) {
            tvFpHint.setText("Étape 1 : Posez 4 doigts de la main GAUCHE");
        } else if (mFpCaptureStep == 1) {
            tvFpHint.setText("Étape 2 : Posez 4 doigts de la main DROITE");
        } else if (mFpCaptureStep == 2) {
            tvFpHint.setText("Étape 3 : Posez vos 2 POUCES");
        } else {
            btn.setText("ALL DONE");
            btn.setEnabled(false);
            tvFpHint.setText("Empreintes 4-4-2 terminées avec succès !");
            tvFingerScore.setText("Score: OK");
        }
    }

    private void captureFingerprint() {
        EnumPrintType currentType;
        if (mFpCaptureStep == 0) {
            currentType = EnumPrintType.LEFT_FOUR;
        } else if (mFpCaptureStep == 1) {
            currentType = EnumPrintType.RIGHT_FOUR;
        } else {
            currentType = EnumPrintType.BOTH_THUMB;
        }

        runOnUiThread(() -> {
            Button btnCapture = findViewById(R.id.btnCaptureFp);
            btnCapture.setText("ANALYSE EN COURS...");
            btnCapture.setEnabled(false);
            tvFingerScore.setText("Analyse en cours... Gardez vos doigts posés.");
        });

        CaptureConfig captureConfig = new CaptureConfig.Builder()
                .setEnumPrintType(currentType)
                .setMissingFinger(new java.util.ArrayList<>())
                .setAreaScore(CaptureConfig.DEFAULT_AREA_SCORE) // Lowered area
                .setNfiqLevel(5) // 5 is the most permissive level for dry fingers
                .setPreviewCallBack((mxImage, mxError) -> {
                    if (mxImage != null) {
                        byte[] bmpData = new byte[mxImage.width * mxImage.height + 1078];
                        mFingerAlgAPI.convertRawToBMP(mxImage.data, mxImage.width, mxImage.height, bmpData);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bmpData, 0, bmpData.length);
                        runOnUiThread(() -> {
                            fpPreview.setImageBitmap(bitmap);
                            if (mxImage.singleFingerImages != null && mxImage.singleFingerImages.length > 0) {
                                int totalNfiq = 0;
                                int totalArea = 0;
                                for (MxImage.RawInfo info : mxImage.singleFingerImages) {
                                    totalNfiq += info.nfiqLevel;
                                    totalArea += info.area;
                                }
                                int avgNfiq = totalNfiq / mxImage.singleFingerImages.length;
                                int avgArea = totalArea / mxImage.singleFingerImages.length;
                                tvFingerScore.setText("Doigts détectés: " + mxImage.singleFingerImages.length
                                        + "\nNFIQ: " + avgNfiq + " | Surface: " + avgArea);
                            } else {
                                tvFingerScore.setText("Analyse en cours... Appuyez bien vos 4 doigts sur la vitre !");
                            }
                        });
                    }
                }).build();

        executor.execute(() -> {
            MxResult<MxImage> capture = mFingerApi.getImage(captureConfig);

            if (capture != null && capture.getData() != null && capture.getData().singleFingerImages != null) {
                MxImage.RawInfo[] fingers = capture.getData().singleFingerImages;
                for (int i = 0; i < capture.getData().fingerNum; i++) {
                    byte[] template = new byte[1024];
                    int[] len = new int[1];
                    int res = mFingerAlgAPI.createTemplateISO(
                            fingers[i].imageData, fingers[i].width, fingers[i].height,
                            12, template, true, len);
                    if (res >= 0) {
                        String prefix = "";
                        if (mFpCaptureStep == 0)
                            prefix = "left_";
                        else if (mFpCaptureStep == 1)
                            prefix = "right_";
                        else if (mFpCaptureStep == 2)
                            prefix = "thumb_";
                        
                        String positionKey = prefix + i;
                        mFingerMap.put(positionKey, android.util.Base64.encodeToString(Arrays.copyOf(template, len[0]),
                                android.util.Base64.DEFAULT));
                                
                        byte[] singleBmpData = new byte[fingers[i].width * fingers[i].height + 1078];
                        int bmpRes = mFingerAlgAPI.convertRawToBMP(fingers[i].imageData, fingers[i].width, fingers[i].height, singleBmpData);
                        if (bmpRes == 0) {
                            mFingerImageMap.put(positionKey, android.util.Base64.encodeToString(singleBmpData, android.util.Base64.DEFAULT));
                        }
                    }
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, "Capture " + (mFpCaptureStep + 1) + "/3 réussie !", Toast.LENGTH_SHORT).show();
                    mFpCaptureStep++;
                    Button btnCapture = findViewById(R.id.btnCaptureFp);
                    updateFpUI(btnCapture, false);
                });
            } else {
                runOnUiThread(() -> {
                    String msg = (capture != null) ? capture.getMsg() : "null";
                    Toast.makeText(this, "Échec: " + msg, Toast.LENGTH_LONG).show();
                    tvFingerScore.setText("Erreur : " + msg);
                    Button btnCapture = findViewById(R.id.btnCaptureFp);
                    updateFpUI(btnCapture, true);
                });
            }
        });
    }

    // --- IRIS ---
    private void setupStep3() {
        findViewById(R.id.btnCaptureIris).setOnClickListener(v -> {
            if (mIrisFragment != null) {
                mIrisFragment.enroll(15000, new IrisFragment.EnrollCallback() {
                    @Override
                    public void onSuccess(byte[] template) {
                        mIrisLeft = template; // Simplified for demo
                        Toast.makeText(MultimodalEnrollmentActivity.this, "Iris Captured!", Toast.LENGTH_SHORT).show();
                        irisHintTv.setText("Iris Capture Success!");
                    }

                    @Override
                    public void onError(int errCode, String msg) {
                        Toast.makeText(MultimodalEnrollmentActivity.this, "Iris Error: " + msg, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onProcess(int progress, int state) {
                        irisHintTv.setText("Iris Progress: " + progress + "%");
                    }
                });
            }
        });
        findViewById(R.id.btnNextToStep4).setOnClickListener(v -> {
            if (mIrisLeft == null) {
                Toast.makeText(this, "Please capture Iris first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (mIrisFragment != null) {
                mIrisFragment.closeCamera();
            }
            showStep(4);
        });
    }

    private void initIris() {
        mProgressDialog.setMessage("Loading Iris...");
        mProgressDialog.show();

        // Power sequence: turn off fingerprint, turn on Iris
        TR760Manager.getInstance().fingerprintPower(false);
        TR760Manager.getInstance().IrPower(true);

        // Add 2.5s delay to ensure the USB host has fully enumerated the Iris camera
        getWindow().getDecorView().postDelayed(() -> {
            mIrisSDK = IrisSDK.getInstance();
            mIrisSDK.init(getApplicationContext(), "", new IrisSDK.IrisSDKCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        FragmentManager manager = getFragmentManager();
                        mIrisFragment = new IrisFragment();

                        mIrisFragment.addCallback(new IrisFragment.CameraCallbackX() {
                            @Override
                            public void onOpen() {
                            }

                            @Override
                            public void onOpenError(int error) {
                            }

                            @Override
                            public void onClose() {
                            }

                            @Override
                            public void onPreviewFailed() {
                            }

                            @Override
                            public void onPreview() {
                                runOnUiThread(() -> mIrisFragment.openLight());
                            }
                        });

                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.irisFragmentContainer, mIrisFragment);
                        transaction.commit();

                        // Exact same delay and close/open dance as IrisActivity
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                            } catch (Exception e) {
                            }
                            runOnUiThread(() -> {
                                if (mIrisFragment != null)
                                    mIrisFragment.closeCamera();
                            });
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                            }
                            runOnUiThread(() -> {
                                if (mIrisFragment != null)
                                    mIrisFragment.openCamera();
                                mProgressDialog.dismiss();
                            });
                        }).start();
                    });
                }

                @Override
                public void onError(int code, String msg) {
                    runOnUiThread(() -> {
                        mProgressDialog.dismiss();
                        Toast.makeText(MultimodalEnrollmentActivity.this, "Iris Init Failed: " + msg, Toast.LENGTH_LONG)
                                .show();
                    });
                }
            });
        }, 2500); // 2500ms delay for USB enumeration
    }

    // --- FACE ---
    private void setupStep4() {
        findViewById(R.id.btnLaunchFaceCamera).setOnClickListener(v -> {
            // Turn off Iris power when going to face
            TR760Manager.getInstance().IrPower(false);
            if (mIrisFragment != null) {
                mIrisFragment.closeCamera();
            }
            Intent intent = new Intent(this, CameraActivity.class);
            faceResultLauncher.launch(intent);
        });

        findViewById(R.id.btnFinishEnrollment).setOnClickListener(v -> {
            if (TextUtils.isEmpty(mFaceToken)) {
                Toast.makeText(this, "Please capture Face first", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAllData();
        });
    }

    private void processFaceImage(String path) {
        mProgressDialog.setMessage("Processing Face...");
        mProgressDialog.show();
        InitFacePassHandler.init(this, facePassHandler -> {
            if (facePassHandler != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                try {
                    FacePassAddFaceResult result = facePassHandler.addFace(bitmap);
                    if (result != null && result.result == 0) {
                        if (facePassHandler.bindGroup(group_name, result.faceToken)) {
                            mFaceToken = new String(result.faceToken, StandardCharsets.ISO_8859_1);
                            
                            // Load image file to Base64
                            try {
                                java.io.File file = new java.io.File(path);
                                byte[] fileBytes = new byte[(int) file.length()];
                                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                                fis.read(fileBytes);
                                fis.close();
                                mPhotoFaceBase64 = android.util.Base64.encodeToString(fileBytes, android.util.Base64.DEFAULT);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            
                            runOnUiThread(() -> {
                                mProgressDialog.dismiss();
                                tvFaceResult.setText("Face Captured Successfully!");
                            });
                            return;
                        }
                    }
                } catch (FacePassException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                mProgressDialog.dismiss();
                Toast.makeText(MultimodalEnrollmentActivity.this, "Face Capture Failed", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // --- SAVE ---
    private void saveAllData() {
        // 1. Sauvegarde locale (SQLite & EasyCache)
        User user = new User();
        user.id = System.currentTimeMillis();
        user.faceToken = mFaceToken;
        user.name = mUserName;
        user.prenom = mPrenom;
        user.sex = mSex;
        user.dateNaissance = mDateNaissance;
        user.localite = mLocalite;
        ((App) getApplication()).getUserDao().insert(user);

        EasyCache easyCache = new EasyCache(this, "fp");
        for (String key : mFingerMap.keySet()) {
            easyCache.putString(mUserName + "-" + key, mFingerMap.get(key));
        }

        EasyCache irisCache = new EasyCache(this, "iris");
        irisCache.putString(mUserName + "-left", new String(mIrisLeft, StandardCharsets.ISO_8859_1));

        // 2. Envoi vers l'API Spring Boot (PostgreSQL)
        sendToBackendApi();
    }

    private void sendToBackendApi() {
        mProgressDialog.setMessage("Envoi vers le serveur...");
        mProgressDialog.show();

        executor.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("nom", mUserName);
                json.put("prenom", mPrenom);
                json.put("sexe", mSex);
                json.put("dateNaissance", mDateNaissance);
                json.put("localite", mLocalite);
                json.put("faceToken", mFaceToken);
                json.put("facePhotoBase64", mPhotoFaceBase64);

                JSONArray empreintesArray = new JSONArray();
                for (String key : mFingerMap.keySet()) {
                    JSONObject emp = new JSONObject();
                    emp.put("position", key);
                    emp.put("templateBase64", mFingerMap.get(key));
                    if (mFingerImageMap.containsKey(key)) {
                        emp.put("imageBase64", mFingerImageMap.get(key));
                    }
                    empreintesArray.put(emp);
                }
                json.put("empreintes", empreintesArray);

                JSONArray irisArray = new JSONArray();
                JSONObject irisObj = new JSONObject();
                irisObj.put("position", "left");
                // Base64 encode for API consistency
                String irisBase64 = android.util.Base64.encodeToString(mIrisLeft, android.util.Base64.DEFAULT);
                irisObj.put("templateBase64", irisBase64);
                irisArray.put(irisObj);
                json.put("iris", irisArray);

                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
                Request request = new Request.Builder()
                        .url("http://192.168.11.48:8080/api/enroll") // ADRESSE IP DU PC
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> {
                            mProgressDialog.dismiss();
                            Toast.makeText(MultimodalEnrollmentActivity.this, "Erreur API: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            finish();
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        runOnUiThread(() -> {
                            mProgressDialog.dismiss();
                            if (response.isSuccessful()) {
                                Toast.makeText(MultimodalEnrollmentActivity.this,
                                        "Sauvegarde locale et Serveur réussie !", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MultimodalEnrollmentActivity.this, "Erreur Serveur: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                            finish();
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(MultimodalEnrollmentActivity.this, "Erreur préparation JSON", Toast.LENGTH_SHORT)
                            .show();
                    finish();
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFingerApi.stopCapture();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFingerApi.closeDevice();
        mFingerAlgAPI.free();
        if (mIrisFragment != null) {
            mIrisFragment.closeCamera();
        }
        TR760Manager.getInstance().fingerprintPower(false);
        TR760Manager.getInstance().IrPower(false);
    }
}
