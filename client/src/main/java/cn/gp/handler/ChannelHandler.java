package cn.gp.handler;

import cn.gp.model.Basic;
import cn.gp.model.Request;
import cn.gp.proto.Data;
import cn.gp.util.ByteAndObject;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

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

        byte[] real = Basic.getAes().decode(msg.getBody().toByteArray());

        Request request = ByteAndObject.deserialize(real);

        if(request.getServiceName() == null) {
            Remote.setResult(request.getId(), request.getResult());
        } else {
            Service.sendMessage(request);
        }
    }

    /**
     * 发送消息的最终体
     * @param request 发送体
     * @param channel 通道
     */
    public static void sendFinal(Request request,Channel channel) {
        double d = Math.random();
        request.setRandom(d);

        Data.Message.Builder builder1 = Data.Message.newBuilder();
        byte[] crypto = Basic.getAes().encode(ByteAndObject.serialize(request));
        builder1.setBody(ByteString.copyFrom(crypto));

        channel.writeAndFlush(builder1.build());
    }
}
