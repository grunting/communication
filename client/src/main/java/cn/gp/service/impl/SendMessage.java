package cn.gp.service.impl;

/**
 * 简易通信协议
 */
public interface SendMessage {

    /**
     * 发送信息
     * @param name 接收人
     * @param message 信息
     */
    void sendMessage(String name, String message);

    /**
     * 接受信息
     * @param sparker 发送人
     * @param message 信息
     */
    void recoveMessage(String sparker, String message);
}
