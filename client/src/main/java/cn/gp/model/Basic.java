package cn.gp.model;

import cn.gp.crypto.AES;
import cn.gp.crypto.JksTool;
import cn.gp.util.IndexTest;
import io.netty.channel.Channel;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基本信息集合
 */
public class Basic {

    // 客户端名称
    private static String name;

    // 非对称加密实例(本机实例,也可以说是id)
    private static KeyPair keyPair;

    // 远端通道
    private static Channel channel;

    // 可信列表
    private static Map<String,PublicKey> trustMap;

    // 朋友
    private static IndexTest<Friend> indexTest = new IndexTest<Friend>();

    // 分组
    private static ConcurrentMap<String, AES> groups = new ConcurrentHashMap<String, AES>();

    // 构建单人传输的中间信息
    private static ConcurrentMap<String,Integer> sendMessageBefore = new ConcurrentHashMap<String, Integer>();

    // 测试单人传输
    private static ConcurrentMap<String, AES> sendMessage = new ConcurrentHashMap<String, AES>();

    static {
        try {
            keyPair = JksTool.getKeyPair();
            name = JksTool.getAlias();
            trustMap = JksTool.getTrustMap();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("无法生成自身秘钥");
            System.exit(1);
        }
    }

    public static Channel getChannel() {
        return channel;
    }

    public synchronized static void setChannel(Channel channel) {
        if (Basic.channel == null) {
            Basic.channel = channel;
        }
    }

    public static String getName() {
        return name;
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static IndexTest<Friend> getIndexTest() {
        return indexTest;
    }

    public static ConcurrentMap<String, AES> getGroups() {
        return groups;
    }


    public static ConcurrentMap<String, AES> getSendMessage() {
        return sendMessage;
    }

    public static ConcurrentMap<String,Integer> getSendMessageBefore() {
        return sendMessageBefore;
    }


    public static Map<String, PublicKey> getTrustMap() {
        return trustMap;
    }
}
