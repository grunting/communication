package cn.gp.main;

import cn.gp.handler.ChannelHandler;
import cn.gp.handler.Remote;
import cn.gp.handler.Service;
import cn.gp.model.Basic;
import cn.gp.proto.Data;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;

/**
 * Netty服务端
 */
public class NettyServer {

    /**
     * netty通用服务端实现
     */
    public static void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(100);// boss线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(100);// worker线程池

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            pipeline.addLast(createSslHandler(initSSLContext(),true));
                            pipeline.addLast(new ProtobufVarint32FrameDecoder());
                            pipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
                            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            pipeline.addLast(new ProtobufEncoder());
                            pipeline.addLast(new ChannelHandler());
                        }
                    })
                    // BACKLOG用于构造服务端套接字ServerSocket对象，标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度。如果未设置或所设置的值小于1，Java将使用默认值50。
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    // 是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活。
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(8088).sync();
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("[ChatServer 关闭了]");
        }
    }

    private static SslHandler createSslHandler(SSLContext sslContext, boolean needsClientAuth) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(false);
        if (needsClientAuth) {
            sslEngine.setNeedClientAuth(true);
        }
        return new SslHandler(sslEngine);
    }

    private static SSLContext initSSLContext() throws Exception{
//        ClassLoader runtime = Thread.currentThread().getContextClassLoader();

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(Basic.getJksPath()),Basic.getPasswd().toCharArray());
//        ks.load(runtime.getResourceAsStream("sChat.jks"),"sNetty".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks,Basic.getPasswd().toCharArray());

        TrustManagerFactory trustManagerFactory =   TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        try {
            sslContext.init(kmf.getKeyManagers(),trustManagerFactory.getTrustManagers(),null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return sslContext;
    }
}
