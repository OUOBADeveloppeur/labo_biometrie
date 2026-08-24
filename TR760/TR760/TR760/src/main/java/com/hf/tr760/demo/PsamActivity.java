package com.hf.tr760.demo;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.hf.tr760.R;
import com.hf.tr760.utils.PsamManager;
import com.hf.tr760.utils.TR760Manager;
import com.hf.tr760.utils.TR760Pos;
import com.hf.ui.widget.EasyActionbar;
import com.hftech.pos.manager.Conversion;



public class PsamActivity extends AppCompatActivity implements View.OnClickListener {


    private PsamManager psamManager;
    private TextView show, status, cu;
    private EditText msg;
    private Button po1, po2,po3, cmd, pof;

    private RadioGroup group;

    private EasyActionbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TR760Manager.getInstance().PSAMPower(true);


        setContentView(R.layout.activity_psam);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        initViews();

        TR760Pos.getInstance().init(getApplicationContext());
        if(TR760Pos.getInstance().isInit()){
            psamManager = PsamManager.getInstance();
            Toast.makeText(PsamActivity.this, R.string.yesconn, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(PsamActivity.this, R.string.conn_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews(){
        actionbar = this.findViewById(R.id.actionbar);
        cu = (TextView)findViewById(R.id.textView_b);
        cu.setText("Current select card: None");
        show = (TextView)findViewById(R.id.textView_v);
        show.setText("");
        status = (TextView)findViewById(R.id.textView_s);
        status.setText("");
        msg = (EditText)findViewById(R.id.editText_msg);
        msg.setText("0700A4000002DF01");
        //msg.setText("80FA800060F534E74291D064A437757C0FE6B7D8861B97F4B3AB1E3565DBDAF89DA3F51262BD207BCC7838B8A6A245F4DF87D2BAFD3A2834BE06883A1FEE15769B4636486670FB625E3AFFBC156509C059818438EA8E8C47481ECCF5A0C6FADBC73D959AA4");

        po1 = (Button)findViewById(R.id.button_p1);
        po1.setOnClickListener(this);


        cmd = (Button)findViewById(R.id.button_cmd);
        cmd.setOnClickListener(this);
        cmd.setEnabled(false);
        pof = (Button)findViewById(R.id.button_off);
        pof.setOnClickListener(this);
        pof.setEnabled(false);

        group = findViewById(R.id.slot_group);

        actionbar.backTitleStyle(getString(R.string.app_name));
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method vstub
        if( view == po1)
        {
            byte res[] = psamManager.psamReset(Integer.parseInt(findViewById(group.getCheckedRadioButtonId()).getTag().toString()));// psam slot 1

            if(res == null)
            {
                status.setText("active card 1 failed, maybe card is not present or some error occured");
                return;
            }
            show.append("Atr:"+Conversion.Bytes2HexString(res)+"\n");
            status.setText("success get ATR");

            cmd.setEnabled(true);
            pof.setEnabled(true);
            cu.setText("Current select card: "+findViewById(group.getCheckedRadioButtonId()).getTag().toString());
            //test();

        }

        else if(view == cmd)
        {
            String mCmd = msg.getText().toString().trim();
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

            byte[] res = psamManager.psamApdu(
                    Integer.parseInt(findViewById(group.getCheckedRadioButtonId()).getTag().toString()),
                    mApdu);
            if(res == null)
            {
                status.setText("exec cmd failed, please retry");
                return;
            }

            show.append("Recv:"+Conversion.Bytes2HexString(res)+"\n");
            status.setText("success execute cmd");
            cu.setText("Current select card: "+findViewById(group.getCheckedRadioButtonId()).getTag().toString());

        }
        else if(view == pof)
        {
            if(!TR760Pos.getInstance().isInit()){
                Toast.makeText(PsamActivity.this,getString(R.string.conn_closed),Toast.LENGTH_SHORT).show();
                return;
            }
            psamManager.psamClose( Integer.parseInt(findViewById(group.getCheckedRadioButtonId()).getTag().toString()));// psam 卡下电
            status.setText("");
            show.setText("");
            cu.setText("Current select card: None");
            po1.setEnabled(true);
            cmd.setEnabled(false);
            pof.setEnabled(false);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        TR760Manager.getInstance().PSAMPower(false);
    }
}
