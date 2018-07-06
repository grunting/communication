package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.service.RegisterServer;
import cn.gp.service.Report;
import com.google.protobuf.ByteString;

/**
 * 实现上报功能
 */
public class ReportImpl implements Report {

    /**
     * 丢失客户端公告
     * @param name 丢失的名称
     */
    public void lostClient(String name) {
        Basic.getFriends().remove(name);

        System.out.println(Basic.getFriends().keySet());
    }

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 通信唯一id
     * @param byteString 公钥
     */
    public void findClient(String name,String channelId,ByteString byteString) {

        Friend friend = new Friend(name,channelId,byteString);
        Basic.getFriends().put(name,friend);

        System.out.println(Basic.getFriends().keySet());

    }

    /**
     * 发送本客户端信息给服务器
     */
    public static void send() {

        byte[] cc = Basic.getKeyPair().getPublic().getEncoded();

        ByteString byteString = ByteString.copyFrom(cc);

        RegisterServer registerServer = Remote.getRemoteProxyObj(RegisterServer.class);
        boolean b = registerServer.addClient(Basic.getName(),byteString);
        System.out.println("上报的结果" + b);
    }
}
