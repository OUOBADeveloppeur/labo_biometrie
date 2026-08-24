package com.hf.tr760.facex;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.hf.ui.base.EasyActivity;

public abstract class BaseActivity extends EasyActivity {
    public String MODEL ="";

    public abstract void onCreateBase(Bundle bundle);

    @Override
    public void create() {
        MODEL = android.os.Build.MODEL.replace("-","");
        onCreateBase(null);
    }
}
