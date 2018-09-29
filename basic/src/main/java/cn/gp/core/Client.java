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
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


import io.netty.channel.Channel;

/**
 * 客户端
 */
public class Client {

	// 本机名
	private String name;

	// 秘钥对
	private KeyPair keyPair;

	// 远端通道
	public Channel channel;

	// 本客户端存货与否
	private AtomicBoolean isAlive = new AtomicBoolean(true);

	// 发送给远端执行的实例
	private Remote remote;

	// 远端需要本地执行的实例
	private Service service;

	private Configure configure;

	private JksTool jksTool;

	// 可信列表
	private Map<String,PublicKey> trustMap;

	/**
	 * 私有化构造
	 */
	private Client() {
		super();

		this.remote = new Remote();
		this.service = new Service();
		this.configure = Configure.getInstance("basic.properties","client.config");

		if (configure != null) {
			this.jksTool = JksTool.getInstance(
					configure.getConfigString(Constant.CLIENT_JKS_PATH),
					configure.getConfigString(Constant.CLIENT_JKS_KEYPASS),
					configure.getConfigString(Constant.CLIENT_JKS_KEYPASS)
			);
		} else {
			isAlive.set(false);
		}

		if (jksTool != null) {
			this.name = jksTool.getAlias();
			this.keyPair = jksTool.getKeyPair();
			this.trustMap = jksTool.getTrustMap();
		} else {
			isAlive.set(false);
		}

	}

	public Map<String, PublicKey> getTrustMap() {
		return trustMap;
	}

	/**
	 * 获取本机名
	 * @return 本机名
     */
	public String getName() {
		return name;
	}

	/**
	 * 获取本机秘钥对
	 * @return 秘钥对
     */
	public KeyPair getKeyPair() {
		return keyPair;
	}

	/**
	 * 关闭函数
	 */
	public void close() {

		// 先关闭本实例
		isAlive.set(false);

		// 关闭给远端执行的实例
		remote.close();

		// 关闭本地执行的实例
		service.close();

		// 关闭连接通道
		if (channel != null && channel.isOpen()) {
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
		System.out.println(channel);

		return this.remote.getRemoteProxyObj(serviceInterface,channel);
	}

	/**
	 * 设置本机提供的接口(与服务器需要配对添加)
	 * @param key 接口名
	 * @param value 接口实现
     */
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

		// 根据配置实例化限速相关
		final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
		final GlobalTrafficShapingHandler trafficShapingHandler = new GlobalTrafficShapingHandler(
				executorGroup,
				client.configure.getConfigInteger(Constant.CLIENT_NETTY_WRITELIMIT),
				client.configure.getConfigInteger(Constant.CLIENT_NETTY_READLIMIT));

		// 开启可重试的线程
		Thread thread = new Thread(){

			@Override
			public void run() {
				super.run();
				int retry = client.configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY,5);
				int count = 0;
				while (true) {

					// 判断是否到了就义的时刻
					if (!client.getIsAlive()) {
						break;
					}

					// 判断是否达到就义的次数
					if (retry == 0) {
						break;
					}

					if (retry != client.configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY)) {
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
										channelPipeline.addLast(createSslHandler(client.configure,client.jksTool));
										channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
										channelPipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
										channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
										channelPipeline.addLast(new ProtobufEncoder());
										channelPipeline.addLast(executorGroup,new ChannelHandler(client.remote,client.service,null,client));
									}
								})
								.option(ChannelOption.TCP_NODELAY,true);
						ChannelFuture f = b.connect(
								client.configure.getConfigString(
										Constant.CLIENT_SERVER_HOST),
								client.configure.getConfigInteger(
										Constant.CLIENT_SERVER_PORT)).sync();
						client.channel = f.channel();

						// 正常连接时将在此行阻塞
						f.channel().closeFuture().sync();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						group.shutdownGracefully();
						if (client.channel != null && client.channel.isOpen()) {
							try {
								client.channel.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					try {

						// 重试间隔
						Thread.sleep(client.configure.getConfigInteger(Constant.CLIENT_SERVER_INTERVAL));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				client.close();
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
