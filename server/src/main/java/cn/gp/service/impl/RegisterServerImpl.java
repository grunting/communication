package cn.gp.service.impl;

import cn.gp.crypto.JksTool;
import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.RegisterServer;
import cn.gp.service.Report;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.security.PublicKey;
import java.util.Arrays;

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
     * @param publicKeyClient 公钥
     * @param crypto 验证密文
     * @return 返回添加结果
     */
    public synchronized boolean addClient(String name,PublicKey publicKeyClient,byte[] crypto) throws Exception {

        if(name == null || "".equals(name)) {
            return false;
        }

        PublicKey publicKey = null;

        // 需要的话进行严密验证
        if (Configure.getConfigBoolean(Constant.SERVER_SECURITY_STRICTVAILDATION)) {
            if (JksTool.getTrustMap().containsKey(name)) {
                try {
                    publicKey = JksTool.getTrustMap().get(name);
                    byte[] real = RSA.decrypt(crypto,publicKey);
                    if (!Arrays.equals(name.getBytes(),real)) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        }

        if (publicKey == null) {
            publicKey = publicKeyClient;
        }

        ClientBean clientBeanSelf = new ClientBean();
        clientBeanSelf.setName(name);
        clientBeanSelf.setChannel(channel);
        clientBeanSelf.setPublicKey(publicKey);

        Basic.getIndex().setIndex("names",name,clientBeanSelf);
        Basic.getIndex().setIndex("channelid",clientBeanSelf.getChannelId(),clientBeanSelf);

        for(ClientBean clientBean : Basic.getIndex().getAllNode()) {
            if(!clientBean.getName().equals(name)) {

                // 除了自己,广播给其他客户端自己的存在
                Report report = Remote.getRemoteProxyObj(Report.class,clientBean.getChannel());
                report.findClient(name,channel.id().asLongText(),publicKey);

                // 将其他客户端的信息发送给自己
                Report report2 = Remote.getRemoteProxyObj(Report.class,clientBeanSelf.getChannel());
                report2.findClient(clientBean.getName(),clientBean.getChannelId(),clientBean.getPublicKey());
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
