package cn.gp.service.impl;

import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * 注册类实现
 */
public class RegisterServerImpl implements RegisterServer {

    // 发起者的通道
    private Channel channel;

    public RegisterServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 实现客户端注册
     * @param name 名称
     * @param crypto 验证密文
     * @return 返回添加结果
     */
    public synchronized boolean addClient(String name,byte[] crypto) throws Exception {

        if(name == null || "".equals(name)) {
            return false;
        }

        // 没找到如何获取tls握手时的用户别称,只能用这种low炸的办法……用来防止冒充别人,不过想了想,好像冒充了我也没办法……
        try {
            byte[] real = RSA.decrypt(crypto,Basic.getTrustMap().get(name));
            String message = new String(real);
            if (!message.equals(name)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


        ClientBean clientBeanSelf = new ClientBean();
        clientBeanSelf.setName(name);
        clientBeanSelf.setChannel(channel);
        clientBeanSelf.setPublicKey(Basic.getTrustMap().get(name));

        Basic.getIndex().setIndex("names",name,clientBeanSelf);
        Basic.getIndex().setIndex("channelid",clientBeanSelf.getChannelId(),clientBeanSelf);

        for(ClientBean clientBean : Basic.getIndex().getAllNode()) {
            if(!clientBean.getName().equals(name)) {

                // 除了自己,广播给其他客户端自己的存在
                Report report = Remote.getRemoteProxyObj(Report.class,clientBean.getChannel());
                report.findClient(name,clientBeanSelf.getChannelId());

                // 将其他客户端的信息发送给自己
                Report report2 = Remote.getRemoteProxyObj(Report.class,clientBeanSelf.getChannel());
                report2.findClient(clientBean.getName(),clientBean.getChannelId());
            }
        }

        return true;
    }

    /**
     * 删除通道
     * @param ctx 通道上下文
     */
    public synchronized static void removeChannel(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        for (ClientBean clientBean : Basic.getIndex().getNode("channelid",channel.id().asLongText())) {
            clientBean.setDie();
        }

        // 实现客户端调用
        for(ClientBean clientBean : Basic.getIndex().getAllNode()) {
            Report report = Remote.getRemoteProxyObj(Report.class,clientBean.getChannel());
            report.lostClient(channel.id().asLongText());
        }
    }
}
