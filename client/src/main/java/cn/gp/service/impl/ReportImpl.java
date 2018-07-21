package cn.gp.service.impl;

import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.service.RegisterServer;
import cn.gp.service.Report;
import com.google.protobuf.ByteString;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * 实现上报功能
 */
public class ReportImpl implements Report {

    /**
     * 丢失客户端公告
     * @param channelId 丢失的名称
     */
    public void lostClient(String channelId) {

        for (Friend friend : Basic.getIndexTest().getNode("channelid",channelId)) {
            friend.setDie();
        }

        System.out.println();
        System.out.println(Basic.getIndexTest().getAllNode());
        System.out.print("order:");
    }

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 通信唯一id
     * @param publicKey 公钥
     */
    public void findClient(String name,String channelId,PublicKey publicKey) {

        Friend friend = new Friend(channelId,name,publicKey);

        Basic.getIndexTest().setIndex("name",name,friend);
        Basic.getIndexTest().setIndex("channelid",channelId,friend);

        System.out.println();
        System.out.println(Basic.getIndexTest().getAllNode());
        System.out.print("order:");

    }

    /**
     * 发送本客户端信息给服务器
     */
    public static void send() throws Exception {

        RegisterServer registerServer = Remote.getRemoteProxyObj(RegisterServer.class);

        byte[] crypto = RSA.encrypt(Basic.getName().getBytes(),Basic.getKeyPair().getPrivate());

        boolean b = registerServer.addClient(Basic.getName(),Basic.getKeyPair().getPublic(), crypto);

        System.out.println();
        System.out.println("上报的结果" + b);
    }
}
