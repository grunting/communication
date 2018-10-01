package cn.gp.handler;

import cn.gp.core.Basic;
import cn.gp.model.Request;
import io.netty.channel.Channel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地需要处理的信息
 */
public class Service {

	private Basic basic;

	private ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

	private ExecutorService pool = Executors.newCachedThreadPool();

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

	public void close() {
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

	public void putServers(String key, Class value) {
		this.servers.put(key,value);
	}

	/**
	 * 远端发送过来需要自身处理的信息(客户端版本)
	 * @param request 信息
	 */
	public void sendMessageClient(final Channel channel,final Request request) {
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

					Class serviceClass = servers.get(serviceName);

					Class<?>[] argsClass = new Class<?>[1];
					argsClass[0] = Basic.class;

					Object[] args = new Object[1];
					args[0] = basic;

					Constructor cons = serviceClass.getConstructor(argsClass);
					Method method = serviceClass.getMethod(methodName,parameterTypes);

					Object result = method.invoke(cons.newInstance(args),arguments);

					Request request1 = new Request();
					request1.setId(id);
					request1.setResult(result);

					while(ChannelHandler.sendFinalChannelFuture(request1, channel) == null) {
						Thread.sleep(100);
					}
				} catch (Exception e) {
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

					Class serviceClass = servers.get(serviceName);

					Class<?>[] argsClass = new Class<?>[2];
					argsClass[0] = Channel.class;
					argsClass[1] = Basic.class;

					Object[] args = new Object[2];
					args[0] = channel;
					args[1] = basic;

					Constructor cons = serviceClass.getConstructor(argsClass);

					Method method = serviceClass.getMethod(methodName,parameterTypes);

					Object result = method.invoke(cons.newInstance(args),arguments);

					Request request1 = new Request();
					request1.setId(id);
					request1.setResult(result);

					while(ChannelHandler.sendFinalChannelFuture(request1, channel) == null) {
						Thread.sleep(100);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		pool.submit(thread);
	}
}
