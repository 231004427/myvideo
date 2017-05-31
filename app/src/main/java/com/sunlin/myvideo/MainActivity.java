package com.sunlin.myvideo;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.sunlin.myvideo.MLM.MLMSocketDelegate;
import com.sunlin.myvideo.MLM.MLMTCPClient;
import com.sunlin.myvideo.Media.VideoEncode;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener,MLMSocketDelegate {

    SurfaceView surfaceView;
    Button btnSwitch;
    Button btnSwitch2;
    VideoEncode myMedia;
    MLMTCPClient server;
    String TAG="MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);
        btnSwitch2 = (Button) findViewById(R.id.btn_switch2);
        btnSwitch2.setOnClickListener(this);
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
        myMedia=new VideoEncode(rotation);
        //初始化摄像头
        if (!myMedia.ctreateCamera(holder)){
            Toast.makeText(this, "初始化失败", Toast.LENGTH_LONG).show();
            return;
        }
        //初始化编码器
        if(!myMedia.initMediaCodec()){
            Toast.makeText(this, "编码器错误", Toast.LENGTH_LONG).show();
            myMedia.destroyCamera();
            return;
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
                    //链接服务器
                    myMedia.initServer("talk1",1,this);
                } else {
                    myMedia.closeServer();
                    myMedia.stopPreview();
                    btnSwitch.setText("开始");
                }
                break;
            case R.id.btn_switch2:
                Intent showActivity=new Intent(this,ShowActivity.class);
                this.startActivity(showActivity);

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (myMedia.started) {
            myMedia.stopPreview();
            btnSwitch.setText("开始");
            myMedia.closeServer();
            myMedia.destroyCamera();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        myMedia.destroyCamera();
    }

    @Override
    public void MLMSocketDidConnectError(int error, String str, long from, long to, MLMTCPClient sender) {
        Log.e(TAG,"服务器错误("+error+")");
        myMedia.closeServer();
        myMedia.stopPreview();
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                //更新UI
                Toast.makeText(MainActivity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                btnSwitch.setText("重连");
            }
        });

    }

    @Override
    public void MLMSocketDidRoom(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketDidRoomUserOut(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketRoomRequest(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketRoomRefuse(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketDidRoomUserIn(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketRoomDel(long from, int to_room, MLMTCPClient sender) {

    }

    @Override
    public void MLMSocketDidConnect(MLMTCPClient sender) {

        Log.e(TAG,"链接成功("+sender.userid+")");
        myMedia.startPreview();
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                //更新UI
                Toast.makeText(MainActivity.this, "链接成功", Toast.LENGTH_SHORT).show();
                btnSwitch.setText("关闭");
            }
        });

    }

    @Override
    public void MLMGetMessage(long from, int type, byte[] data, MLMTCPClient sender) {
        try {
            String res = new String(data,"UTF-8");
            Log.e(TAG,"From("+from+"):"+res);
            myMedia.server.sendByUserIdStr("Hello apple!",2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}

