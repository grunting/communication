package cn.gp.model;

import cn.gp.service.IsAlive;
import io.netty.util.internal.ConcurrentSet;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同服务器的其他客户端信息
 */
public class Friend<K> implements IsAlive<K> {

    // 服务端记录的通道id
    private String channelId;

    // 公开名称
    private String name;

    // 公钥(类似于身份证)
    private PublicKey key;

    // 是否存活
    private AtomicBoolean isAlive = new AtomicBoolean(true);

    // 属性值
    private Map<String,String> properties = new HashMap<String, String>();

    // 索引根
    private ConcurrentMap<String,ConcurrentMap<String,ConcurrentSet<K>>> index;

    public Friend(String channelId, String name, PublicKey key) {
        this.channelId = channelId;
        this.name = name;
        this.key = key;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getName() {
        return name;
    }

    public PublicKey getKey() {
        return key;
    }

    /**
     * 设置节点失效
     */
    public void setDie() {
        isAlive.set(false);

        for (String key : properties.keySet()) {
            String value = properties.get(key);
            ConcurrentSet<K> nodes = index.get(key).get(value);
            for (K node : nodes) {
                if (node.equals(this)) {
                    nodes.remove(node);
                }
            }
        }
    }

    public boolean isAlive() {
        return isAlive.get();
    }

    /**
     * 设置属性
     * @param key 键
     * @param value 值
     * @param index 索引地址
     */
    public void setProperties(String key, String value, ConcurrentMap<String,ConcurrentMap<String,ConcurrentSet<K>>> index) {
        this.index = index;
        properties.put(key,value);
    }

    @Override
    public String toString() {
        return "name:" + name + ",channelid:" + channelId;
    }
}
