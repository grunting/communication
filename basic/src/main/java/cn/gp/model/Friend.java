package cn.gp.model;

import cn.gp.base.IsAlive;
import cn.gp.util.IndexTest;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 同服务器的其他客户端信息
 */
public class Friend implements IsAlive {

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
    private IndexTest<Friend> index;

    public Friend(String channelId, String name, PublicKey key,IndexTest<Friend> index) {
        this.index = index;
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
            Set<Friend> nodes = index.getNode(key,value);
            for (Friend node : nodes) {
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
     */
    public void setProperties(String key, String value) {
        properties.put(key,value);
    }

    @Override
    public String toString() {
        return "name:" + name + ",channelid:" + channelId;
    }
}
