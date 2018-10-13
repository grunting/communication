package cn.gp.channel;

import cn.gp.core.impl.SimpleBasic;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.JksTool;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自定义通道的基础版本
 */
public abstract class SimpleChannel {

    protected SimpleBasic simpleBasic;
    protected Configure configure;
    protected JksTool jksTool;
    protected AtomicBoolean isAlive = new AtomicBoolean(true);
    protected AtomicBoolean isReady = new AtomicBoolean(false);
    protected Channel channel;

    /**
     * 初始化
     * @param simpleBasic 节点信息
     */
    public SimpleChannel(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
        this.configure = simpleBasic.getConfigure();
        this.jksTool = simpleBasic.getJksTool();
    }

    /**
     * 关闭函数
     */
    public void close() {
        this.isAlive.set(false);
        try {
            if (channel != null) {
                ChannelFuture channelFuture = channel.close();
                channelFuture.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否联通
     * @return 布尔
     */
    public boolean isLink() {

        // 需要本节点存活,同时处于连接状态
        return isAlive.get() && channel != null && channel.isActive();
    }

    /**
     * 是否准备好
     * @return 布尔
     */
    public boolean isReady() {

        // 需要本节点存活,同时处于连接状态,同时钩子函数执行成功
        return isAlive.get() && isLink() && isReady.get();
    }

    /**
     * 重新连接
     * @return 布尔
     */
    public boolean reLink() {

        if (channel == null) {
            return true;
        }
        try {
            ChannelFuture channelFuture = channel.close();
            channelFuture.get();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获得通道
     * @return 返回通道
     */
    public Channel getChannel() {
        return isLink() ? channel : null;
    }

    /**
     * 开始函数
     */
    public abstract void start(CheckReadyHook channelHook);

}
