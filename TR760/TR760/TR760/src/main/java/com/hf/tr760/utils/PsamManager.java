package com.hf.tr760.utils;

import android.util.Log;

import com.hftech.pos.port.HiboryPort;

public class PsamManager {

    public  static final int PSAM_SLOT1 = 1;
    public  static final int PSAM_SLOT2 = 2;
    private static final String TAG = PsamManager.class.getSimpleName();
    private HiboryPort port;
    private PsamManager() {
        port = TR760Pos.getInstance().getConnectPort();
    }

    private static class PsamManagerBinder {
        final static PsamManager manager = new PsamManager();
    }

    public static PsamManager getInstance() {
        return PsamManagerBinder.manager;
    }

    private void printerEmptyInput(){
        byte[] data =  port.read();
        if(data!=null){
            Log.d(TAG,"printerEmptyInput:"+ Conversion.Bytes2HexString(data));
        }
    }

    public byte[] psamReset(int slot){
        printerEmptyInput();
        // 1B	23   23    50  53  41  4D
        byte[] cmds = new byte[]{0x1B,0x23,0x23,0x50,0x53,0x41,0x4D,(byte) slot,0x00};
        Log.d(TAG,"psamReset:"+Conversion.Bytes2HexString(cmds));
        port.sendByteData(cmds);
        try{
            Thread.sleep(500);
        }catch (Exception e){
            e.printStackTrace();
        }
        byte[] data = port.readMuit();
        if(data !=null){
            if(data[0] == 0x00){
                byte atr[] = new byte[data.length -1];
                System.arraycopy(data,1,atr,0,data.length-1);
                return atr;
            }else {
                Log.d(TAG,"psam reset fail");
            }
        }
        return null;
    }

    public byte[] psamApdu(int slot,byte[] apdu){
        printerEmptyInput();
        //1B 23  23  50 53 41 4D
        if(apdu == null || apdu.length == 0){
            return null;
        }
        byte cmds[] = new byte[apdu.length+9];
        cmds[0] = 0x1B;
        cmds[1] = 0x23;
        cmds[2] = 0x23;
        cmds[3] = 0x50;
        cmds[4] = 0x53;
        cmds[5] = 0x41;
        cmds[6] = 0x4D;
        cmds[7] = (byte) slot;
        cmds[8] = (byte)(apdu.length&0xFF);
        System.arraycopy(apdu,0,cmds,9,apdu.length);
        Log.d(TAG,"psamApdu:"+Conversion.Bytes2HexString(cmds));
        port.sendByteData(cmds);

//        try{
//            Thread.sleep(50);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        byte[] data = port.readMuit();
//        if(data!=null){
//            if(data.length == 1){
//                if(data[0] == 0x01){
//                    Log.e(TAG,"psam no reset");
//                }else if(data[0] == 0x02){
//                    Log.e(TAG,"apdu error");
//                }
//                return null;
//            }
//            return data;
//        }
        byte[] data = null;
        for(int i = 0;i<10;i++) {
            data = port.readMuit();
            if(data==null){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }
            if(data.length == 1){
                if(data[0] == 0x01){
                    Log.e(TAG,"psam no reset");
                }else if(data[0] == 0x02){
                    Log.e(TAG,"apdu error");
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                continue;
            }
            return data;
        }
        return data;
    }

    public byte[] psamClose(int slot){
        printerEmptyInput();
        //1B 23  23 50 4D 44 4E  N  N-01  02
        byte[] buzzerCommand = new byte[]{0x1B,0x23,0x23, 0x50,0x4D,0x44,0x4E,(byte) slot};
        port.sendByteData(buzzerCommand);
        byte[] data = port.read();
        return data;
    }
}
