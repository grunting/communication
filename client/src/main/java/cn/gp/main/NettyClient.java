package cn.gp.main;

import cn.gp.crypto.JksTool;
import cn.gp.handler.*;
import cn.gp.handler.ChannelHandler;
import cn.gp.model.Basic;
import cn.gp.proto.Data;
import cn.gp.service.impl.ReportImpl;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
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


/**
 * netty的客户端
 */
public class NettyClient {

    // 限流相关
    private static final EventExecutorGroup EXECUTOR_GROUOP = new DefaultEventExecutorGroup(
            Runtime.getRuntime().availableProcessors() * 2);
    private static final GlobalTrafficShapingHandler trafficHandler = new GlobalTrafficShapingHandler(
            EXECUTOR_GROUOP,
            Configure.getConfigInteger(Constant.CLIENT_NETTY_WRITELIMIT),
            Configure.getConfigInteger(Constant.CLIENT_NETTY_READLIMIT));

    /**
     * netty通用客户端实现
     */
    public static void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline channelPipeline = ch.pipeline();
                            channelPipeline.addLast(trafficHandler);
                            channelPipeline.addLast(createSslHandler());
                            channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                            channelPipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
                            channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            channelPipeline.addLast(new ProtobufEncoder());
                            channelPipeline.addLast(EXECUTOR_GROUOP,new ChannelHandler());
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY,true);
            ChannelFuture f = b.connect(
                    Configure.getConfigString(
                            Constant.SERVER_HOST),
                    Configure.getConfigInteger(
                            Constant.SERVER_PORT)).sync();
            Channel channel = f.channel();

            Basic.setChannel(channel);
            ReportImpl.send();
            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
            group.shutdownGracefully();

            System.out.println();
            System.out.println("服务器已退出或被主动关闭连接");
            System.exit(0);
        }
    }

    private static SslHandler createSslHandler() throws Exception {

        // 访问Java密钥库，JKS是keytool创建的Java密钥库
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

        //保存服务端的授权证书
        trustManagerFactory.init(JksTool.getKeyStore());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(JksTool.getKeyStore(),Configure.getConfigString(Constant.CLIENT_JKS_KEYPASS).toCharArray());

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
