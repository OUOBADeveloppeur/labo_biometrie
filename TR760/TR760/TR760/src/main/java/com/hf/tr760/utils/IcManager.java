package com.hf.tr760.utils;

import android.util.Log;
import com.hftech.pos.port.HiboryPort;

public class IcManager {
    private static final String TAG = IcManager.class.getSimpleName();
    private HiboryPort port;
    private IcManager() {
        port = TR760Pos.getInstance().getConnectPort();
    }

    private static class IcManagerBinder {
        final static IcManager manager = new IcManager();
    }

    public static IcManager getInstance() {
        return IcManagerBinder.manager;
    }

    private void printerEmptyInput(){
        byte[] data =  port.read();
        if(data!=null){
            Log.d(TAG,"printerEmptyInput:"+ Conversion.Bytes2HexString(data));
        }
    }

    public byte[] icApdu(byte[] apdu){
        printerEmptyInput();
        //byte[] cmds = new byte[]{0x1B,0x23,0x23,0x43,0x41,0x52,0x44,0x00};
        if(apdu == null){
            return null;
        }

        byte cmds[] = new byte[apdu.length+8];
        cmds[0] = 0x1B;
        cmds[1] = 0x23;
        cmds[2] = 0x23;
        cmds[3] = 0x43;
        cmds[4] = 0x41;
        cmds[5] = 0x52;
        cmds[6] = 0x44;
        cmds[7] = (byte)(apdu.length&0xFF);
        System.arraycopy(apdu,0,cmds,8,apdu.length);
        Log.d(TAG,"psamApdu:"+Conversion.Bytes2HexString(cmds));
        port.sendByteData(cmds);
        byte[] data = null;
        for(int i = 0;i<10;i++) {
            data = port.readMuit();
            if (data == null) {
                Log.d(TAG, "IcRecv: null");
            }else {
                Log.d(TAG, "IcRecv:" + Conversion.Bytes2HexString(data));
                return data;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        return data;
    }

    public int icWrite(byte[] apdu){
        printerEmptyInput();
        //byte[] cmds = new byte[]{0x1B,0x23,0x23,0x43,0x41,0x52,0x44,0x00};
        if(apdu == null){
            return 0;
        }

        byte cmds[] = new byte[apdu.length+8];
        cmds[0] = 0x1B;
        cmds[1] = 0x23;
        cmds[2] = 0x23;
        cmds[3] = 0x43;
        cmds[4] = 0x41;
        cmds[5] = 0x52;
        cmds[6] = 0x44;
        cmds[7] = (byte)(apdu.length&0xFF);
        System.arraycopy(apdu,0,cmds,8,apdu.length);
        Log.d(TAG,"psamApdu:"+Conversion.Bytes2HexString(cmds));
        port.sendByteData(cmds);
        int code = 0;
        for(int i = 0;i<10;i++) {
            code = port.write(cmds);
            if (code == 0) {
                Log.d(TAG, "IcRecv: 0");
            }else {
                Log.d(TAG, "IcRecv:" + code);
                return code;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        return code;
    }

    public void release(){
        //IcManagerBinder.manager = null;
    }
}
