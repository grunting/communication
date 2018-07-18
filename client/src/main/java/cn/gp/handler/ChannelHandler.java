package cn.gp.handler;

import cn.gp.crypto.SHA;
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

//        byte[] real = Basic.getAes().decode(msg.getBody().toByteArray());
//        Request request = ByteAndObject.deserialize(real);

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
    public static void sendFinal(Request request,Channel channel) {

        Data.Message.Builder builder1 = Data.Message.newBuilder();

//        byte[] crypto = Basic.getAes().encode(ByteAndObject.serialize(request));
//        builder1.setBody(ByteString.copyFrom(crypto));

        builder1.setBody(ByteString.copyFrom(ByteAndObject.serialize(request)));

        channel.writeAndFlush(builder1.build());
    }


    /**
     * 获取请求体签名
     * @param request 请求体
     * @return 签名
     */
//    private String getSha(Request request) {
//
//        byte[] shabefore1 = ByteAndObject.serialize(request);
//        byte[] shabefore2 = Basic.getServerKey().getBytes();
//
//        byte[] shaafter = new byte[3096];
//        for(int i = 0;i < 3000;i ++) {
//            shaafter[i] = shabefore1[i];
//        }
//        for(int i = 3000;i < shaafter.length;i ++) {
//            shaafter[i] = shabefore2[i - 3000];
//        }
//
//        return SHA.encodeSHA(shaafter);
//    }
}
