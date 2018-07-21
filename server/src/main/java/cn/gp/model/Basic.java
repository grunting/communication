package cn.gp.model;

import cn.gp.util.IndexTest;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础信息类
 * 设定:
 *
 * 1. 服务器有单独的交流密码
 * 2. 响应简单通信以及分组通信两种模式
 */
public class Basic {

    // 记录当前channelId和客户端的对应关系
    private static final IndexTest<ClientBean> index = new IndexTest<ClientBean>();

    // 分组id的分配
    private static final AtomicInteger integer = new AtomicInteger(1);

    // 分组的信息
    private static final ConcurrentHashMap<String,ConcurrentSet<Channel>> groups = new ConcurrentHashMap<String, ConcurrentSet<Channel>>();

    /**
     * 给出索引项
     * @return
     */
    public static IndexTest<ClientBean> getIndex() {
        return index;
    }
}
