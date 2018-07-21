package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.SendMessage;
import cn.gp.service.SendMessageServer;
import io.netty.channel.Channel;

/**
 * 发送消息简易实现
 */
public class SendMessageServerImpl implements SendMessageServer {

    // 发起者通道
    private Channel channel;

    public SendMessageServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 发送消息给指定的人
     * @param name 客户端名称
     * @param message 信息
     */
    public void send(String name, String message) {
        String self = null;
        ClientBean target = null;

        for (ClientBean clientBean : Basic.getIndex().getNode("channelid",channel.id().asLongText())) {
            self = clientBean.getName();
        }

        if (self == null) {
            self = "none";
        }

        for (ClientBean clientBean : Basic.getIndex().getNode("names",name)) {
            SendMessage sendMessage = Remote.getRemoteProxyObj(SendMessage.class,clientBean.getChannel());
            sendMessage.recoveMessage(self,message);
        }
    }
}
