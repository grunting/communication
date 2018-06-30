package cn.gp.service;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.impl.RegisterServer;
import cn.gp.service.impl.Report;
import com.google.protobuf.ByteString;
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
     * @param byteString 公钥
     * @return 返回添加结果
     */
    public synchronized boolean addClient(final String name, final ByteString byteString) {

        if(name == null || "".equals(name)) {
            return false;
        }

        if(Basic.getChannelMap().contains(name)) {
            return false;
        } else {

            final ClientBean clientBeanSelf = new ClientBean();
            clientBeanSelf.setName(name);
            clientBeanSelf.setChannel(channel);
            clientBeanSelf.setPublicPass(byteString);

            Basic.getChannelMap().put(name,clientBeanSelf);

            for(ClientBean clientBean : Basic.getChannelMap().values()) {
                if(!clientBean.getName().equals(name)) {

                    // 除了自己,广播给其他客户端自己的存在
                    Report report = Remote.getRemoteProxyObj(Report.class,clientBean.getSendQueue());
                    report.findClient(name,channel.id().asLongText(),byteString);

                    // 将其他客户端的信息发送给自己
                    Report report2 = Remote.getRemoteProxyObj(Report.class,clientBeanSelf.getSendQueue());
                    report2.findClient(clientBean.getName(),clientBean.getChannelId(),clientBean.getPublicPass());
                }
            }

            return true;
        }
    }

    /**
     * 删除通道
     * @param ctx 通道上下文
     */
    public synchronized static void removeChannel(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        String name = "";
        for(ClientBean clientBean : Basic.getChannelMap().values()) {
            if (clientBean.getChannelId().equals(channel.id().asLongText())) {
                name = clientBean.getName();
            }
        }
        Basic.getChannelMap().remove(name);

        // 实现客户端调用
        for(ClientBean clientBean : Basic.getChannelMap().values()) {
            Report report = Remote.getRemoteProxyObj(Report.class,clientBean.getSendQueue());
            report.lostClient(clientBean.getName());
        }
    }
}
