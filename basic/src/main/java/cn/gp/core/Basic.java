package cn.gp.core;

import cn.gp.model.Request;
import cn.gp.service.CheckReadyHook;
import io.netty.channel.Channel;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Map;

/**
 * 抽象类
 */
public interface Basic {

	/**
	 * 设置配置
	 * @param configPath 内部配置文件
	 * @param defaultConfigKey 外部指定的配置文件
	 */
	void setConfigPath(String configPath,String defaultConfigKey);

	/**
	 * 增加配置
	 * @param key 键
	 * @param value 值
	 */
	void addConfig(String key,String value);

	/**
	 * 设置准备就绪后执行的钩子函数
	 * @param checkReadyHook 钩子函数
	 */
	void setCheckReadyHook(CheckReadyHook checkReadyHook);

	/**
	 * 启动节点
	 * @return 布尔
	 */
	boolean start();

	/**
	 * 是否就绪
	 * @return 布尔
	 */
	boolean getIsReady();

	/**
	 * 查看是否存活
	 * @return 布尔
	 */
	boolean getIsAlive();

	/**
	 * 关闭函数
	 */
	void close();

	/**
	 * 由该上层统一这个接口
	 * @param channel 通道
	 * @param request 信息
	 */
	void sendMessage(Channel channel, Request request);

	/**
	 * 给出服务实例化
	 * @param serviceInterface 服务的接口
	 * @param channel 通道
	 * @param <T> 服务实例
	 * @return 返回服务实例
	 */
	<T> T getRemoteProxyObj(Class<?> serviceInterface, Channel channel);

	/**
	 * 给出服务实例化
	 * @param serviceInterface 服务的接口
	 * @param <T> 服务实例
	 * @return 返回服务实例
	 */
	<T> T getRemoteProxyObj(Class<?> serviceInterface);

	/**
	 * 获取可信列表
	 * @return 返回可信列表及其公钥
	 */
	Map<String, PublicKey> getTrustMap();

	/**
	 * 获取本机秘钥对
	 * @return 秘钥对
	 */
	KeyPair getKeyPair();

	/**
	 * 获取本机名
	 * @return 本机名
	 */
	String getName();
}
