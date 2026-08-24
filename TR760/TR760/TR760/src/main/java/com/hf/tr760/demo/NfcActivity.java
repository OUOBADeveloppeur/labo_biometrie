package com.hf.tr760.demo;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;

import com.hf.tr760.databinding.ActivityNfcBinding;
import com.hf.tr760.utils.Conversion;
import com.hf.tr760.utils.NFCUtils;
import com.hf.ui.base.EasyActivity;
import com.hf.ui.base.EasyEvent;

public class NfcActivity extends EasyActivity {

    ActivityNfcBinding nfcBinding;

    //NFC
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (nfcBinding.tvRead != null) {

            String data = NFCUtils.processNfcIntent(intent);
            nfcBinding.tvRead.setText(""+data);


            //获取ID
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String id = Conversion.Bytes2HexString(tag.getId());
        }


    }


    @Override
    public void onMessageRecieve(String name, EasyEvent msg) {

    }

    @Override
    public void create() {
        nfcBinding = ActivityNfcBinding.inflate(getLayoutInflater());
        setContentView(nfcBinding.getRoot());

        nfcBinding.actionbar.backTitleStyle("NFC");

    }

    @Override
    public void restart() {

    }

    @Override
    public void onstart() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);//设备的NfcAdapter对象
        if (mNfcAdapter == null) {//判断设备是否支持NFC功能
            showToast("设备不支持NFC功能!");
            finish();
            return;
        }
        if (!mNfcAdapter.isEnabled()) {//判断设备NFC功能是否打开
            showToast("请到系统设置中打开NFC功能!");
            finish();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);//创建PendingIntent对象,当检测到一个Tag标签就会执行此Intent
    }

    @Override
    public void resume() {
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null,
                null);
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
