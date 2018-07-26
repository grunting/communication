package cn.gp.model;

import cn.gp.crypto.JksTool;
import cn.gp.util.IndexTest;
import io.netty.channel.Channel;

import java.security.KeyPair;
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

    // 朋友
    private static IndexTest<Friend> indexTest = new IndexTest<Friend>();

    // 分组
    private static ConcurrentMap<String,byte[]> groups = new ConcurrentHashMap<String, byte[]>();

    static {
        try {
            keyPair = JksTool.getKeyPair();
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("无法生成自身秘钥");
            System.exit(1);
        }
    }

    public static Channel getChannel() {
        return channel;
    }

    public static void setChannel(Channel channel) {
        Basic.channel = channel;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        Basic.name = name;
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static IndexTest<Friend> getIndexTest() {
        return indexTest;
    }

    public static ConcurrentMap<String, byte[]> getGroups() {
        return groups;
    }
}
