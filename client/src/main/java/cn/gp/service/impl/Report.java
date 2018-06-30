package cn.gp.service.impl;

import com.google.protobuf.ByteString;

/**
 * 处理客户端基本信息协议
 */
public interface Report {

    /**
     * 丢失客户端公告
     * @param name 名称
     */
    void lostClient(String name);

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 服务端通道id
     * @param byteString 公钥
     */
    void findClient(String name, String channelId, ByteString byteString);
}
