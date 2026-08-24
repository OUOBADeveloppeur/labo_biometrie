package com.hf.tr760.utils;

import android.content.Context;

import com.hftech.pos.manager.PrinterManager;
import com.hftech.pos.port.HiboryPort;

public class TR760Pos {
    private final String Version;
    private HiboryPort port;

    private TR760Pos() {
        this.Version = "V1.0.0 2023.07.27";
    }

    public static TR760Pos getInstance() {
        return TR760Pos.TR760PosBinder.pos;
    }

    public String getVersion() {
        return "V1.0.0 2023.07.27";
    }

    public int init(Context context) {
        this.port = new HiboryPort(105,27,"/dev/ttyS1", 115200);
//        this.port = new HiboryPort("/dev/ttyS1", 115200);
        this.port.open();
        return this.port.getState();
    }

    public boolean isInit() {
        if (this.port == null) {
            return false;
        } else {
            return this.port.getState() == 101;
        }
    }

    public HiboryPort getConnectPort() {
        return this.port;
    }

    public PrinterManager getPrinterManager() {
        return PrinterManager.getInstance();
    }

    public void release() {
        if (this.port != null) {
            this.port.close();
        }

    }

    private static class TR760PosBinder {
        static final TR760Pos pos = new TR760Pos();

        private TR760PosBinder() {
        }
    }
}