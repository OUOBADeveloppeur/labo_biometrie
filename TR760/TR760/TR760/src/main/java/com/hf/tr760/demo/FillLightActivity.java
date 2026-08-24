package com.hf.tr760.demo;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.hf.tr760.R;
import com.hf.tr760.utils.TR760Manager;

public class FillLightActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_light);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        Button open = findViewById(R.id.open);
        Button close = findViewById(R.id.close);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TR760Manager.getInstance().WhiteLightPower(true);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TR760Manager.getInstance().WhiteLightPower(false);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        TR760Manager.getInstance().WhiteLightPower(false);
    }

}
