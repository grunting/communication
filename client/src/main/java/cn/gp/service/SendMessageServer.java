package cn.gp.service;

/**
 * 简易通信服务端协议
 */
public interface SendMessageServer {

    /**
     * 发送消息
     * @param name 客户端名称
     * @param message 信息
     */
    void send(String name, String message);
}
