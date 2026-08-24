package com.hf.passport.ui;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.gemalto.jp2.JP2Decoder;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.hf.passport.R;
import com.hf.passport.model.BacKeyParts;
import com.hf.passport.model.UserChipInfo;
//import com.hf.passport.util.ThreadManager;
import com.hf.passport.wsq.WsqDecoder;
import com.innovatrics.mrz.MrzRecord;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.jmrtd.BACKey;
import org.jmrtd.BACKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.ChipAuthenticationPublicKeyInfo;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SODFile;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG14File;
import org.jmrtd.lds.icao.DG1File;
import org.jmrtd.lds.icao.DG2File;
import org.jmrtd.lds.icao.MRZInfo;
import org.jmrtd.lds.iso19794.FaceImageInfo;
import org.jmrtd.lds.iso19794.FaceInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.jvm.internal.Intrinsics;
import params.com.stepview.StatusViewScroller;

public class PassportActivity extends AppCompatActivity implements TR760Manager.OnScannerListener {
    private static final String TAG = "PassportActivity";

    ToneGenerator tonePlayer;

    // status
    private StatusViewScroller sv_pp_status;
    private TextView tv_pp_tip;
    private Button btn_scan;
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

//    private ThreadManager threadManager;
    private Bitmap bitmapFromGallery = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport);

        tonePlayer = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);

        initView();
        initDate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableNfc();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableNfc();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (null != bacKeyParts) {
            startReadNfc(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tonePlayer!=null){
            tonePlayer.stopTone();
            tonePlayer.release();
        }
        TR760Manager.getInstance().BarcodeClose();
    }

    private void initView() {
        // status
        sv_pp_status = findViewById(R.id.sv_pp_status);
        tv_pp_tip = findViewById(R.id.tv_pp_tip);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnTouchListener(new View.OnTouchListener() {
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

        // user info
        ll_pp_user_info = findViewById(R.id.ll_pp_user_info);
        iv_pp_photo = findViewById(R.id.iv_pp_photo);
        tv_pp_info_type = findViewById(R.id.tv_pp_info_type);
        tv_pp_info_country_code = findViewById(R.id.tv_pp_info_country_code);
        tv_pp_info_no = findViewById(R.id.tv_pp_info_no);
        tv_pp_info_surname = findViewById(R.id.tv_pp_info_surname);
        tv_pp_info_given_name = findViewById(R.id.tv_pp_info_given_name);
        tv_pp_info_date_of_birth = findViewById(R.id.tv_pp_info_date_of_birth);
        tv_pp_info_gender = findViewById(R.id.tv_pp_info_gender);
        tv_pp_info_nationality = findViewById(R.id.tv_pp_info_nationality);
        tv_pp_info_date_of_expiry = findViewById(R.id.tv_pp_info_date_of_expiry);
        tv_pp_info_optional_info = findViewById(R.id.tv_pp_info_optional_info);
        tv_pp_info_auth_result = findViewById(R.id.tv_pp_info_auth_result);
        tv_pp_info_mrz = findViewById(R.id.tv_pp_info_mrz);

        setStep(1, getString(R.string.lib_pp_tip_step_1));

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bg_passport);
        if (bitmapFromGallery == null) {
            bitmapFromGallery = bitmap;
        }
    }

    private void initDate() {
        initScan();

//        threadManager = new ThreadManager();
    }

    private void initScan() {
        // 接下来的初始化操作，有可能会自动更新模组固件，需要保持屏幕常亮，避免整机休眠。
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TR760Manager.getInstance().setScannerListener(this);
        TR760Manager.getInstance().BarcodeOpen();
    }

    private void enableNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

            Intent intent = new Intent(this.getApplicationContext(), this.getClass());
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
            String[][] filter = new String[][]{{"android.nfc.tech.IsoDep"}};
            adapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, filter);
        }
    }

    private void disableNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null) {
            adapter.disableForegroundDispatch(this);
        }
    }

    protected void startReadNfc(Intent intent) {
        try {
            if (null != bacKeyParts && NfcAdapter.ACTION_TECH_DISCOVERED.equalsIgnoreCase(intent.getAction())) {
                Tag tag = Objects.requireNonNull(intent.getExtras()).getParcelable(NfcAdapter.EXTRA_TAG);
                assert tag != null;
                String[] techList = tag.getTechList();
                if (Arrays.asList(Arrays.copyOf(techList, techList.length)).contains("android.nfc.tech.IsoDep")) {
                    setStep(3, getString(R.string.lib_pp_tip_step_3));
//                    threadManager.execute(() -> {
//                        BACKeySpec bacKey = new BACKey(bacKeyParts.getDocNum(), bacKeyParts.getBirthDate(), bacKeyParts.getExpiryDate());
//                        readTask(IsoDep.get(tag), bacKey);
//                    });
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BACKeySpec bacKey = new BACKey(bacKeyParts.getDocNum(), bacKeyParts.getBirthDate(), bacKeyParts.getExpiryDate());
                            readTask(IsoDep.get(tag), bacKey);
                        }
                    }).start();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setStep(3, getString(R.string.lib_pp_tip_read_failure));
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
            sv_pp_status.getStatusView().setCurrentCount(step);
            tv_pp_tip.setText(tip);
        });
    }

    @SuppressLint("SetTextI18n")
    private void setUserInfo(UserChipInfo chipInfo) {
        if (null == chipInfo) {
            return;
        }
        runOnUiThread(() -> {
            if (null != chipInfo.getPhoto()) {
                iv_pp_photo.setImageBitmap(chipInfo.getPhoto());
                setStep(5,getString(R.string.lib_pp_auth_result_success));
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
            tv_pp_info_date_of_birth.setText(chipInfo.getBirthDate());
            tv_pp_info_gender.setText(chipInfo.getGender());
            tv_pp_info_nationality.setText(chipInfo.getNationality());
            tv_pp_info_date_of_expiry.setText(chipInfo.getExpiryDate());
            if (!chipInfo.getOptionalData1().isEmpty() || !chipInfo.getOptionalData2().isEmpty()) {
                tv_pp_info_optional_info.setText(chipInfo.getOptionalData1() + " " + chipInfo.getOptionalData2());
            } else {
                tv_pp_info_optional_info.setText("");
            }

            if (null != chipInfo.getChipAuth()) {
                String result = getString(chipInfo.getChipAuth() ? R.string.lib_pp_auth_result_success : R.string.lib_pp_auth_result_failed);
                tv_pp_info_auth_result.setText(result);
            } else if (null != chipInfo.getPassiveAuth()) {
                String result = getString(chipInfo.getPassiveAuth() ? R.string.lib_pp_auth_result_success : R.string.lib_pp_auth_result_failed);
                tv_pp_info_auth_result.setText(result);
            } else {
                tv_pp_info_auth_result.setText("");
            }
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

            iv_pp_photo.setImageBitmap(null);
        });
    }

    private void readTask(IsoDep isoDep, BACKeySpec bacKey) {
        PassportService service = null;

        try {
            CardService cardService = CardService.getInstance(isoDep);
            cardService.open();

            service = new PassportService(cardService, PassportService.NORMAL_MAX_TRANCEIVE_LENGTH, org.jmrtd.PassportService.DEFAULT_MAX_BLOCKSIZE, false, false);
            service.open();

            // read access file
            boolean paceSucceeded = false;
            try {
                CardAccessFile cardAccessFile = new CardAccessFile(service.getInputStream(PassportService.EF_CARD_ACCESS));
                for (SecurityInfo securityInfo : cardAccessFile.getSecurityInfos()) {
                    if (securityInfo instanceof PACEInfo) {
                        service.doPACE(bacKey, securityInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(((PACEInfo) securityInfo).getParameterId()), null);
                        paceSucceeded = true;
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, e);
            }
            service.sendSelectApplet(paceSucceeded);

            if (!paceSucceeded) {
                try {
                    service.getInputStream(PassportService.EF_COM).read();
                } catch (Exception e) {
                    service.doBAC(bacKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            DG1File dg1File = new DG1File(service.getInputStream(PassportService.EF_DG1));
            UserChipInfo chipInfo = transform(dg1File, null);
            chipInfo.setChipAuth(null);
            chipInfo.setPassiveAuth(null);
            setUserInfo(chipInfo);
            setStep(4, getString(R.string.lib_pp_tip_step_4));
            DG2File dg2File = new DG2File(service.getInputStream(PassportService.EF_DG2));
            ArrayList<FaceImageInfo> allFaceImageInfo = new ArrayList<>();
            for (FaceInfo faceImageInfo : dg2File.getFaceInfos()) {
                allFaceImageInfo.addAll(faceImageInfo.getFaceImageInfos());
            }
            if (!allFaceImageInfo.isEmpty()) {
                FaceImageInfo faceImageInfo = allFaceImageInfo.get(0);
                int imageLength = faceImageInfo.getImageLength();
                DataInputStream dataInputStream = new DataInputStream(faceImageInfo.getImageInputStream());
                byte[] buffer = new byte[imageLength];
                dataInputStream.readFully(buffer, 0, imageLength);
//                InputStream inputStream = new ByteArrayInputStream(buffer, 0, imageLength);
                Bitmap bitmap = decodeImage(faceImageInfo.getMimeType(), buffer);
//                String imageBase64 = Base64.encodeToString(buffer, Base64.DEFAULT);
                chipInfo = transform(dg1File, bitmap);
            }
            chipInfo.setChipAuth(null);
            chipInfo.setPassiveAuth(null);
            setUserInfo(chipInfo);

            setStep(5, getString(R.string.lib_pp_tip_step_5));
            SODFile sodFile = new SODFile(service.getInputStream(PassportService.EF_SOD));

            // We perform Chip Authentication using Data Group 14
            doChipAuth(service);

            // Then Passive Authentication using SODFile
            doPassiveAuth(dg1File, dg2File, sodFile);

            if (chipAuthSucceeded) {
                chipInfo.setChipAuth(true);
            } else {
                chipInfo.setChipAuth(null);
            }
            if (passiveAuthSuccess) {
                chipInfo.setPassiveAuth(true);
            } else {
                chipInfo.setPassiveAuth(null);
            }
            setUserInfo(chipInfo);

            boolean isAuthPassed = chipAuthSucceeded || passiveAuthSuccess;
            runOnUiThread(() -> {
                if (isAuthPassed) {
                    sv_pp_status.getStatusView().setCurrentDrawable(getDrawable(R.drawable.pp_ic_done_black_24dp));
                } else {
                    sv_pp_status.getStatusView().setCurrentDrawable(getDrawable(R.drawable.ic_dissatisfied_black_24dp));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (sv_pp_status.getStatusView().getCurrentCount() == 3 && e instanceof CardServiceException) {
                CardServiceException cardServiceException = (CardServiceException) e;
                if (cardServiceException.getSW() == 0x6982) {
                    setStep(sv_pp_status.getStatusView().getCurrentCount(), getString(R.string.lib_pp_auth_result_failed_1));
                }
                return;
            }
            if (sv_pp_status.getStatusView().getCurrentCount() == 4) {
                setStep(sv_pp_status.getStatusView().getCurrentCount(), getString(R.string.lib_pp_auth_result_failed_2));
            } else {
                setStep(sv_pp_status.getStatusView().getCurrentCount(), getString(R.string.lib_pp_tip_read_failure));
            }
        }
    }

    private void doChipAuth(PassportService service) {
        try {
            CardFileInputStream dg14In = service.getInputStream((short) 270, 223);
            dg14Encoded = IOUtils.toByteArray(dg14In);
            ByteArrayInputStream dg14InByte = new ByteArrayInputStream(dg14Encoded);
            DG14File dg14File = new DG14File(dg14InByte);
            Collection<SecurityInfo> dg14FileSecurityInfos = dg14File.getSecurityInfos();

            for (SecurityInfo securityInfo : dg14FileSecurityInfos) {
                if (securityInfo instanceof ChipAuthenticationPublicKeyInfo) {
                    BigInteger keyId = ((ChipAuthenticationPublicKeyInfo) securityInfo).getKeyId();
                    PublicKey publicKey = ((ChipAuthenticationPublicKeyInfo) securityInfo).getSubjectPublicKey();
                    String oid = securityInfo.getObjectIdentifier();
                    service.doEACCA(keyId, ChipAuthenticationPublicKeyInfo.ID_CA_ECDH_AES_CBC_CMAC_256, oid, publicKey);
                    this.chipAuthSucceeded = true;
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private void doPassiveAuth(DG1File dg1File, DG2File dg2File, SODFile sodFile) {
        try {
            MessageDigest digest = MessageDigest.getInstance(sodFile.getDigestAlgorithm());
            Map<Integer, byte[]> dataHashes = sodFile.getDataGroupHashes();
            byte[] dg14Hash = new byte[0];
            if (this.chipAuthSucceeded) {
                dg14Hash = digest.digest(this.dg14Encoded);
            }

            byte[] dg1Hash = digest.digest(dg1File.getEncoded());
            byte[] dg2Hash = digest.digest(dg2File.getEncoded());
            if (Arrays.equals(dg1Hash, dataHashes.get(1)) && Arrays.equals(dg2Hash, dataHashes.get(2)) && (!this.chipAuthSucceeded || Arrays.equals(dg14Hash, dataHashes.get(14)))) {
                ASN1InputStream asn1InputStream = new ASN1InputStream(this.getAssets().open("masterList"));
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(null, null);
                CertificateFactory cf = CertificateFactory.getInstance("X.509");

                while (true) {
                    ASN1Primitive asn1Primitive = asn1InputStream.readObject();
                    if (asn1Primitive == null) {
                        List<X509Certificate> docSigningCertificates = sodFile.getDocSigningCertificates();
                        for (X509Certificate signingCertificate : docSigningCertificates) {
                            signingCertificate.checkValidity();
                        }

                        CertPath cp = cf.generateCertPath(docSigningCertificates);
                        PKIXParameters pkixParameters = new PKIXParameters(keystore);
                        pkixParameters.setRevocationEnabled(false);
                        CertPathValidator cpv = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
                        cpv.validate(cp, pkixParameters);
                        String sodDigestEncryptionAlgorithm = sodFile.getDocSigningCertificate().getSigAlgName();
                        boolean isSSA = false;
                        if (Intrinsics.areEqual(sodDigestEncryptionAlgorithm, "SSAwithRSA/PSS")) {
                            sodDigestEncryptionAlgorithm = "SHA256withRSA/PSS";
                            isSSA = true;
                        }

                        Signature sign = Signature.getInstance(sodDigestEncryptionAlgorithm);
                        if (isSSA) {
                            sign.setParameter(new PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1));
                        }

                        sign.initVerify(sodFile.getDocSigningCertificate());
                        sign.update(sodFile.getEContent());
                        this.passiveAuthSuccess = sign.verify(sodFile.getEncryptedDigest());
                        break;
                    }

                    ASN1Sequence asn1 = ASN1Sequence.getInstance(asn1Primitive);
                    boolean isAsn1Valid = asn1 != null && asn1.size() != 0;
                    String message;
                    if (!isAsn1Valid) {
                        message = "null or empty sequence passed.";
                        throw new IllegalArgumentException(message);
                    }

                    isAsn1Valid = asn1.size() == 2;
                    if (!isAsn1Valid) {
                        message = "Incorrect sequence size: " + asn1.size();
                        throw new IllegalArgumentException(message);
                    }

                    ASN1Set certSet = ASN1Set.getInstance(asn1.getObjectAt(1));
                    for (int i = 0; i < certSet.size(); ++i) {
                        org.bouncycastle.asn1.x509.Certificate certificate = org.bouncycastle.asn1.x509.Certificate.getInstance(certSet.getObjectAt(i));
                        byte[] pemCertificate = certificate.getEncoded();
                        Certificate javaCertificate = cf.generateCertificate(new ByteArrayInputStream(pemCertificate));
                        keystore.setCertificateEntry(String.valueOf(i), javaCertificate);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }

    private UserChipInfo transform(DG1File dg1File, Bitmap bitmap) {
        if (null == dg1File) {
            return null;
        }

        MRZInfo mrzInfo = dg1File.getMRZInfo();
        Bitmap photo = null;
        if (null != bitmap) {
            double ratio = 320.0 / bitmap.getHeight();
            int targetHeight = (int) (bitmap.getHeight() * ratio);
            int targetWidth = (int) (bitmap.getWidth() * ratio);
            photo = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false);
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
        userChipInfo.setPhoto(photo);
        return userChipInfo;
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

    public Bitmap decodeImage(String mimeType, byte[] bytes) {
        try {
            if (mimeType.equalsIgnoreCase("image/jp2") || mimeType.equalsIgnoreCase("image/jpeg2000")) {
                JP2Decoder jp2Decoder = new JP2Decoder(bytes);
                return jp2Decoder.decode();
            } else if (mimeType.equalsIgnoreCase("image/x-wsq")) {
                WsqDecoder wsqDecoder = new WsqDecoder();
                com.hf.passport.wsq.Bitmap bitmap = wsqDecoder.decode(bytes);
                byte[] byteData = bitmap.getPixels();
                int[] intData = new int[byteData.length];
                for (int j = 0; j < byteData.length; j++) {
                    intData[j] = 0xFF000000 | ((byteData[j] & 0xFF) << 16) | ((byteData[j] & 0xFF) << 8) | (byteData[j] & 0xFF);
                }
                return Bitmap.createBitmap(intData, 0, bitmap.getWidth(), bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } else {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    StringBuilder stringBuilder = new StringBuilder();
    long lastScanTime = 0;
    long lastShowTime = 0;

    @Override
    public void onScan(byte[] bytes) {
        if (lastScanTime == 0 || (System.currentTimeMillis() - lastScanTime) < 300) {
            lastScanTime = System.currentTimeMillis();
        } else {
            stringBuilder = new StringBuilder();
            lastScanTime = System.currentTimeMillis();
        }
        if (bytes != null) {
            stringBuilder.append(new String(bytes));
        }
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastShowTime == 0 || System.currentTimeMillis() - lastShowTime > 1000) {
                    lastShowTime = System.currentTimeMillis();
                    tonePlayer.startTone(ToneGenerator.TONE_PROP_BEEP, 100);
                    Log.e("parse", stringBuilder.toString());
                    bacKeyParts = parseMRZInfo(stringBuilder.toString());
                    if (null != bacKeyParts) {
                        runOnUiThread(() -> {
                            setStep(2, getString(R.string.lib_pp_tip_step_2));
                        });
                    } else {
                        runOnUiThread(() -> setStep(1, getString(R.string.lib_pp_tip_parse_code_failure)));
                    }
                    TR760Manager.getInstance().BarcodeTrigger(false);
                }
            }
        }, 1000);

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
}
