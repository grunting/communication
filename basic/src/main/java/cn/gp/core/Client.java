package cn.gp.core;

import cn.gp.handler.Remote;
import cn.gp.handler.Service;
import cn.gp.proto.Data;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import cn.gp.handler.ChannelHandler;
import cn.gp.util.JksTool;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.util.concurrent.atomic.AtomicBoolean;


import io.netty.channel.Channel;

/**
 * 客户端
 */
public class Client {

	private String name;

	private KeyPair keyPair;

	// 远端通道
	private Channel channel;

	// 本客户端存货与否
	private AtomicBoolean isAlive = new AtomicBoolean(true);

	private Remote remote;
	private Service service;

	/**
	 * 私有化构造
	 */
	private Client() {
		super();
	}

	public String getName() {
		return name;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * 关闭函数
	 */
	public void close() {
		isAlive.set(false);

		remote.close();
		service.close();

		if (channel != null) {
			channel.close();
		}
	}

	/**
	 * 是否存活
	 * @return 返回存活与否
	 */
	public boolean getIsAlive() {
		return isAlive.get();
	}

	/**
	 * 给出服务实例化
	 * @param serviceInterface 服务的
	 * @param <T> 服务实例
	 * @return 返回服务实例
	 */
	public <T> T getRemoteProxyObj(Class<?> serviceInterface) {
		return this.remote.getRemoteProxyObj(serviceInterface);
	}

	public void putServiceInterFace(String key, Class value) {
		this.service.putServers(key,value);
	}

	/**
	 * 开启一个会重试的客户端
	 * @return 返回客户端实例
	 */
	public static Client getInstance() {

		// 非单例,可以多开
		final Client client = new Client();

		final Remote remote = new Remote();
		final Service service = new Service();

		client.remote = remote;
		client.service = service;

		// 实例化本客户端配置文件
		final Configure configure = Configure.getInstance("basic.properties","client.config");

		// 根据配置实例化限速相关
		final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
		final GlobalTrafficShapingHandler trafficShapingHandler = new GlobalTrafficShapingHandler(
				executorGroup,
				configure.getConfigInteger(Constant.CLIENT_NETTY_WRITELIMIT),
				configure.getConfigInteger(Constant.CLIENT_NETTY_READLIMIT));

		final JksTool jksTool = JksTool.getInstance(
				configure.getConfigString(Constant.CLIENT_JKS_PATH),
				configure.getConfigString(Constant.CLIENT_JKS_KEYPASS),
				configure.getConfigString(Constant.CLIENT_JKS_KEYPASS)
		);

		client.name = jksTool.getAlias();
		client.keyPair = jksTool.getKeyPair();

		// 开启可重试的线程
		final Thread thread = new Thread(){

			@Override
			public void run() {
				super.run();
				int retry = configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY,5);
				int count = 0;
				while (true) {

					// 判断是否到了就义的时刻
					if (!client.isAlive.get()) {
						break;
					}

					// 判断是否达到就义的次数
					if (retry == 0) {
						break;
					}

					if (retry != configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY)) {
						count ++;
						System.out.println("服务器连接失败,进行第" + count + "次重试");
					}
					retry --;

					// 真实逻辑
					EventLoopGroup group = new NioEventLoopGroup();
					try {
						Bootstrap b = new Bootstrap();
						b.group(group)
								.channel(NioSocketChannel.class)
								.handler(new ChannelInitializer<SocketChannel>() {
									@Override
									protected void initChannel(SocketChannel ch) throws Exception {
										ChannelPipeline channelPipeline = ch.pipeline();
										channelPipeline.addLast(trafficShapingHandler);
										channelPipeline.addLast(createSslHandler(configure,jksTool));
										channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
										channelPipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
										channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
										channelPipeline.addLast(new ProtobufEncoder());
										channelPipeline.addLast(executorGroup,new ChannelHandler(remote,service));
									}
								})
								.option(ChannelOption.TCP_NODELAY,true);
						ChannelFuture f = b.connect(
								configure.getConfigString(
										Constant.CLIENT_SERVER_HOST),
								configure.getConfigInteger(
										Constant.CLIENT_SERVER_PORT)).sync();
						client.channel = f.channel();
						remote.setChannel(client.channel);
						service.setChannel(client.channel);

						// 正常连接时将在此行阻塞
						f.channel().closeFuture().sync();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						group.shutdownGracefully();
						if (client.channel != null) {
							try {
								client.channel.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							client.channel = null;
						}
					}
					try {

						// 重试间隔
						Thread.sleep(configure.getConfigInteger(Constant.CLIENT_SERVER_INTERVAL));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				client.isAlive.set(false);
				client.remote.close();
				client.service.close();
			}
		};

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				client.close();
				if (client.channel != null) {
					client.channel.close();
				}
			}
		});

		thread.start();
		return client;
	}

	private static SslHandler createSslHandler(Configure configure, JksTool jksTool) throws Exception {

		// 访问Java密钥库，JKS是keytool创建的Java密钥库
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

		//保存服务端的授权证书
		trustManagerFactory.init(jksTool.getKeyStore());

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(jksTool.getKeyStore(),configure.getConfigString(Constant.CLIENT_JKS_KEYPASS).toCharArray());

		SSLContext sslContext = SSLContext.getInstance("TLS");
		try {
			sslContext.init(kmf.getKeyManagers(),trustManagerFactory.getTrustManagers(),null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}

		SSLEngine sslEngine = sslContext.createSSLEngine();
		sslEngine.setUseClientMode(true);
		return new SslHandler(sslEngine);
	}
}
