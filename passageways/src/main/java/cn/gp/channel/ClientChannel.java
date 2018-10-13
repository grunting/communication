package cn.gp.channel;

import cn.gp.core.impl.SimpleBasic;
import cn.gp.proto.Data;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import cn.gp.util.JksTool;
import cn.gp.handler.ChannelHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyManagementException;

/**
 * 节点通信模块
 */
public class ClientChannel extends SimpleChannel {

    private static final Logger logger = LoggerFactory.getLogger(ClientChannel.class);

    /**
     * 初始化
     * @param simpleBasic 节点信息
     */
    public ClientChannel(SimpleBasic simpleBasic) {
        super(simpleBasic);
    }

    /**
     * 开始函数
     */
    public void start(final CheckReadyHook checkReadyHook) {

        // 根据配置实例化限速相关
        final EventExecutorGroup executorGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
        final GlobalTrafficShapingHandler trafficShapingHandler = new GlobalTrafficShapingHandler(
                executorGroup,
                configure.getConfigInteger(Constant.CLIENT_NETTY_WRITELIMIT),
                configure.getConfigInteger(Constant.CLIENT_NETTY_READLIMIT));

        // 这里准备改为多连接版本
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                int retry = configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY,5);
                int count = 0;
                while(true) {
                    // 判断是否到了就义的时刻
                    if (!isAlive.get()) {
                        logger.debug("server lost,timeToDie");
                        break;
                    }

                    // 判断是否达到就义的次数
                    if (retry == 0) {
                        logger.debug("server lost,retry upper limit,retryUpperLimit:{}",configure.getConfigInteger(Constant.SERVER_RESTART_RETRY));
                        break;
                    }

                    if (retry != configure.getConfigInteger(Constant.CLIENT_SERVER_RETRY)) {
                        count ++;
                        logger.debug("server lost retry:{},retryUpperLimit:{}",count,configure.getConfigInteger(Constant.SERVER_RESTART_RETRY));
                    }
                    retry --;

                    logger.info("start link");

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
                                        channelPipeline.addLast(executorGroup,new ChannelHandler(simpleBasic));
                                    }
                                })
                                .option(ChannelOption.TCP_NODELAY,true);


                        logger.info("connection server host:{},port:{}",configure.getConfigString(Constant.CLIENT_SERVER_HOST),configure.getConfigInteger(Constant.CLIENT_SERVER_PORT));
                        ChannelFuture f = b.connect(
                                configure.getConfigString(
                                        Constant.CLIENT_SERVER_HOST),
                                configure.getConfigInteger(
                                        Constant.CLIENT_SERVER_PORT)).sync();
                        channel = f.channel();

                        logger.debug("client is started");

                        try {
                            int i = 1;
                            while(!checkReadyHook.checkReadyHook()) {
                                Thread.sleep(10);
                                logger.debug("send checkReadyHook,retry:{}",i ++);
                            }
                            isReady.set(true);
                            logger.info("client is ready");
                        } catch (Exception e) {
                            logger.error("client is started,but hook is failed",e);
                            e.printStackTrace();
                        }

                        // 正常连接时将在此行阻塞
                        f.channel().closeFuture().sync();
                        isReady.set(false);
                        checkReadyHook.lostServer();

                    } catch (Exception e) {

                        logger.error("client not started,hook is failed,message:{}",e.getMessage());
                        e.printStackTrace();
                    } finally {
                        group.shutdownGracefully();
                        if (channel != null && channel.isOpen()) {
                            try {
                                channel.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    try {
                        // 重试间隔
                        Thread.sleep(configure.getConfigInteger(Constant.CLIENT_SERVER_INTERVAL));
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
                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
            }
        });

        thread.start();
    }





    /**
     * 给出加密处理
     * @param configure 配置
     * @param jksTool 秘钥配置
     * @return 返回加密处理实例
     * @throws Exception
     */
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
