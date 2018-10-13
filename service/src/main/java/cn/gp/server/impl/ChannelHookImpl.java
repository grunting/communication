package cn.gp.server.impl;

import cn.gp.core.impl.SimpleBasic;
import cn.gp.server.RegisterServer;
import cn.gp.service.ChannelHook;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对通道状态处理的钩子接口实现
 */
public class ChannelHookImpl implements ChannelHook {

    private static final Logger logger = LoggerFactory.getLogger(ChannelHookImpl.class);

    private SimpleBasic simpleBasic;

    public ChannelHookImpl(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
    }

    public void handlerRemoved(ChannelHandlerContext ctx) {

        RegisterServer registerServer = simpleBasic.getRemoteProxyObj(RegisterServer.class);
        registerServer.removeChannel(ctx);

        logger.debug("handlerRemoved channel:{}",ctx.channel().id().asLongText());

    }

    public void channelInactive(ChannelHandlerContext ctx) {

        RegisterServer registerServer = simpleBasic.getRemoteProxyObj(RegisterServer.class);
        registerServer.removeChannel(ctx);

        logger.debug("channelInactive channel:{}",ctx.channel().id().asLongText());

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        RegisterServer registerServer = simpleBasic.getRemoteProxyObj(RegisterServer.class);
        registerServer.removeChannel(ctx);

        logger.debug("exceptionCaught channel:{}",ctx.channel().id().asLongText());

//        // 当出现异常就关闭连接
//        Channel incoming = ctx.channel();
//        System.out.println("ChatClient:" + incoming.remoteAddress() + "异常,已被服务器关闭");
//        cause.printStackTrace();

        ctx.close();
    }
}
