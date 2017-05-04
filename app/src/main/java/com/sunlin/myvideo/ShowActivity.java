package com.sunlin.myvideo;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import com.sunlin.myvideo.Media.VideoDecode;
import com.sunlin.myvideo.Media.VideoDecoder;
import com.sunlin.myvideo.Media.VideoFileReader;
import com.sunlin.myvideo.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ShowActivity extends AppCompatActivity {
    private static String LOG_TAG="ShowActivity";
    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private Thread mDecodeThread;
    private MediaCodec mCodec;
    private boolean mStopFlag = false;
    private DataInputStream mInputStream;
    private String FileName = "test.h264";
    private int Video_Width = 480;
    private int Video_Height = 320;
    private int FrameRate = 15;
    private Boolean isUsePpsAndSps = false;
    private String filePath = Environment.getExternalStorageDirectory() + "/" + FileName;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Toast.makeText(ShowActivity.this, "播放结束!", Toast.LENGTH_LONG).show();
        }
    };

    byte[] header_sps;
    byte[] header_pps;
    private VideoDecoder videoDecoder;
    Surface surfaceShow=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        //保持屏幕常亮
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //

        mSurface = (SurfaceView) findViewById(R.id.sv_surfaceview);
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                //surfaceShow=holder.getSurface();
                videoDecoder=new VideoDecoder(holder.getSurface(),0);
                startDecodingThread();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }
    private void startDecodingThread() {
        mDecodeThread = new Thread(new decodeH264Thread());
        mDecodeThread.start();
    }

    /**
     * @author ldm
     * @description 解码线程
     * @time 2016/12/19 16:36
     */
    private class decodeH264Thread implements Runnable {
        @Override
        public void run() {
            try {
                decodeLoop();
            } catch (Exception e) {
            }
        }

        private void decodeLoop() {
            //获取每一帧数据
            VideoFileReader videoReader=new VideoFileReader();
            videoReader.openVideoFile(getResources().openRawResource(R.raw.test));
            while(true){
                byte[] iframe=videoReader.readIframe();
                if(iframe==null)break;
                int nalType = iframe[4] & 0x1F;
                switch(nalType){
                    case 0x05:
                        Log.d(LOG_TAG,"Nal type is IDR frame");
                        try {
                            videoDecoder.initial(header_sps,header_pps);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        videoDecoder.setVideoData(iframe);
                        break;
                    case 0x07:
                        Log.d(LOG_TAG,"Nal type is SPS");
                        header_sps=iframe;
                        break;
                    case 0x08:
                        Log.d(LOG_TAG,"Nal type is PPS");
                        header_pps=iframe;
                        break;
                    default:
                        Log.d(LOG_TAG,"Nal type is B/P frame");
                        videoDecoder.setVideoData(iframe);
                        break;
                }
                //
            }
        }
    }
}