package cn.gp.model;

import cn.gp.crypto.AES;
import cn.gp.proto.Data;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础信息类
 * 设定:
 *
 * 1. 服务器有单独的交流密码
 * 2. 响应简单通信以及分组通信两种模式
 */
public class Basic {

    // 服务器密码
    private static String key;

    // 对称加解密
    private static AES aes;

    // 记录当前channelId和客户端的对应关系
    private static final ConcurrentHashMap<String,ClientBean> channelMap = new ConcurrentHashMap<String, ClientBean>();

    // 分组id的分配
    private static final AtomicInteger integer = new AtomicInteger(1);

    // 分组的信息
    private static final ConcurrentHashMap<String,ConcurrentSet<Channel>> groups = new ConcurrentHashMap<String, ConcurrentSet<Channel>>();

    public static ConcurrentHashMap<String,ClientBean> getChannelMap() {
        return channelMap;
    }

    public static String getKey() {
        return key;
    }

    public static void setKey(String key) {
        Basic.key = key;
    }

    public static AES getAes() {
        return aes;
    }

    public static void setAes(AES aes) {
        Basic.aes = aes;
    }
}
