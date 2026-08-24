package com.hf.tr760.demo;


import android.hibory.CommonApi;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hf.tr760.R;
import com.hf.tr760.utils.IcManager;
import com.hf.tr760.utils.TR760Manager;
import com.hf.tr760.utils.TR760Pos;
import com.hf.ui.widget.EasyActionbar;
import com.hftech.pos.manager.Conversion;


public class IcActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView show, status, cu;
//    private Button reset,b95,b96,b805c;

    private Button binaryReset,readBinary,updateBinary;
    private EasyActionbar actionbar;

    private IcManager icManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ic);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        TR760Manager.getInstance().PSAMPower(true);

        initViews();

        TR760Pos.getInstance().init(getApplicationContext());
        if(TR760Pos.getInstance().isInit()){
            icManager = IcManager.getInstance();
//            CommonApi commonApi = new CommonApi();
//            commonApi.setGpioDir(27,1);
//            commonApi.setGpioOut(27,1);
            Toast.makeText(IcActivity.this, R.string.yesconn, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(IcActivity.this, R.string.conn_failed, Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TR760Manager.getInstance().PSAMPower(false);
    }

    private void initViews(){
        actionbar = this.findViewById(R.id.actionbar);
        cu = (TextView)findViewById(R.id.textView_b);
        cu.setText("Current select card: None");
        show = (TextView)findViewById(R.id.textView_v);
        show.setText("");
        status = (TextView)findViewById(R.id.textView_s);
        status.setText("");

//        reset = (Button)findViewById(R.id.button_reset);
//        reset.setOnClickListener(this);
//        reset.setEnabled(true);
//
//        b95 = (Button)findViewById(R.id.button_95);
//        b95.setOnClickListener(this);
//        b95.setEnabled(true);
//
//        b96 = (Button)findViewById(R.id.button_96);
//        b96.setOnClickListener(this);
//        b96.setEnabled(true);
//
//        b805c = (Button)findViewById(R.id.button_805c);
//        b805c.setOnClickListener(this);
//        b805c.setEnabled(true);

        binaryReset = (Button)findViewById(R.id.binaryReset);
        binaryReset.setOnClickListener(this);
        binaryReset.setEnabled(true);

        readBinary = (Button)findViewById(R.id.readBinary);
        readBinary.setOnClickListener(this);
        readBinary.setEnabled(true);

        updateBinary = (Button)findViewById(R.id.updateBinary);
        updateBinary.setOnClickListener(this);
        updateBinary.setEnabled(true);

        actionbar.backTitleStyle(getString(R.string.app_name));
    }

    @Override
    public void onClick(View view) {
        String mCmd = null;
//        if(view == reset) {
//            mCmd = "";
//        }else if(view==b95){
//            mCmd ="00B0950000";
//        }else if(view==b96){
//            mCmd ="00B0960000";
//        }else if(view==b805c){
//            mCmd ="805C000204";
//        }
        if(view==binaryReset){
            mCmd ="";
        }
        else if(view==readBinary){
            mCmd ="00B09B0010";
        }
        else if(view==updateBinary){
            mCmd ="00D69B0010"+"00112233445566778899AABBCCDDEEFF";
            int code = icManager.icWrite(Conversion.HexString2Bytes(mCmd));

            status.setText("ic write code:"+code);
            show.append("write code:"+code+"\n");
            status.setText("success execute cmd");
            return;
        }
        if(mCmd==null) return;
        byte [] mApdu = null;
        try{
            mApdu = Conversion.HexString2Bytes(mCmd);
        }catch(Exception e){
            e.printStackTrace();
            status.setText("Invalid char, cmd shoud formed by HEX number");
        }

        if(mApdu==null){
            status.setText("Invalid mApdu, cmd shoud formed by HEX number");
            return ;
        }

        byte[] res = icManager.icApdu(mApdu);
        if(res == null)
        {
            status.setText("exec cmd failed, please retry");
            return;
        }

        show.append("Recv:"+Conversion.Bytes2HexString(res)+"\n");
        status.setText("success execute cmd");
    }
}
