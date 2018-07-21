package cn.gp.model;

import cn.gp.service.IsAlive;

import java.security.PublicKey;

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

    private boolean isAlive = true;

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

    public void setDie() {
        isAlive = false;
    }

    public boolean isAlive() {
        return isAlive;
    }

    @Override
    public String toString() {
        return "name:" + name + ",channelid:" + channelId;
    }
}
