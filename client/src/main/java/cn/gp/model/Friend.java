package cn.gp.model;

import com.google.protobuf.ByteString;

/**
 * 同服务器的其他客户端信息
 */
public class Friend {

    // 服务端记录的通道id
    private String channelId;

    // 公开名称
    private String name;

    // 公钥(类似于身份证)
    private ByteString key;

    public Friend(String channelId, String name, ByteString key) {
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

    public ByteString getKey() {
        return key;
    }
}
