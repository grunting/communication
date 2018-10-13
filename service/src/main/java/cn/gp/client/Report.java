package cn.gp.client;

import cn.gp.model.Friend;

import java.util.Set;

/**
 * 处理客户端基本信息协议
 */
public interface Report {

    /**
     * 丢失客户端公告
     * @param channelId 名称
     */
    void lostClient(String channelId);

    /**
     * 发现客户端公告
     * @param name 名称
     * @param channelId 服务端通道id
     */
    void findClient(String name, String channelId);

    /**
     * 发送本客户端信息给服务器
     */
    boolean send();

    /**
     * 获取当前存活终端
     * @return 存活终端
     */
    Set<Friend> getAllFriends();

    /**
     * 去除所有客户端
     */
    void lostClientAll();
}
