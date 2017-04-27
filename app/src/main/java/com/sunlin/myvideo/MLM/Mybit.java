package com.sunlin.myvideo.MLM;

/**
 * Created by sunlin on 2017/4/26.
 */

public class Mybit {
    Mybit() {

        load();
    }

    private void load() {
        try {
            System.loadLibrary("native-lib");
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public native String stringFromJNI();
    public native int buildData(byte[] buffer,int t,int from,int to,byte[] data);
}
