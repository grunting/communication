package cn.gp.model;

import cn.gp.crypto.AES;
import cn.gp.crypto.RSA;
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

    // 服务器密码
    private static String serverKey;

    // 服务器加密对象
    private static AES aes;

    // 非对称加密实例(本机实例,也可以说是id)
    private static KeyPair keyPair;

    // 远端通道
    private static Channel channel;

    static {
        try {
            keyPair = RSA.genKeyPair(1024);
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

    // 记录同服务器的其他人
    private static final ConcurrentMap<String,Friend> friends = new ConcurrentHashMap<String, Friend>();

    public static ConcurrentMap<String, Friend> getFriends() {
        return friends;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        Basic.name = name;
    }

    public static String getServerKey() {
        return serverKey;
    }

    public static void setServerKey(String serverKey) {
        Basic.serverKey = serverKey;
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static AES getAes() {
        return aes;
    }

    public static void setAes(AES aes) {
        Basic.aes = aes;
    }
}
