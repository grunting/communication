package cn.gp.core.impl;

import cn.gp.channel.SimpleChannel;
import cn.gp.core.Basic;
import cn.gp.handler.LocalProcess;
import cn.gp.handler.Remote;
import cn.gp.model.ClientBean;
import cn.gp.service.ChannelHook;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.JksTool;

import java.lang.reflect.Constructor;
import java.security.KeyPair;
import java.security.PublicKey;

/**
 * 服务基础抽象类
 */
public abstract class SimpleBasic implements Basic {

    protected Configure configure = new Configure();
    protected JksTool jksTool;
    protected Remote remote;
    protected LocalProcess localProcess;
    protected SimpleChannel simpleChannel;
    protected CheckReadyHook checkReadyHook;
    protected ChannelHook channelHook;

    public SimpleBasic() {
        this.remote = new Remote(this,checkReadyHook);
        this.localProcess = new LocalProcess(this);
    }



    public ChannelHook getChannelHook() {
        return this.channelHook;
    }

    public String getName() {
        return jksTool.getAlias();
    }

    public KeyPair getKeyPair() {
        return jksTool.getKeyPair();
    }

    public Remote getRemote() {
        return this.remote;
    }

    public LocalProcess getLocalProcess() {
        return this.localProcess;
    }

    public SimpleChannel getSimpleChannel() {
        return this.simpleChannel;
    }

    /**
     * 关闭函数
     */
    public void close() {
        this.remote.close();
        this.localProcess.close();
        this.simpleChannel.close();
    }

    /**
     * 本节点应提供的服务及其实现
     * @param key 键
     * @param value 值
     */
    public void putServiceInterface(String key, Class value) {

        this.localProcess.putServers(key,value);
    }

    /**
     * 获取服务实现
     * @param key 键
     * @return 值
     */
    public Class getServiceInterface(String key) {
        return this.localProcess.getServers(key);
    }

    /**
     * 查询可信列表中是否存在该客户端
     * @param name 客户端名称
     * @return 布尔
     */
    public boolean containsTrust(String name) {
        return this.jksTool.getTrustMap().containsKey(name);
    }

    /**
     * 查找可信终端的公钥
     * @param name 客户端名称
     * @return 布尔
     */
    public PublicKey getTrustPublicKey(String name) {
        return this.jksTool.getTrustMap().get(name);
    }

    public <T> T getRemoteProxyObj(Class<?> serviceInterface, ClientBean clientBean) {
        return getRemoteProxyObj(serviceInterface,clientBean,10,100);
    }

    public <T> T getRemoteProxyObj(Class<?> serviceInterface, ClientBean clientBean,long sleepTime,int retry) {
        return this.remote.getRemoteProxyObj(serviceInterface,clientBean.getChannel(),sleepTime,retry);
    }

    public <T> T getRemoteProxyObj(Class<?> serviceInterface) {
        return getRemoteProxyObj(serviceInterface,10,100);
    }

    public <T> T getRemoteProxyObj(Class<?> serviceInterface,long sleepTime,int retry) {
        if (this.localProcess.getServers(serviceInterface.getName()) != null) {

            try {
                Class cla = this.localProcess.getServers(serviceInterface.getName());

                Class<?>[] argsClass = new Class[0];

                Constructor[] cons = cla.getConstructors();
                for (Constructor con : cons) {
                    argsClass = con.getParameterTypes();
                }

                Object[] args = new Object[argsClass.length];
                for (int i = 0;i < argsClass.length;i ++) {

                    String ba = SimpleBasic.class.getName();
                    if (ba.equals(argsClass[i].getName())) {
                        args[i] = this;
                    }

                    ba = SimpleChannel.class.getName();
                    if (ba.equals(argsClass[i].getName())) {
                        args[i] = simpleChannel;
                    }
                }

                Constructor<T> constructor = cla.getDeclaredConstructor(argsClass);
                return constructor.newInstance(args);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return this.remote.getRemoteProxyObj(serviceInterface,simpleChannel.getChannel(),sleepTime,retry);
        }
    }

    /**
     * 开始函数
     */
    public void start() {
        simpleChannel.start(checkReadyHook);
    }

    /**
     * 获取配置
     * @return 配置内容
     */
    public Configure getConfigure() {
        return this.configure;
    }

    /**
     * 获取jks
     * @return jks
     */
    public JksTool getJksTool() {
        return this.jksTool;
    }
}
