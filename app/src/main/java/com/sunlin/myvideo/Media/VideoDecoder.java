package com.sunlin.myvideo.Media;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.sunlin.myvideo.Comm.H264SPSPaser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sunlin on 2017/5/4.
 */

public class VideoDecoder {
    private String LOG_TAG="VideoDecoder";
    //处理音视频的编解码的类MediaCodec
    private MediaCodec video_decoder;
    //显示画面的Surface
    private Surface surface;
    // 0: live, 1: playback, 2: local file
    private int state = 0;
    //视频数据队列
    private BlockingQueue<byte[]> video_data_Queue = new ArrayBlockingQueue<byte[]>(10000);
    //音频数据队列
    private BlockingQueue<byte[]> audio_data_Queue = new ArrayBlockingQueue<byte[]>(10000);

    private boolean isReady = false;
    private int fps = 0;
    private boolean isInit=false;

    public Surface getSurface() {
        return surface;
    }

    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
    private int frameCount = 0;
    private long deltaTime = 0;
    private long counterTime = System.currentTimeMillis();
    public boolean isRuning = false;
    private Handler handler;

    public VideoDecoder(Surface surface, int playerState) {
        this.surface = surface;
        this.state = playerState;

    }
    public void start(){
        isRuning=true;
        isInit=false;
        runDecodeVideoThread();
    }
    public void stopRunning() {
        isRuning=false;
        isInit=false;
        video_data_Queue.clear();
        audio_data_Queue.clear();
    }

    //添加视频数据
    public void setVideoData(byte[] data) {

        try {
            video_data_Queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //添加音频数据
    public void setAudioData(byte[] data) {
        try {
            audio_data_Queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public int getFPS() {
        return fps;
    }


    public void initial(byte[] sps,byte[] pps,Handler _handler) throws IOException {

        if(isInit){return;}
        MediaFormat format = null;
        handler=_handler;
        //分析视频分辨率
        int seq_parameter_set_id=H264SPSPaser.ue(sps,64);
        int log2_max_frame_num_minus4=H264SPSPaser.ue(sps,-1);
        int pic_order_cnt_type=H264SPSPaser.ue(sps,-1);
        int log2_max_pic_order_cnt_lsb_minus4=H264SPSPaser.ue(sps,-1);
        int num_ref_frames=H264SPSPaser.ue(sps,-1);
        int gaps_in_frame_num_value_allowed_fla=H264SPSPaser.u(sps,1,H264SPSPaser.startBit);

        int width = (H264SPSPaser.ue(sps,H264SPSPaser.startBit+1) + 1)*16;
        int height = (H264SPSPaser.ue(sps,-1) + 1)*16;

            format = MediaFormat.createVideoFormat("video/avc", width, height);
            format.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
            format.setByteBuffer("csd-1", ByteBuffer.wrap(pps));

        if (video_decoder != null) {
            video_decoder.stop();
            video_decoder.release();
            video_decoder = null;
        }
        video_decoder = MediaCodec.createDecoderByType("video/avc");
        if (video_decoder == null) {
            return;
        }

        video_decoder.configure(format, surface, null, 0);
        video_decoder.start();
        inputBuffers = video_decoder.getInputBuffers();
        outputBuffers = video_decoder.getOutputBuffers();
        frameCount = 0;
        deltaTime = 0;
        isInit=true;
    }

    /**
     * @description 解码视频流数据
     * @author ldm
     * @time 2016/12/20
     */
    private void runDecodeVideoThread() {

        Thread t = new Thread() {

            @SuppressLint("NewApi")
            public void run() {

                while (isRuning) {

                    if(video_data_Queue.size()>300){
                        video_data_Queue.clear();
                    }
                    if (!video_data_Queue.isEmpty()){
                        int inIndex = -1;
                        try {
                            inIndex = video_decoder.dequeueInputBuffer(-1);
                        } catch (Exception e) {
                            return;
                        }
                        try {
                            if (inIndex >= 0) {
                                ByteBuffer buffer = inputBuffers[inIndex];
                                buffer.clear();
                                byte[] data;
                                data = video_data_Queue.take();
                                buffer.put(data);
                                if (state == 0) {
                                    video_decoder.queueInputBuffer(inIndex, 0, data.length, 0, 0);
                                } else {
                                    video_decoder.queueInputBuffer(inIndex, 0, data.length, 33, 0);
                                }
                            } else {
                                video_decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            }

                            int outIndex = video_decoder.dequeueOutputBuffer(info, 0);
                            switch (outIndex) {
                                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                                    outputBuffers = video_decoder.getOutputBuffers();
                                    break;
                                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                    isReady = true;
                                    break;
                                case MediaCodec.INFO_TRY_AGAIN_LATER:
                                    break;
                                default:

                                    video_decoder.releaseOutputBuffer(outIndex, true);
                                    frameCount++;
                                    deltaTime = System.currentTimeMillis() - counterTime;
                                    if (deltaTime > 1000) {
                                        fps = (int) (((float) frameCount / (float) deltaTime) * 1000);
                                        counterTime = System.currentTimeMillis();
                                        frameCount = 0;
                                    }
                                    break;
                            }
                            //所有流数据解码完成，可以进行关闭等操作
                            if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                isRuning = false;
                                Message message = new Message();
                                message.what = 1;
                                handler.sendMessage(message);
                                Log.e(LOG_TAG, "BUFFER_FLAG_END_OF_STREAM");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else
                    {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        };
        t.start();
    }
}