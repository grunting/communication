package cn.gp.model;

import cn.gp.handler.Remote;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * 客户端bean
 */
public class ClientBean {

    // 客户端名
    private String name;

    // 公钥
    private ByteString publicPass;

    // 通道id
    private String channelId;

    // 通道
    private Channel channel;

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

    public ByteString getPublicPass() {
        return publicPass;
    }

    public void setPublicPass(ByteString publicPass) {
        this.publicPass = publicPass;
    }
}
