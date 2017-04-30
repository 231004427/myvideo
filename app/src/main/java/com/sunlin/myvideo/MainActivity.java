package com.sunlin.myvideo;

import android.media.MediaCodecInfo;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.sunlin.myvideo.Media.MyMedia;
import com.sunlin.myvideo.Media.NV21Convertor;
import com.sunlin.myvideo.Media.Util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    SurfaceView surfaceView;
    Button btnSwitch;
    MyMedia myMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);

        //版本判断4：
        if(Build.VERSION.SDK_INT<16){
            Toast.makeText(this, "手机设备版本过低！", Toast.LENGTH_LONG).show();
            return;
        }
        //初始化视图
        surfaceView = (SurfaceView) findViewById(R.id.sv_surfaceview);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setFixedSize(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        int rotation=getWindowManager().getDefaultDisplay().getRotation();
        myMedia=new MyMedia(rotation);
        //初始化摄像头
        if (!myMedia.ctreateCamera(holder)){
            Toast.makeText(this, "初始化失败", Toast.LENGTH_LONG).show();
        }
        //初始化编码器
        if(!myMedia.initMediaCodec()){
            Toast.makeText(this, "编码器错误", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        myMedia.stopPreview();
        myMedia.destroyCamera();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                if (!myMedia.started) {
                    myMedia.startPreview();
                    btnSwitch.setText("停止");
                } else {
                    myMedia.stopPreview();
                    btnSwitch.setText("开始");
                }
                break;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myMedia.destroyCamera();
    }
}

