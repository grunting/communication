package cn.gp.main;

import cn.gp.handler.*;
import cn.gp.handler.ChannelHandler;
import cn.gp.model.Basic;
import cn.gp.proto.Data;
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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;


/**
 * netty的客户端
 */
public class NettyClient {

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
                            channelPipeline.addLast(createSslHandler(getClientSSLContext()));
                            channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                            channelPipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
                            channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            channelPipeline.addLast(new ProtobufEncoder());
                            channelPipeline.addLast(new ChannelHandler());
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY,true);
            ChannelFuture f = b.connect("localhost",8088).sync();

            Channel channel = f.channel();

            Basic.setChannel(channel);

            ScannerHandler.run();

            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            group.shutdownGracefully();

            System.out.println();
            System.out.println("服务器已退出或被主动关闭连接");
            System.exit(0);
        }
    }

    private static SslHandler createSslHandler(SSLContext sslContext) {
        SSLEngine sslEngine = sslContext.createSSLEngine();
        sslEngine.setUseClientMode(true);
        return new SslHandler(sslEngine);
    }

    private static SSLContext getClientSSLContext() throws Exception {
//        ClassLoader runtime = Thread.currentThread().getContextClassLoader();

        KeyStore trustKeyStore= KeyStore.getInstance("JKS");// 访问Java密钥库，JKS是keytool创建的Java密钥库
        trustKeyStore.load(new FileInputStream(Basic.getJksPath()),Basic.getPasswd().toCharArray());
//        trustKeyStore.load(runtime.getResourceAsStream("cChat.jks"),"cNetty".toCharArray());
        TrustManagerFactory trustManagerFactory =   TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustKeyStore);//保存服务端的授权证书

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(trustKeyStore,Basic.getPasswd().toCharArray());

        SSLContext clientContext = SSLContext.getInstance( "TLS");
        clientContext.init(kmf.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        return clientContext;
    }
}
