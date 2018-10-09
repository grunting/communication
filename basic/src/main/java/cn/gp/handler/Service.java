package cn.gp.handler;

import ch.qos.logback.core.util.TimeUtil;
import cn.gp.core.Basic;
import cn.gp.model.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 本地需要处理的信息
 */
public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	// 节点
	private Basic basic;

	// 本节点提供的服务
	private ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

	// 本节点需要处理的任务
	private ExecutorService pool = Executors.newCachedThreadPool();

	/**
	 * 需要本节点实例做实例化
	 * @param basic 当前节点
     */
	public Service(Basic basic) {
		this.basic = basic;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				pool.shutdown();
			}
		});
	}

	/**
	 * 关闭函数
	 */
	public void close() {

		logger.debug("service closed");

		if (!pool.isShutdown()) {
			pool.shutdownNow();
		}
	}

	/**
	 * 返回本地的服务
	 * @param key 服务名(接口全地址)
	 */
	public Class getServers(String key) {
		return this.servers.get(key);
	}

	/**
	 * 设置本地服务
	 * @param key 服务名
	 * @param value 服务的实现实例
     */
	public void putServers(String key, Class value) {
		this.servers.put(key,value);
	}

	/**
	 * 远端发送过来需要自身处理的信息(客户端版本)
	 * @param request 信息
	 */
	public void sendMessageClient(final Channel channel,final Request request) {

		logger.debug("sendMessageClient1 channel:{},request:{}",channel,request);

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					Integer id = request.getId();
					String serviceName = request.getServiceName();
					String methodName = request.getMethodName();
					Class<?>[] parameterTypes = request.getParameterTypes();
					Object[] arguments = request.getArguments();

					logger.debug("sendMessageClient2 id:{},serviceName:{},methodName:{},parameterTypes:{},arguments:{}", Arrays.asList(id,serviceName,methodName,parameterTypes,arguments));

					Class serviceClass = servers.get(serviceName);
					logger.debug("sendMessageClient3 id:{},serviceClass:{}",id,serviceClass);

					Class<?>[] argsClass = new Class<?>[1];
					argsClass[0] = Basic.class;

					Object[] args = new Object[1];
					args[0] = basic;

					Constructor cons = serviceClass.getConstructor(argsClass);
					Method method = serviceClass.getMethod(methodName,parameterTypes);
					Object result = method.invoke(cons.newInstance(args),arguments);
					logger.debug("sendMessageClient4 id:{},result:{}",id,result);

					Request request1 = new Request();
					request1.setId(id);
					request1.setResult(result);
					logger.debug("sendMessageClient5 request:{}",request1);

					ChannelFuture channelFuture;
					while((channelFuture = ChannelHandler.sendFinalChannelFuture(request1, channel)) == null) {
						Thread.sleep(100);
					}

					// 暂时设置为10小时超时
					channelFuture.get(20, TimeUnit.SECONDS);
					logger.debug("sendMessageClient6 id:{},success",id);

				} catch (Exception e) {
					logger.error("sendMessageClient error,id:{},exception:{}",request.getId(),e.getMessage());
					e.printStackTrace();
				}
			}
		};
		pool.submit(thread);
	}

	/**
	 * 远端发送过来需要自身处理的信息(服务端版本)
	 * @param request 信息
	 */
	public void sendMessageServer(final Channel channel,final Request request) {

		logger.debug("sendMessageServer1 channel:{},request:{}",channel,request);

		Thread thread = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					Integer id = request.getId();
					String serviceName = request.getServiceName();
					String methodName = request.getMethodName();
					Class<?>[] parameterTypes = request.getParameterTypes();
					Object[] arguments = request.getArguments();

					logger.debug("sendMessageServer2 id:{},serviceName:{},methodName:{},parameterTypes:{},arguments:{}", Arrays.asList(id,serviceName,methodName,parameterTypes,arguments));

					Class serviceClass = servers.get(serviceName);
					logger.debug("sendMessageServer3 id:{},serviceClass:{}",id,serviceClass);

					Class<?>[] argsClass = new Class<?>[2];
					argsClass[0] = Channel.class;
					argsClass[1] = Basic.class;

					Object[] args = new Object[2];
					args[0] = channel;
					args[1] = basic;

					Constructor cons = serviceClass.getConstructor(argsClass);
					Method method = serviceClass.getMethod(methodName,parameterTypes);
					Object result = method.invoke(cons.newInstance(args),arguments);
					logger.debug("sendMessageServer4 id:{},result:{}",id,result);

					Request request1 = new Request();
					request1.setId(id);
					request1.setResult(result);
					logger.debug("sendMessageServer5 request:{}",request1);

					ChannelFuture channelFuture;
					while((channelFuture = ChannelHandler.sendFinalChannelFuture(request1, channel)) == null) {
						Thread.sleep(100);
					}

					// 暂时设置为10小时超时
					channelFuture.get(20, TimeUnit.SECONDS);
					logger.debug("sendMessageServer6 id:{},success",id);


				} catch (Exception e) {
					logger.error("sendMessageServer error,id:{},exception:{}",request.getId(),e.getMessage());
					e.printStackTrace();
				}
			}
		};
		if (pool.isShutdown()) {

			logger.error("sendMessageServer pool is shutdown,id:{}",request.getId());
			throw new RuntimeException("通道已关闭");
		}
		pool.submit(thread);
	}
}
