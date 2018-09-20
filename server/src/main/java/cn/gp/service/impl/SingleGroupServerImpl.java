package cn.gp.service.impl;

import cn.gp.client.SingleGroup;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.server.SingleGroupServer;
import io.netty.channel.Channel;

/**
 * 单人分组客户端实现
 */
public class SingleGroupServerImpl implements SingleGroupServer {

    // 发起者的通道
    private Channel channel;

    public SingleGroupServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 创建分组第一步
     * @param name 对方的名称
     * @param crypto 加密内容
     * @return 返回对方的响应信息
     * @throws Exception
     */
    public byte[] createGroup01(String name, byte[] crypto) throws Exception {

        ClientBean clientBeanTarget = Basic.getIndex().getNode("names",name).iterator().next();
        SingleGroup singleGroup = Remote.getRemoteProxyObj(SingleGroup.class,clientBeanTarget.getChannel());
        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();

        return singleGroup.create01(clientBeanSelf.getName(),crypto);
    }

    /**
     * 创建分组第二步
     * @param name 对方的名称
     * @param crypto 加密内容
     * @return 返回加密过的对称秘钥
     * @throws Exception
     */
    public byte[] createGroup02(String name, byte[] crypto) throws Exception {

        ClientBean clientBeanTarget = Basic.getIndex().getNode("names",name).iterator().next();
        SingleGroup singleGroup = Remote.getRemoteProxyObj(SingleGroup.class,clientBeanTarget.getChannel());
        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();

        return singleGroup.create02(clientBeanSelf.getName(),crypto);
    }

    /**
     * 发送信息
     * @param name 发送目标
     * @param message 加密信息
     * @return 返回发送成功与否
     * @throws Exception
     */
    public boolean sendMessage(String name, byte[] message) throws Exception {

        ClientBean clientBeanTarget = Basic.getIndex().getNode("names",name).iterator().next();
        SingleGroup singleGroup = Remote.getRemoteProxyObj(SingleGroup.class,clientBeanTarget.getChannel());
        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();

        return singleGroup.receiveMessage(clientBeanSelf.getName(),message);
    }
}
