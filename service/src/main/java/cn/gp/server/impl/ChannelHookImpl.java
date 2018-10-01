package cn.gp.server.impl;

import cn.gp.core.Basic;
import cn.gp.server.RegisterServer;
import cn.gp.service.ChannelHook;
import io.netty.channel.ChannelHandlerContext;

/**
 * 对通道状态处理的钩子接口实现
 */
public class ChannelHookImpl implements ChannelHook {

	private Basic basic;

	public ChannelHookImpl(Basic basic) {
		this.basic = basic;
	}

	public void handlerRemoved(ChannelHandlerContext ctx) {

		RegisterServer registerServer = basic.getRemoteProxyObj(RegisterServer.class);
		registerServer.removeChannel(ctx);

	}

	public void channelInactive(ChannelHandlerContext ctx) {

		RegisterServer registerServer = basic.getRemoteProxyObj(RegisterServer.class);
		registerServer.removeChannel(ctx);

	}

	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

		RegisterServer registerServer = basic.getRemoteProxyObj(RegisterServer.class);
		registerServer.removeChannel(ctx);

//        // 当出现异常就关闭连接
//        Channel incoming = ctx.channel();
//        System.out.println("ChatClient:" + incoming.remoteAddress() + "异常,已被服务器关闭");
//        cause.printStackTrace();

		ctx.close();
	}
}
