package cn.gp.model;

import cn.gp.handler.Remote;
import cn.gp.proto.Data;
import cn.gp.proto.Order;
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

    // 需要处理rpc的队列
    private ConcurrentLinkedQueue<Order.Message.Builder> sendQueue = new ConcurrentLinkedQueue<Order.Message.Builder>();

    public ConcurrentLinkedQueue<Order.Message.Builder> getSendQueue() {
        return sendQueue;
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
        Remote remote = new Remote();
        remote.start(channel,sendQueue);
    }

    public ByteString getPublicPass() {
        return publicPass;
    }

    public void setPublicPass(ByteString publicPass) {
        this.publicPass = publicPass;
    }
}
