package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.service.SendMessage;
import cn.gp.service.SendMessageServer;

/**
 * 简易发送消息的实现
 */
public class SendMessageImpl implements SendMessage {

    /**
     * 发送消息
     * @param name 接收人
     * @param message 信息
     */
    public void sendMessage(String name, String message) {
        SendMessageServer sendMessageServer = Remote.getRemoteProxyObj(SendMessageServer.class);
        sendMessageServer.send(name,message);
    }

    /**
     * 接收消息
     * @param sparker 发送人
     * @param message 信息
     */
    public void recoveMessage(String sparker, String message) {
        System.out.println();
        System.out.println(sparker + ":" + message);
        System.out.print("order:");
    }
}
