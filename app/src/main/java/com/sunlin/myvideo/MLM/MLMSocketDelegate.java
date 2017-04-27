package com.sunlin.myvideo.MLM;

/**
 * Created by sunlin on 2017/4/25.
 */

public interface MLMSocketDelegate{
    /*error:-2=服务器断开,-3=服务器连接错误，-4=注册返回信息格式错误，
 *-12=用户已注册,-13=用户最大数,-11=系统错误
 *-23=房间最大数,-22=用户未注册,-24=已属于其他房间,-21=系统错误
 */
    public void  MLMSocketDidConnectError(Integer error,String str,Integer from,Integer to,MLMTCPClient sender);
    //房间创建完成
    public void MLMSocketDidRoom(Integer from,Integer to_room,MLMTCPClient sender);
    //退出房间
    public void MLMSocketDidRoomUserOut(Integer from,Integer to_room,MLMTCPClient sender);
    //邀请加入
    public void MLMSocketRoomRequest(Integer from,Integer to_room,MLMTCPClient sender);
    //拒绝加入
    public void MLMSocketRoomRefuse(Integer from,Integer to_room,MLMTCPClient sender);
    //进入房间
    public void MLMSocketDidRoomUserIn(Integer from,Integer to_room,MLMTCPClient sender);
    //删除房间
    public void MLMSocketRoomDel(Integer from,Integer to_room,MLMTCPClient sender);
    //用户成功登入
    public void MLMSocketDidConnect(MLMTCPClient sender);
    //数据传输
    public void MLMGetMessage(Integer from,Integer type,byte[] data,MLMTCPClient sender);
}