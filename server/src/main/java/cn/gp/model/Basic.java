package cn.gp.model;

import cn.gp.util.IndexTest;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础信息类
 */
public class Basic {

    // 记录当前channel和客户端的对应关系
    private static final IndexTest<ClientBean> index = new IndexTest<ClientBean>();


    /**
     * 给出索引项
     * @return
     */
    public static IndexTest<ClientBean> getIndex() {
        return index;
    }
}
