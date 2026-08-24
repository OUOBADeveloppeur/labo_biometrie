package com.hf.tr760.facex;

import static com.hf.tr760.facex.facepass.InitFacePassHandler.group_name;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.hf.permission.EasyPermission;
import com.hf.tr760.R;
import com.hf.tr760.databinding.ActivityFacexMainBinding;
import com.hf.tr760.facex.facepass.InitFacePassHandler;
import com.hf.tr760.facex.facepass.ScanFaceActivity;
import com.hf.tr760.facex.facepass.UserListActivity;
import com.hf.tr760.facex.facepass.camera.CameraActivity;
import com.hf.tr760.facex.facepass.db.User;
import com.hf.ui.base.EasyEvent;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;

import java.nio.charset.StandardCharsets;

import mcv.facepass.FacePassException;
import mcv.facepass.FacePassHandler;
import mcv.facepass.types.FacePassAddFaceResult;

public class FaceXMainActivity extends BaseActivity {

    ActivityFacexMainBinding mainBinding;

    private ProgressDialog mProgressDialog;

    private String signName= "";

    ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == 200) {
                    Intent intent = result.getData();
                    String path = intent.getStringExtra("data");

                    mProgressDialog.setMessage(getString(R.string.loading));
                    mProgressDialog.show();
                    InitFacePassHandler.init(FaceXMainActivity.this, new InitFacePassHandler.IFacePassInit() {
                        @Override
                        public void result(FacePassHandler facePassHandler) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressDialog.dismiss();
                                }
                            });
                            if (facePassHandler != null) {
                                Bitmap bitmap = BitmapFactory.decodeFile(path);

                                try {
                                    FacePassAddFaceResult result = facePassHandler.addFace(bitmap);
                                    if (result != null) {
                                        if (result.result == 0) {

                                            if(!facePassHandler.bindGroup(group_name,result.faceToken)){
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(FaceXMainActivity.this,getString(R.string.failed),Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                                return;
                                            }
                                            User user = new User();
                                            user.id = System.currentTimeMillis();
                                            user.faceToken = new String(result.faceToken, StandardCharsets.ISO_8859_1);
                                            user.name = signName;
                                            ((App)getApplication()).getUserDao().insert(user);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(FaceXMainActivity.this,getString(R.string.success),Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else if (result.result == 1) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(FaceXMainActivity.this,"no face ！",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(FaceXMainActivity.this,"quality problem！",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(FaceXMainActivity.this,"face chek failed！",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (FacePassException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(FaceXMainActivity.this,getString(R.string.failed),Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(FaceXMainActivity.this,getString(R.string.failed),Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                            }
                        }
                    });
                }
            });


    @Override
    public void onCreateBase(Bundle bundle) {
        mainBinding = ActivityFacexMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        mainBinding.actionbar.titleStyle(getString(R.string.app_name));

        mainBinding.module.setText(Build.MODEL + "-" + Build.DISPLAY);
        mainBinding.vn.setText("version_name:" + getVersionName());
        mainBinding.vc.setText("version_code:" + getVersionCode() + "");

        mProgressDialog = new ProgressDialog(this);

        mainBinding.llScanface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(FaceXMainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            startActivity(new Intent(FaceXMainActivity.this, ScanFaceActivity.class));
                        }
                    }
                });
            }
        });
        mainBinding.llUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(FaceXMainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(new EasyPermission.IPermissionResult() {
                    @Override
                    public void result(boolean allGranted) {
                        if (allGranted) {
                            startActivity(new Intent(FaceXMainActivity.this, UserListActivity.class));
                        }
                    }
                });
            }
        });

        mainBinding.llRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(FaceXMainActivity.this).asInputConfirm(getString(R.string.user_name), "", new OnInputConfirmListener() {
                    @Override
                    public void onConfirm(String text) {
                        if(!text.isEmpty()){
                            signName= text;
                            resultLauncher.launch(new Intent(FaceXMainActivity.this, CameraActivity.class));
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

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

    }
}
