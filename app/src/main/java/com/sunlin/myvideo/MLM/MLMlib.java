package com.sunlin.myvideo.MLM;

/**
 * Created by sunlin on 2017/4/26.
 */

public class MLMlib {
    MLMlib() {
        //load();
    }

    private void load() {
        try {
            System.loadLibrary("native-lib");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public native String stringFromJNI();
    //public native int buildData(byte[] buffer,int t,long from,long to,byte[] data);
    public int buildData(byte[] buff,int t,long from,long to ,byte[] data,int data_size){
        buff[0]=1;
        buff[1]=(byte)t;

        buff[2]=0;
        buff[3]=0;

        buff[4]=(byte)((data_size>>>24)&0xFF);
        buff[5]=(byte)((data_size>>>16)&0xFF);
        buff[6]=(byte)((data_size>>>8)&0xFF);
        buff[7]=(byte)((data_size>>>0)&0xFF);

        buff[8]=(byte)((from>>>24)&0xFF);
        buff[9]=(byte)((from>>>16)&0xFF);
        buff[10]=(byte)((from>>>8)&0xFF);
        buff[11]=(byte)((from>>>0)&0xFF);

        buff[12]=(byte)((to>>>24)&0xFF);
        buff[13]=(byte)((to>>>16)&0xFF);
        buff[14]=(byte)((to>>>8)&0xFF);
        buff[15]=(byte)((to>>>0)&0xFF);
        System.arraycopy(data, 0, buff,MyHead.size, data_size);
        return data_size+MyHead.size;
    }
    /*
    uint8_t v;
    uint8_t t;
    uint16_t d;
    uint32_t l;
    uint32_t from;
    uint32_t to;
     */
    public int getDataHead(byte[] src_data,MyHead head){

        head.v=src_data[0]& 0xFF;
        head.t=src_data[1]& 0xFF;
        head.d=byteToint_u16(src_data,2);
        head.l=byteToint_u32(src_data,4);
        head.from=byteTolong_u32(src_data,8);
        head.to=byteTolong_u32(src_data,12);
        return 1;
    }
    public int byteToint_u16(byte[] b,int offset) {
        return   b[offset+1] & 0xFF |
                (b[offset] & 0xFF) << 8;
    }
    public int byteToint_u32(byte[] b,int offset) {
        return   b[offset+3] & 0xFF |
                (b[offset+2] & 0xFF) << 8 |
                (b[offset+1] & 0xFF) << 16 |
                (b[offset] & 0xFF) << 24;
    }
    public long byteTolong_u32(byte[] b,int offset) {
        return   b[offset+3] & 0xFF |
                (b[offset+2] & 0xFF) << 8 |
                (b[offset+1] & 0xFF) << 16 |
                (b[offset] & 0xFF) << 24;
    }
}
