package com.hf.tr760.demo;

import android.content.Intent;
import android.hibory.CommonApi;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;

import com.hf.passport.ui.CameraActivity;
import com.hf.passport.ui.PassportActivity;
import com.hf.tr760.R;
import com.hf.tr760.databinding.ActivityMainBinding;
import com.hf.tr760.facex.FaceXMainActivity;
import com.hf.tr760.fingerprint.Fingerprint442Activity;
import com.hf.tr760.fingerprint.FingerprintSearchActivity;
import com.hf.tr760.utils.DeviceUtils;
import com.hf.ui.base.EasyActivity;
import com.hf.ui.base.EasyEvent;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.palm.demo.activity.ExampleActivity;

public class MainActivity extends EasyActivity {
    ActivityMainBinding mainBinding;

    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void create() {
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        DeviceUtils.checkDevice(this);

        mainBinding.actionbar.imgTitleStyle(getString(R.string.app_name), 0, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mainBinding.usbA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(MainActivity.this).asBottomList("USB HOST",
                        new String[]{"OPEN", "CLOSE"}, new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                CommonApi commonApi = new CommonApi();
                                if(position==0){
                                    commonApi.setUsbHost(1);
                                }else {
                                    commonApi.setUsbHost(0);
                                }
                            }
                        }).show();
            }
        });

        mainBinding.btnMultimodal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(MainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(allGranted2 -> {
                    if (allGranted2) {
                        startActivity(new Intent(MainActivity.this, com.hf.tr760.fingerprint.MultimodalEnrollmentActivity.class));
                    }
                });
            }
        });

        mainBinding.fft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Fingerprint442Activity.class));
            }
        });

        mainBinding.fpSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FingerprintSearchActivity.class));
            }
        });

        mainBinding.nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,NfcActivity.class));
            }
        });

        mainBinding.iris.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(MainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(allGranted2 -> {
                    if (allGranted2) {
                        startActivity(new Intent(MainActivity.this,IrisActivity.class));
                    }
                });
            }
        });

        mainBinding.barcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,BarcodeActivity.class));
            }
        });

        mainBinding.mrz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(MainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(allGranted2 -> {
                    if (allGranted2) {
                        showListDialog();
                    }
                });
            }
        });

        mainBinding.face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!getEasyPermission().manage_storage(MainActivity.this)) {
                    return;
                }
                getEasyPermission().cameraAndStorage(allGranted2 -> {
                    if (allGranted2) {
                        startActivity(new Intent(MainActivity.this, FaceXMainActivity.class));
                    }
                });
            }
        });

        mainBinding.psam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PsamActivity.class));
            }
        });

        mainBinding.ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, IcActivity.class));
            }
        });

        mainBinding.fillLight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FillLightActivity.class));
            }
        });

        mainBinding.palm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ExampleActivity.class));
            }
        });
    }

    private void showListDialog() {
        // 定义列表项
        String[] items = new String[]{getString(R.string.item_fast), getString(R.string.item_ocr)};

        // 创建一个数组适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);

        // 创建一个AlertDialog构建器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an item");
        builder.setAdapter(adapter, (dialog, which) -> {
            // 当用户选择列表中的项时，执行一些操作
            // 这里我们只是显示所选的项
            String selectedItem = adapter.getItem(which);
            dialog.dismiss();
            // 你可以在这里更新UI，例如显示所选的项
            if (0 == which) {
                startActivity(new Intent(MainActivity.this, PassportActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            }
        });

        // 创建并显示AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
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
