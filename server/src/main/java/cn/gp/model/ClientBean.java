package cn.gp.model;

import cn.gp.service.IsAlive;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 客户端bean
 */
public class ClientBean<K> implements IsAlive<K> {

    // 客户端名
    private String name;

    // 公钥
    private PublicKey publicKey;

    // 通道id
    private String channelId;

    // 通道
    private Channel channel;

    // 是否存活
    private AtomicBoolean isAlive = new AtomicBoolean(true);

    // 属性值
    private Map<String,String> properties = new HashMap<String, String>();

    // 索引根
    private ConcurrentMap<String,ConcurrentMap<String,ConcurrentSet<K>>> index;

    public Channel getChannel() {
        return channel;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChannel(Channel channel) {
        this.channelId = channel.id().asLongText();
        this.channel = channel;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
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

    /**
     * 回应这个通道是否存活
     * @return
     */
    public boolean isAlive() {
        return isAlive.get() && this.channel.isActive();
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
}
