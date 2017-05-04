package com.sunlin.myvideo.Media;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sunlin on 2017/5/3.
 */

public class VideoFileReader {
    public ArrayList<Byte[]> streamBuffer;
    byte[] streamBuffer2;
    InputStream fileStream;
    byte[] startCode= new byte[]{0, 0, 0, 1};
    public int iframeNum;
    public VideoFileReader(){
    }

    public void openVideoFile(InputStream _fileStream) {
        streamBuffer = new ArrayList<Byte[]>();
        streamBuffer2 =new byte[480*320*2];
        fileStream = _fileStream;
        iframeNum = 0;
    }
    public void  decode(byte[] iframe,int size){

    }
    public byte[] readIframe(){
        int temp;
        int bytes = 0;
        try {
            while((temp=fileStream.read())!=-1){    //当没有读取完时，继续读取
                streamBuffer2[bytes]=(byte)temp;
                if(bytes>3)
                {
                    if(streamBuffer2[bytes-3]==startCode[0]&&
                    streamBuffer2[bytes-2]==startCode[1]&&
                    streamBuffer2[bytes-1]==startCode[2]&&
                    streamBuffer2[bytes]==startCode[3]){
                        //iframe
                        byte[] result;
                        if(iframeNum==0) {
                            result=new byte[bytes-3];
                            System.arraycopy(streamBuffer2, 0, result, 0, result.length);
                        }else {
                            result=new byte[bytes+1];
                            System.arraycopy(startCode, 0, result, 0, startCode.length);
                            System.arraycopy(streamBuffer2, 0, result,startCode.length, result.length - 4);
                        }
                        iframeNum++;
                        return result;
                    }
                }
                bytes++;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
