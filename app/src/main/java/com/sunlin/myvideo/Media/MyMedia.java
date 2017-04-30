package com.sunlin.myvideo.Media;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sunlin on 2017/4/30.
 */

public class MyMedia implements Camera.PreviewCallback{

    Camera mCamera;
    int mCameraId;
    NV21Convertor mConvertor;
    public boolean started;
    String path;
    String TAG="MyMedia";
    int width = 0, height = 0;
    int framerate, bitrate;
    MediaCodec mMediaCodec;
    int rotation;

    public MyMedia(int _rotation){
        path=Environment.getExternalStorageDirectory() + "/easy.h264";
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        started = false;
        rotation=_rotation;
        width=0;
        height=0;
    }
    //初始化编码器
    public boolean initMediaCodec() {

        int dgree = getDgree();
        int colorFormat= MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
        //帧率
        framerate = 15;
        //码率
        bitrate = 2 * width * height * framerate / 20;
        mConvertor = new NV21Convertor();
        mConvertor.setSize(width,height);
        mConvertor.setEncoderColorFormat(colorFormat);

        try {
            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
            MediaFormat mediaFormat;
            if (dgree == 0) {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", height, width);
            } else {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            }
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,colorFormat);
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    //初始化摄像头
    public boolean ctreateCamera(SurfaceHolder surfaceHolder) {
        try {
            mCamera = Camera.open(mCameraId);
            mCamera.setPreviewCallback(this);
            Camera.Parameters parameters = mCamera.getParameters();

            int[] max = determineMaximumSupportedFramerate(parameters);

            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;

            int rotate = (360 + cameraRotationOffset - getDgree()) % 360;
            parameters.setRotation(rotate);

            parameters.setPreviewFormat(ImageFormat.NV21);
            //设置摄像分辨率
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : sizes
                    ) {
                if (size.width >= 480 && size.width <= 640) {
                    width = size.width;
                    height= size.height;
                    Log.i(TAG, String.format("find preview size width=%d,height=%d",width,
                            height));
                    break;
                }
            }
            if(width==0){return  false;}
            parameters.setPreviewSize(width, height);
            //设置预览帧数FPS
            parameters.setPreviewFpsRange(max[0], max[1]);
            mCamera.setParameters(parameters);

            mCamera.autoFocus(null);
            //显示角度
            int displayRotation;
            displayRotation = (cameraRotationOffset - getDgree() + 360) % 360;
            mCamera.setDisplayOrientation(displayRotation);

            mCamera.setPreviewDisplay(surfaceHolder);
            return true;
        } catch (Exception e) {
            //StringWriter sw = new StringWriter();
            //PrintWriter pw = new PrintWriter(sw);
            //e.printStackTrace(pw);
            //String stack = sw.toString();
            //Toast.makeText(this, stack, Toast.LENGTH_LONG).show();
            destroyCamera();
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 开启摄像
     */
    public synchronized void startPreview() {
        if (mCamera != null && !started) {
            mCamera.startPreview();
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height
                    * ImageFormat.getBitsPerPixel(previewFormat)
                    / 8;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(this);
            started = true;
        }
    }
    /**
     * 销毁Camera
     */
    public synchronized void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {

            }
            mCamera = null;
        }
        if(mMediaCodec!=null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
    /**
     * 停止摄像
     */
    public synchronized void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            started = false;
        }
    }
    private int getDgree() {

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }
    //获取摄像头支持FPS
    public static int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        return maxFps;
    }
    byte[] mPpsSps = new byte[0];
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

            if (data == null) {
                return;
            }
            ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
            byte[] dst = new byte[data.length];
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            if (getDgree() == 0) {
                dst = Util.rotateNV21Degree90(data, previewSize.width, previewSize.height);
            } else {
                dst = data;
            }
            try {
                int bufferIndex = mMediaCodec.dequeueInputBuffer(5000000);
                if (bufferIndex >= 0) {
                    inputBuffers[bufferIndex].clear();

                    mConvertor.convert(dst, inputBuffers[bufferIndex]);

                    mMediaCodec.queueInputBuffer(bufferIndex, 0,
                            inputBuffers[bufferIndex].position(),
                            System.nanoTime() / 1000, 0);
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                        byte[] outData = new byte[bufferInfo.size];
                        outputBuffer.get(outData);
                        //记录pps和sps
                        if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 103) {
                            mPpsSps = outData;
                        } else if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 101) {
                            //在关键帧前面加上pps和sps数据
                            byte[] iframeData = new byte[mPpsSps.length + outData.length];
                            System.arraycopy(mPpsSps, 0, iframeData, 0, mPpsSps.length);
                            System.arraycopy(outData, 0, iframeData, mPpsSps.length, outData.length);
                            outData = iframeData;
                        }
                        Util.save(outData, 0, outData.length, path, true);
                        mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                        outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                } else {
                    Log.e("easypusher", "No buffer available !");
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stack = sw.toString();
                Log.e("save_log", stack);
                e.printStackTrace();
            } finally {
                mCamera.addCallbackBuffer(dst);
            }
    }
}
