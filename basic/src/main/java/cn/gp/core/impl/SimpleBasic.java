package cn.gp.core.impl;

import cn.gp.core.Basic;
import cn.gp.handler.Remote;
import cn.gp.handler.Service;
import cn.gp.service.ChannelHook;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.JksTool;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务基础抽象类
 */
public abstract class SimpleBasic implements Basic {

	private static final Logger logger = LoggerFactory.getLogger(SimpleBasic.class);

	// 持有通道
	private Channel channel;

	// 是否存活
	private AtomicBoolean isAlive = new AtomicBoolean(true);

	// 是否准备好
	private AtomicBoolean isReady = new AtomicBoolean(false);

	// 配置相关
	protected Configure configure = new Configure();

	// 管理需要远程执行的任务
	private Remote remote;

	// 管理需要本地执行的任务
	private Service service;

	// jks相关
	protected JksTool jksTool;

	// 对通道异常状态的响应实例
	protected ChannelHook channelHook;

	// 钩子函数是否执行成功
	protected AtomicBoolean checkSuccess = new AtomicBoolean(false);

	// 对联通服务器的后续操作
	protected CheckReadyHook checkReadyHook;

	public SimpleBasic() {
		super();
	}

	/**
	 * 设置连接服务器后的钩子操作
	 * @param checkReadyHook 钩子函数
	 */
	public void setCheckReadyHook(CheckReadyHook checkReadyHook) {

		logger.debug("setCheckReadyHook checkReadyHook:{}",checkReadyHook);

		this.checkReadyHook = checkReadyHook;
		if (getRemote() != null) {
			getRemote().close();
			this.remote = new Remote(this,checkReadyHook);
		}
	}

	protected void setChannel(Channel channel) {
		this.channel = channel;
	}

	protected Channel getChannel() {
		return channel;
	}

	protected Remote getRemote() {
		return remote;
	}

	protected Service getService() {
		return service;
	}

	/**
	 * 获取本机秘钥对
	 * @return 秘钥对
	 */
	public KeyPair getKeyPair() {
		return jksTool.getKeyPair();
	}

	/**
	 * 获取本机名
	 * @return 本机名
	 */
	public String getName() {
		return jksTool.getAlias();
	}

	/**
	 * 获取可信列表
	 * @return 返回可信列表及其公钥
	 */
	public Map<String, PublicKey> getTrustMap() {
		return jksTool.getTrustMap();
	}

	/**
	 * 设置配置
	 * @param configPath 内部配置文件
	 * @param defaultConfigKey 外部指定的配置文件
	 */
	public void setConfigPath(String configPath,String defaultConfigKey) {

		logger.debug("setConfigPath configPath:{},defaultConfigKey:{}",configPath,defaultConfigKey);

		this.configure.getProperties().putAll(Configure.getInstance(configPath,defaultConfigKey).getProperties());
	}

	/**
	 * 增加配置
	 * @param key 键
	 * @param value 值
	 */
	public void addConfig(String key,String value) {

		logger.debug("setConfigPath key:{},value:{}",key,value);

		this.configure.getProperties().setProperty(key,value);
	}

	/**
	 * 设置通道钩子
	 * @param channelHook 钩子接口
	 */
	public void setChannelHook(ChannelHook channelHook) {

		logger.debug("setChannelHook channelHook:{}",channelHook);

		this.channelHook = channelHook;
	}

	/**
	 * 返回通道钩子
	 * @return 钩子接口实现
	 */
	protected ChannelHook getChannelHook() {
		return channelHook;
	}

	/**
	 * 初始化配置
	 * @return 返回初始化成功与否
	 */
	public boolean init() {

		logger.debug("init jksTool:{},checkReadyHook:{}",jksTool,checkReadyHook);

		this.remote = new Remote(this,checkReadyHook);
		this.service = new Service(this);

		if (jksTool == null) {
			this.isAlive.set(false);
		}

		return true;
	}

	/**
	 * 本节点应提供的服务及其实现
	 * @param key 键
	 * @param value 值
	 */
	public void putServiceInterface(String key, Class value) {

		logger.debug("putServiceInterface key:{},value:{}",key,value);

		this.service.putServers(key,value);
	}

	/**
	 * 获取服务的实现
	 * @param key 键
	 * @return 值
     */
	public Class getServiceInterface(String key) {
		return this.service.getServers(key);
	}

	/**
	 * 关闭函数
	 */
	public void close() {

		logger.debug("close");

		isAlive.set(false);
		isReady.set(false);
		checkSuccess.set(false);

		if (remote != null) {
			remote.close();
		}
		if (service != null) {
			service.close();
		}

		if (channel != null && channel.isOpen()) {
			channel.close();
		}
	}

	/**
	 * 返回是否存活
	 * @return 布尔
	 */
	public boolean getIsAlive() {
		return isAlive.get();
	}

	/**
	 * 设置启动成功与否
	 * @param isSuccess 布尔
     */
	protected void setCheckSuccess(boolean isSuccess) {

		logger.debug("setCheckSuccess checkSuccess:{}",isSuccess);
		this.checkSuccess.set(isSuccess);
	}

	/**
	 * 返回是否准备就绪
	 * @return 布尔
	 */
	public boolean getIsReady() {
		return isReady.get() && checkSuccess.get();
	}

	/**
	 * 设置是否准备就绪
	 * @param b 布尔
	 */
	protected void setIsReady(boolean b) {

		logger.debug("setIsReady isReady:{}",b);
		this.isReady.set(b);
	}

	/**
	 * 开启节点
	 * @return 开启成功与否
	 */
	public abstract boolean start();

	/**
	 * 给出服务实例化
	 * @param serviceInterface 服务的接口
	 * @param <T> 服务实例
	 * @return 返回服务实例
	 */
	public <T> T getRemoteProxyObj(Class<?> serviceInterface) {

		logger.debug("getRemoteProxyObj serviceInterface:{}",serviceInterface);

		if (this.service.getServers(serviceInterface.getName()) != null) {

			try {
				Class cla = this.service.getServers(serviceInterface.getName());

				Class<?>[] argsClass = new Class[0];

				Constructor[] cons = cla.getConstructors();
				for (Constructor con : cons) {
					argsClass = con.getParameterTypes();
				}

				Object[] args = new Object[argsClass.length];

				for (int i = 0;i < argsClass.length;i ++) {

					String ba = Basic.class.getName();
					if (ba.equals(argsClass[i].getName()) ) {
						args[i] = this;
					}

					ba = Channel.class.getName();
					if (ba.equals(argsClass[i].getName())) {
						args[i] = channel;
					}
				}

				Constructor<T> constructor = cla.getDeclaredConstructor(argsClass);
				return constructor.newInstance(args);

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return this.remote.getRemoteProxyObj(serviceInterface,channel);
		}
	}

	/**
	 * 给出服务实例化
	 * @param serviceInterface 服务的接口
	 * @param channel 通道
	 * @param <T> 服务实例
	 * @return 返回服务实例
	 */
	public <T> T getRemoteProxyObj(Class<?> serviceInterface, Channel channel) {

		logger.debug("getRemoteProxyObj serviceInterface:{},channel:{}",serviceInterface,channel);

		return this.remote.getRemoteProxyObj(serviceInterface,channel);
	}

	/**
	 * 阻塞到准备好
	 * @return
     */
	public boolean checkReady() {

		logger.debug("checkReady");

		setIsReady(false);
		try {
			while (getChannel() == null || !getChannel().isOpen()) {
				Thread.sleep(10);
			}
			setIsReady(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getIsReady();
	}

}
