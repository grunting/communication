package cn.gp.service.impl;

import cn.gp.service.ChannelHook;
import io.netty.channel.ChannelHandlerContext;

/**
 * 对通道状态处理的钩子接口实现
 */
public class ChannelHookImpl implements ChannelHook {


    public void handlerRemoved(ChannelHandlerContext ctx) {

    }

    public void channelInactive(ChannelHandlerContext ctx) {

    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

    }
}
