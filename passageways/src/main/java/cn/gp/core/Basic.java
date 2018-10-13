package cn.gp.core;

import cn.gp.model.ClientBean;
import cn.gp.util.Configure;
import cn.gp.util.JksTool;
import io.netty.channel.Channel;

/**
 * 抽象类
 */
public interface Basic {

    Configure getConfigure();
    JksTool getJksTool();

    /**
     * 给出服务实例化
     * @param serviceInterface 服务的接口
     * @param clientBean 使用远程通道时对方的名称
     * @param <T> 服务实例
     * @return 返回服务实例
     */
    <T> T getRemoteProxyObj(Class<?> serviceInterface, ClientBean clientBean);

    /**
     * 给出服务实例化
     * @param serviceInterface 服务的接口
     * @param clientBean 使用远程通道时对方的名称
     * @param <T> 服务实例
     * @return 返回服务实例
     */
    <T> T getRemoteProxyObj(Class<?> serviceInterface, ClientBean clientBean,long sleepTime,int retry);

    /**
     * 给出服务实例化
     * @param serviceInterface 服务的接口
     * @param <T> 服务实例
     * @return 返回服务实例
     */
    <T> T getRemoteProxyObj(Class<?> serviceInterface);

    /**
     * 给出服务实例化
     * @param serviceInterface 服务的接口
     * @param <T> 服务实例
     * @return 返回服务实例
     */
    <T> T getRemoteProxyObj(Class<?> serviceInterface,long sleepTime,int retry);
}
