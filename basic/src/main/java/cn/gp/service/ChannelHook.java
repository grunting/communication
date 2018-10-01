package cn.gp.service;

import cn.gp.core.Basic;
import io.netty.channel.ChannelHandlerContext;

/**
 * 对通道状态处理的钩子接口
 */
public interface ChannelHook {

    /**
     * 通道丢失
     * @param ctx 通道上下文
     */
    void handlerRemoved(ChannelHandlerContext ctx);


    /**
     * 通道关闭
     * @param ctx 通道上下文
     */
    void channelInactive(ChannelHandlerContext ctx);

    /**
     * 通道异常
     * @param ctx 通道上下文
     * @param cause 异常内容
     */
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause);
}
