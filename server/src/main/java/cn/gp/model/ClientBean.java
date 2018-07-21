package cn.gp.model;

import cn.gp.handler.Remote;
import cn.gp.service.IsAlive;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;

import java.security.PublicKey;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 客户端bean
 */
public class ClientBean implements IsAlive {

    // 客户端名
    private String name;

    // 公钥
    private PublicKey publicKey;

    // 通道id
    private String channelId;

    // 通道
    private Channel channel;

    // 是否存活
    private boolean isAlive = true;

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
        isAlive = false;
    }

    /**
     * 回应这个通道是否存活
     * @return
     */
    public boolean isAlive() {
        return isAlive && this.channel.isActive();
    }
}
