package cn.gp.server;

import io.netty.channel.ChannelHandlerContext;

/**
 * 注册本客户端服务端协议
 */
public interface RegisterServer {

    /**
     * 处理客户端上报
     * @param name 名称
     * @param crypto 验证密文
     * @return 返回上报结果(名称可能冲突)
     */
    boolean addClient(String name, byte[] crypto) throws Exception;

    /**
     * 删除通道
     * @param ctx 通道上下文
     */
    void removeChannel(ChannelHandlerContext ctx);
}
