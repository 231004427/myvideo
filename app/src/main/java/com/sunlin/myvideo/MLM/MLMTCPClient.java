package com.sunlin.myvideo.MLM;

import java.net.Socket;

/**
 * Created by sunlin on 2017/4/25.
 */

public class MLMTCPClient{

    private String host;
    private Integer port;
    private Socket client;

    private String tag;
    private Integer userid;
    private Integer groupid;

    public MLMSocketDelegate delegate;

    public MLMTCPClient(){

    }

}
