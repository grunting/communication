package cn.gp.handler;

import cn.gp.model.Basic;
import cn.gp.proto.Data;
import cn.gp.proto.Order;
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

        Order.Message message = Order.Message.parseFrom(real);

        byte[] bytes = message.getReturn().toByteArray();
        if(bytes.length != 0) {
            Remote.setResult(Integer.parseInt(message.getRandom()),ByteAndObject.toObject(bytes));
        } else {
            Service.sendMessage(message);
        }
    }

    /**
     * 发送消息的最终体
     * @param order 发送体
     * @param channel 通道
     */
    public static void sendFinal(Order.Message.Builder order,Channel channel) {
        double d = Math.random();
        order.setOrder(ByteString.copyFrom(String.valueOf(d).getBytes()));

        final Data.Message.Builder builder1 = Data.Message.newBuilder();
        byte[] crypto = Basic.getAes().encode(order.build().toByteArray());
        builder1.setBody(ByteString.copyFrom(crypto));

        channel.writeAndFlush(builder1.build());
    }
}
