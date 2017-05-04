package com.sunlin.myvideo.MLM;

import android.os.Bundle;
import android.os.Message;
import android.util.JsonReader;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sunlin on 2017/4/25.
 */

public class MLMTCPClient{

    private String host;
    private int port;
    private Socket client;
    private OutputStream output;
    private InputStream input;

    private String tag;
    private int userid;
    private int groupid;

    private MLMlib mlib=new MLMlib();

    public MLMSocketDelegate delegate;

    public MLMTCPClient(String _host,int _port,String _tag,int _userid) {
        host = _host;
        port = _port;
        tag = _tag;
        userid =_userid;
    }
    public MLMTCPClient(String _tag,int _userid){
        host = "192.168.2.122";//"192.168.43.149"
        port = 8888;
        tag = _tag;
        userid=_userid;
    }
    //关闭链接
    public void close(){
        try {
            if(output!=null)
            output.close();
            if(input!=null)
            input.close();
            if(client!=null) {
                client.close();
            }
            groupid=0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connectServer(){
        if(client==null){
            new Thread() {
                @Override
                public void run() {
                    try {
                        client=new Socket();
                        SocketAddress address=new InetSocketAddress(host,port);
                        client.connect(address,1500);
                        client.setTcpNoDelay(true);
                        output=client.getOutputStream();
                        input=client.getInputStream();
                        //用户注册
                        _sendMessage(output,"1",10,userid,0);
                        //监听返回数据
                        readMain(input);

                    }  catch (UnknownHostException e) {
                        e.printStackTrace();
                        close();
                        delegate.MLMSocketDidConnectError(-3,"服务无响应(-3)",0,0,MLMTCPClient.this);
                    }catch (IOException e) {
                        e.printStackTrace();
                        close();
                        delegate.MLMSocketDidConnectError(-3,"服务无响应(-3)",0,0,MLMTCPClient.this);
                    }
                }
            }.start();
        }
    }
    private void readMain(InputStream input){
        MyHead rec_head=new MyHead();
        int j=0,z=0;
        int head_size=MyHead.size;
        rec_head.l=0;
        rec_head.from=0;
        rec_head.to=0;
        rec_head.t=0;
        rec_head.v=0;
        byte[] data_buf=new byte[500000];
        byte[] response=new byte[1024*10];

        while (true){

            try {
                int rec=input.read(response);
                if(rec<0){
                    delegate.MLMSocketDidConnectError(-2,"服务器断开(-2)",0,0,MLMTCPClient.this);
                    break;
                }
                for(int i=0;i<rec;i++){

                    if(rec_head.l==0){
                        data_buf[j]=response[i];
                        j+=1;
                        if(j==head_size){
                            if(mlib.getDataHead(data_buf,rec_head)< 0){break;}
                            //如果数据为空
                            if(rec_head.l==0){
                                //显示信息
                                _showMessage(rec_head.from,rec_head.to,rec_head.t,null);
                                //重置
                                rec_head.l = 0;
                                rec_head.from=0;
                                rec_head.t=0;
                                rec_head.to=0;
                                rec_head.v=0;
                                z=0;
                                j=0;
                            }
                        }
                    }else {
                        data_buf[j]=response[i];
                        j += 1;
                        z += 1;
                        if(z == rec_head.l)
                        {
                            //收取包完成
                            //显示信息
                            _showMessage(rec_head.from,rec_head.to,rec_head.t,Arrays.copyOfRange(data_buf,head_size,j));
                            //重置
                            rec_head.l = 0;
                            rec_head.from=0;
                            rec_head.t=0;
                            rec_head.to=0;
                            rec_head.v=0;
                            z=0;
                            j=0;

                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

    }
    //处理消息
    private void _showMessage(long from,long to,int type,byte[] data){
        if(60<=type && type<80 ){//接收数据
            delegate.MLMGetMessage(from,type,data,MLMTCPClient.this);
        }else if(type==50)//邀请加入
        {


        }else if(type==90)//拒绝加入
        {
        }
        else if(type==40){//处理通知消息

            //message
            String message = new String(data,Charset.forName("utf-8"));
            try{
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(message);
            JsonObject element = root.getAsJsonObject();

            JsonPrimitive nJson = element.getAsJsonPrimitive("n");
            JsonPrimitive tJson = element.getAsJsonPrimitive("t");
            JsonPrimitive cJson = element.getAsJsonPrimitive("c");

            if (nJson == null || tJson == null || cJson == null) {
                //信息格式错误
                delegate.MLMSocketDidConnectError(-4,"服务器错误(-4)",from,to,MLMTCPClient.this);
                return;
            }
            int ms_type = tJson.getAsInt();
            int num=nJson.getAsInt();
            if(num==1)
            {   //print("regist sucess \(json["c"].intValue)")
                if(ms_type==20){
                //创建房间成功
                groupid=cJson.getAsInt();
                    delegate.MLMSocketDidRoom(from,(int)to,MLMTCPClient.this);
                }
                else if(ms_type==10)
                {
                    //用户登录成功
                    delegate.MLMSocketDidConnect(MLMTCPClient.this);
                }
                else if(ms_type==30)
                {
                    //退出房间成功
                    delegate.MLMSocketDidRoomUserOut(from,(int)to,MLMTCPClient.this);
                    groupid=0;
                }
                else if(ms_type==80)
                {
                    //加入房间成功
                    delegate.MLMSocketDidRoomUserIn(from,(int)to,MLMTCPClient.this);

                }else if(ms_type==100)
                {
                    //删除房间成功
                    delegate.MLMSocketRoomDel(from,(int)to,MLMTCPClient.this);
                }
            }else{
                delegate.MLMSocketDidConnectError(num,"服务器错误("+num+")",from,to, MLMTCPClient.this);
            }
            }catch (Exception ex){
                ex.printStackTrace();
                delegate.MLMSocketDidConnectError(-4,"服务器错误(-4)",from,to,MLMTCPClient.this);
                return;
            }

        }

    }
    public boolean _sendMessage(OutputStream output,String content,int type,int from,int to){

        byte[] rawData=content.getBytes();
        byte[] buff = mlib.buildData(type,from,to,rawData,rawData.length);
        try {
            output.write(buff,0,buff.length);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
            close();
            delegate.MLMSocketDidConnectError(-3,"发送失败(-3)",0,0,this);
        }

        return  true;
    }
}
