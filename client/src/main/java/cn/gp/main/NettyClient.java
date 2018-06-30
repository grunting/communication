package cn.gp.main;

import cn.gp.handler.*;
import cn.gp.handler.ChannelHandler;
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
                            channelPipeline.addLast(new ProtobufVarint32FrameDecoder());
                            channelPipeline.addLast(new ProtobufDecoder(Data.Message.getDefaultInstance()));
                            channelPipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
                            channelPipeline.addLast(new ProtobufEncoder());
                            channelPipeline.addLast(new ChannelHandler());
                        }
                    })
                    .option(ChannelOption.TCP_NODELAY,true);
            ChannelFuture f = b.connect("176.122.183.248",8088).sync();

            Channel channel = f.channel();

            Remote remote = new Remote();
            remote.start(channel);

            Service service = new Service();
            service.start(channel);

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
}
