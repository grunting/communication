package cn.gp.core.impl;

import cn.gp.handler.ChannelHandler;
import cn.gp.model.Request;
import cn.gp.proto.Data;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import cn.gp.util.JksTool;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;

/**
 * 服务端
 */
public class ServerNetty extends SimpleBasic {

	private static final Logger logger = LoggerFactory.getLogger(ServerNetty.class);

	private GlobalTrafficShapingHandler globalTrafficShapingHandler;

	public ServerNetty() {
		super();
	}

	public GlobalTrafficShapingHandler getGlobalTrafficShapingHandler() {
		return this.globalTrafficShapingHandler;
	}

	@Override
	public void setConfigPath(String configPath, String defaultConfigKey) {
		super.setConfigPath(configPath, defaultConfigKey);

		logger.debug("setConfigPath configPath:{},defaultConfigKey:{}",configPath,defaultConfigKey);

		jksTool = JksTool.getInstance(
				configure.getConfigString(Constant.SERVER_JKS_PATH),
				configure.getConfigString(Constant.SERVER_JKS_KEYPASS),
				configure.getConfigString(Constant.SERVER_JKS_KEYPASS)
		);
	}

	@Override
	public boolean start() {

		logger.debug("start");

		// 谁能教教我这里咋写……
		final ServerNetty server = this;

		// 根据配置实例化限速相关
		final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
		final GlobalTrafficShapingHandler trafficShapingHandler = new GlobalTrafficShapingHandler(
				executorGroup,
				configure.getConfigInteger(Constant.SERVER_NETTY_WRITELIMIT),
				configure.getConfigInteger(Constant.SERVER_NETTY_READLIMIT));
		this.globalTrafficShapingHandler = trafficShapingHandler;

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				int retry = configure.getConfigInteger(Constant.SERVER_RESTART_RETRY);
				int count = 0;
				while(true) {

					// 判断是否到了就义的时刻
					if (!getIsAlive()) {
						logger.debug("server lost timeToDie");
						break;
					}

					// 判断是否达到就义的次数
					if (retry == 0) {
						logger.debug("server lost retryExeceed,retryUpperLimit:{}",configure.getConfigInteger(Constant.SERVER_RESTART_RETRY));
						break;
					}

					if (retry != configure.getConfigInteger(Constant.SERVER_RESTART_RETRY)) {
						count ++;
						logger.debug("server lost retry:{},retryUpperLimit:{}",count,configure.getConfigInteger(Constant.SERVER_RESTART_RETRY));
					}
					retry --;

					logger.info("start server");

					// 真实逻辑
					EventLoopGroup bossGroup = new NioEventLoopGroup(2);// boss线程池
					EventLoopGroup workerGroup = new NioEventLoopGroup(10);// worker线程池
					try {
						ServerBootstrap b = new ServerBootstrap();
						b.group(bossGroup,workerGroup)
								.channel(NioServerSocketChannel.class)
								.childHandler(new ChannelInitializer<SocketChannel>() {

									/**
									 * 注意这里每次有新连接都会调用一次
									 * @param ch 通道
									 * @throws Exception 报错
									 */
									@Override
									protected void initChannel(SocketChannel ch) throws Exception {
										ChannelPipeline pipeline = ch.pipeline();

										pipeline.addLast(trafficShapingHandler);
										pipeline.addLast(createSslHandler(true,configure,jksTool));
										pipeline.addLast(new ProtobufVarint32FrameDecoder());
										pipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
										pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
										pipeline.addLast(new ProtobufEncoder());
										pipeline.addLast(executorGroup,new ChannelHandler(getRemote(),getChannelHook(),server));
									}
								})
								// BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
								.option(ChannelOption.SO_BACKLOG, 1024)
								// 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
								.childOption(ChannelOption.SO_KEEPALIVE, true);


						logger.info("server port is {}",configure.getConfigInteger(Constant.SERVER_PORT));
						ChannelFuture f = b.bind(configure.getConfigInteger(Constant.SERVER_PORT)).sync();

						setChannel(f.channel());
						setCheckSuccess(true);
						logger.info("server is started");

						f.channel().closeFuture().sync();
						logger.debug("channel closed");

					} catch (Exception e) {

						logger.error("server not started",e);
						e.printStackTrace();
					} finally {
						workerGroup.shutdownGracefully();
						bossGroup.shutdownGracefully();
					}

					try {
						Thread.sleep(configure.getConfigInteger(Constant.SERVER_RESTART_INTERVAL));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				close();
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				close();
			}
		});

		thread.start();

		return checkReady();
	}

	/**
	 * 发送信息到指定通道中
	 * @param channel 通道
	 * @param request 信息
     */
	public void sendMessage(Channel channel, Request request) {
		getService().sendMessageServer(channel, request);
	}

	/**
	 * 给出加密处理
	 * @param needsClientAuth 是否需要验证客户端
	 * @param configure 配置
	 * @param jksTool 秘钥配置
	 * @return 返回加密处理实例
     * @throws Exception
     */
	private static SslHandler createSslHandler(boolean needsClientAuth, Configure configure, JksTool jksTool) throws Exception {

		logger.debug("createSslHandler configure:{},jksTool:{}",configure,jksTool);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(
				jksTool.getKeyStore(),                                                  // 这里由JksTool提供静态加载,屏蔽动态添加
				configure.getConfigString(Constant.SERVER_JKS_KEYPASS).toCharArray());  // 由configure提供的参数配置

		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
				TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(jksTool.getKeyStore());

		SSLContext sslContext = SSLContext.getInstance("TLS");
		try {
			sslContext.init(kmf.getKeyManagers(),trustManagerFactory.getTrustManagers(),null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(false);
		if (needsClientAuth) {
			sslEngine.setNeedClientAuth(true);
		}
		return new SslHandler(sslEngine);
	}

}
