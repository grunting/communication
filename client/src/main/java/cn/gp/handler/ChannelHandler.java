package cn.gp.handler;

import cn.gp.model.Request;
import cn.gp.proto.Data;
import cn.gp.util.ByteAndObject;
import com.google.protobuf.ByteString;
import io.netty.channel.*;

/**
 * 客户端与远程的处理部分
 */
public class ChannelHandler extends SimpleChannelInboundHandler<Data.Message> {

    /**
     * 响应函数
     * @param ctx 通道上下文
     * @param msg 包装出来的信息对象
     * @throws Exception
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Data.Message msg) throws Exception {

        Request request = ByteAndObject.deserialize(msg.getBody().toByteArray());

        // 从远端拿到执行结果
        if(request.getServiceName() == null) {
            Remote.setResult(request.getId(), request.getResult());

        // 需要本地执行的任务
        } else {
            Service.sendMessage(request);
        }
    }

    /**
     * 发送给远端
     * @param request 发送体
     * @param channel 通道
     */
    public static void sendFinal(Request request, Channel channel) {

        Data.Message.Builder builder1 = Data.Message.newBuilder();

        builder1.setBody(ByteString.copyFrom(ByteAndObject.serialize(request)));

        channel.writeAndFlush(builder1.build());
    }

    /**
     * 发送信息给远端同时返回异步操作结果
     * @param request 发送体
     * @param channel 通道
     * @return 异步操作实例
     */
    public static ChannelFuture sendFinalChannelFuture(Request request,Channel channel) {

        Data.Message.Builder builder1 = Data.Message.newBuilder();
        builder1.setBody(ByteString.copyFrom(ByteAndObject.serialize(request)));

//        channel.writeAndFlush(null).addListener(new ChannelFutureListener() {
//            public void operationComplete(ChannelFuture future) throws Exception {
//
//            }
//        });
        return channel.writeAndFlush(builder1.build());
    }
}
