package cn.gp.main;

import cn.gp.crypto.JksTool;
import cn.gp.handler.ChannelHandler;
import cn.gp.handler.Remote;
import cn.gp.handler.Service;
import cn.gp.model.Basic;
import cn.gp.proto.Data;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
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
import io.netty.handler.traffic.TrafficCounter;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端
 */
public class NettyServer {

    // 限流相关
    private static final EventExecutorGroup EXECUTOR_GROUOP = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
    private static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(EXECUTOR_GROUOP,
            Configure.getConfigInteger(Constant.SERVER_NETTY_WRITELIMIT),
            Configure.getConfigInteger(Constant.SERVER_NETTY_READLIMIT));

    static {
        new Thread(new Runnable() {

            public void run() {
                while(true) {
                    TrafficCounter trafficCounter = trafficHandler.trafficCounter();
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final long totalRead = trafficCounter.cumulativeReadBytes();
                    final long totalWrite = trafficCounter.cumulativeWrittenBytes();
                    System.out.println(trafficCounter + ", Total read:" + (totalRead >> 10) + " KB, Total write:" + (totalWrite >> 10) + " KB");
                }
            }
        }).start();
    }


    /**
     * netty通用服务端实现
     */
    public static void run() {
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

                            pipeline.addLast(trafficHandler);
                            pipeline.addLast(createSslHandler(true));
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(EXECUTOR_GROUOP,new ChannelHandler());
                        }
                    })
                    // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(Configure.getConfigInteger(Constant.SERVER_PORT)).sync();
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("[ChatServer 关闭了]");
            System.exit(1);
        }
    }

    /**
     * 给出加密处理
     * @param needsClientAuth 是否需要验证客户端
     * @return 返回加密处理实例
     * @throws Exception 运行出错
     */
    private static SslHandler createSslHandler(boolean needsClientAuth) throws Exception {

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(
                JksTool.getKeyStore(),                                                  // 这里由JksTool提供静态加载,屏蔽动态添加
                Configure.getConfigString(Constant.SERVER_JKS_KEYPASS).toCharArray());  // 由configure提供的参数配置

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(JksTool.getKeyStore());

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
