package cn.gp.handler;

import cn.gp.core.Client;
import cn.gp.model.Request;
import cn.gp.proto.Data;
import cn.gp.service.ChannelHook;
import cn.gp.util.ByteAndObject;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 远程发送过来的数据的处理部分
 */
public class ChannelHandler extends SimpleChannelInboundHandler<Data.Message> {

	private Remote remote;
	private Service service;
	private ChannelHook channelHook;
	private Client client;

	public ChannelHandler(Remote remote,Service service,ChannelHook channelHook,Client client) {
		super();
		this.remote = remote;
		this.service = service;
		this.channelHook = channelHook;
		this.client = client;
	}

	/**
	 * 响应函数
	 * @param ctx 通道上下文
	 * @param msg 包装出来的信息对象
	 * @throws Exception
	 */
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Data.Message msg) throws Exception {
		Request request = ByteAndObject.deserialize(msg.getBody().toByteArray());

		// 从远端获取执行结果
		if (request.getServiceName() == null) {
			remote.setResult(request.getId(),request.getResult());
		} else {
			if (client == null) {
				service.sendMessageServer(ctx.channel(),request);
			} else {
				service.sendMessage(client.channel,request);
			}


		}
	}

	/**
	 * 发送信息给远端,同时返回异步操作结果
	 * @param request 发送体
	 * @param channel 通道
	 * @return 异步操作实例
	 */
	protected static ChannelFuture sendFinalChannelFuture(Request request, Channel channel) {

		if (channel == null || !channel.isWritable()) {
			return null;
		}

		Data.Message.Builder builder = Data.Message.newBuilder();
		builder.setBody(ByteString.copyFrom(ByteAndObject.serialize(request)));
		return channel.writeAndFlush(builder.build());
	}

	/**
	 * 丢失客户端公告
	 * @param ctx 通道上下文
	 * @throws Exception 错误
     */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		super.handlerRemoved(ctx);
		if (channelHook != null) {
			channelHook.handlerRemoved(ctx);
		}
	}

	/**
	 * 通道关闭
	 * @param ctx 通道上下文
	 * @throws Exception 错误
     */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		if (channelHook != null) {
			channelHook.channelInactive(ctx);
		}
	}

	/**
	 * 发生异常
	 * @param ctx 通道上下文
	 * @param cause 异常内容
	 * @throws Exception 错误
     */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		super.exceptionCaught(ctx, cause);
		if (channelHook != null) {
			channelHook.exceptionCaught(ctx,cause);
		}
	}
}
