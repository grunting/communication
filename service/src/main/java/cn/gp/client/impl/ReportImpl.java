package cn.gp.client.impl;

import cn.gp.client.impl.GroupImpl;
import cn.gp.core.impl.SimpleBasic;
import cn.gp.crypto.RSA;
import cn.gp.model.Friend;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;
import cn.gp.util.IndexTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 实现上报功能
 */
public class ReportImpl implements Report {

    private static final Logger logger = LoggerFactory.getLogger(ReportImpl.class);

    private static volatile IndexTest<Friend> index = new IndexTest<Friend>();

    private SimpleBasic simpleBasic;

    public ReportImpl(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
    }

    public static IndexTest<Friend> getIndex() {
        return index;
    }

    /**
     * 丢失客户端公告
     * @param channelId 丢失的名称
     */
    public void lostClient(String channelId) {

        for (Friend friend : index.getNode("channelid",channelId)) {
            if (GroupImpl.passageWays.containsKey(friend.getName())) {
                GroupImpl.passageWays.remove(friend.getName());
            }
            friend.setDie();
            logger.info("friend lost,friend:{}",friend);
        }
    }

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 通信唯一id
     */
    public void findClient(String name,String channelId) {

        if (!simpleBasic.containsTrust(name)) {
            return;
        }

        Friend friend = new Friend(channelId,name,simpleBasic.getTrustPublicKey(name),index);
        index.setIndex("names",name,friend);
        index.setIndex("channelid",channelId,friend);

        logger.info("friend find,friend:{}",friend);
    }

    /**
     * 发送本客户端信息给服务器
     */
    public boolean send() {

        RegisterServer registerServer = simpleBasic.getRemoteProxyObj(RegisterServer.class);

        try {
            // 目前签名中只发布自身名字的签名(通道是加密的)
            byte[] crypto = RSA.encrypt(simpleBasic.getName().getBytes(),simpleBasic.getKeyPair().getPrivate());

            boolean b = false;
            if (registerServer != null) {
                b = registerServer.addClient(simpleBasic.getName(),crypto);
            }

            logger.info("friend report,result:{}",b);

            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取当前存活终端
     * @return 存活终端
     */
    public Set<Friend> getAllFriends() {
        return index.getAllNode();
    }

    /**
     * 丢失所有客户端
     */
    public void lostClientAll() {
        index = new IndexTest<Friend>();
    }
}
