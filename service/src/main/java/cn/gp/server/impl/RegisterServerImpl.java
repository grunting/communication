package cn.gp.server.impl;

import cn.gp.channel.SimpleChannel;
import cn.gp.core.Basic;
import cn.gp.core.impl.SimpleBasic;
import cn.gp.crypto.RSA;
import cn.gp.model.ClientBean;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;
import cn.gp.util.IndexTest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 注册类实现
 */
public class RegisterServerImpl implements RegisterServer {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServerImpl.class);

    public static final IndexTest<ClientBean> index = new IndexTest<ClientBean>();

    private SimpleBasic simpleBasic;
    private SimpleChannel simpleChannel;
    private Channel channel;

    public RegisterServerImpl(SimpleBasic simpleBasic, SimpleChannel simpleChannel, Channel channel){
        this.simpleBasic = simpleBasic;
        this.simpleChannel = simpleChannel;
        this.channel = channel;
    }

    public Set<ClientBean> getClientBean() {
        return index.getAllNode();
    }

    /**
     * 实现客户端注册
     * @param name 名称
     * @param crypto 验证密文
     * @return 返回添加结果
     */
    public synchronized boolean addClient(String name,byte[] crypto) throws Exception {

        logger.debug("addClient1 name:{},crypto:{}",name,crypto);

        if(name == null || "".equals(name)) {
            return false;
        }

        // 没找到如何获取tls握手时的用户别称……
        try {
            byte[] real = RSA.decrypt(crypto,simpleBasic.getTrustPublicKey(name));
            String message = new String(real);
            if (!message.equals(name)) {
                logger.debug("addClient2 message:{}",message);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        ClientBean clientBeanSelf = new ClientBean(index);
        clientBeanSelf.setName(name);
        clientBeanSelf.setChannel(channel);
        clientBeanSelf.setPublicKey(simpleBasic.getTrustPublicKey(name));

        index.setIndex("names",name,clientBeanSelf);
        index.setIndex("channelid",clientBeanSelf.getChannelId(),clientBeanSelf);

        logger.debug("addClient3 nodes:{}",index.getAllNode());

        for(ClientBean clientBean : index.getAllNode()) {
            if(!clientBean.getName().equals(name)) {

                // 除了自己,广播给其他客户端自己的存在
                Report report = simpleBasic.getRemoteProxyObj(Report.class,clientBean);

                report.findClient(name,clientBeanSelf.getChannelId());

                // 将其他客户端的信息发送给自己
                Report report2 = simpleBasic.getRemoteProxyObj(Report.class,clientBeanSelf);
                report2.findClient(clientBean.getName(),clientBean.getChannelId());
            }
        }

        logger.debug("addClient index:{}",index.getAllNode().toString());
        return true;
    }

    /**
     * 删除通道
     * @param ctx 通道上下文
     */
    public synchronized void removeChannel(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        for (ClientBean clientBean : index.getNode("channelid",channel.id().asLongText())) {

            clientBean.setDie();
        }

        // 实现客户端调用
        for(ClientBean clientBean : index.getAllNode()) {
            Report report = simpleBasic.getRemoteProxyObj(Report.class,clientBean);
            report.lostClient(channel.id().asLongText());
        }

        logger.debug("removeChannel index:{}",index.getAllNode().toString());
    }
}
