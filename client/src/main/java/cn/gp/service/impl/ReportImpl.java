package cn.gp.service.impl;

import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;

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
            if (GroupImpl.passageWays.containsKey(friend.getName())) {
                GroupImpl.passageWays.remove(friend.getName());
            }
            friend.setDie();
        }
    }

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 通信唯一id
     */
    public void findClient(String name,String channelId) {

        // 不在可信列表的,拒绝添加到本地(可信的也应该验证一下,暂时忽略)
        if (!Basic.getTrustMap().containsKey(name)) {
            return;
        }

        Friend friend = new Friend(channelId,name,Basic.getTrustMap().get(name));

        Basic.getIndexTest().setIndex("names",name,friend);
        Basic.getIndexTest().setIndex("channelid",channelId,friend);

        System.out.println();
        System.out.println(Basic.getIndexTest().getAllNode());
        System.out.print("order:");

    }

    /**
     * 发送本客户端信息给服务器
     */
    public void send() throws Exception {

        RegisterServer registerServer = Remote.getRemoteProxyObj(RegisterServer.class);

        // 目前签名中只发布自身名字的签名(通道是加密的)
        byte[] crypto = RSA.encrypt(Basic.getName().getBytes(),Basic.getKeyPair().getPrivate());

        boolean b = registerServer.addClient(Basic.getName(),crypto);
        if(!b) {
            System.out.println("服务器拒绝了本节点的注册");
            System.exit(2);
        }
    }
}
