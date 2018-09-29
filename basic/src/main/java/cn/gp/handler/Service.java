package cn.gp.handler;

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

	private Channel channel;

	private ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

	private ExecutorService pool = Executors.newCachedThreadPool();

	public Service() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				super.run();
				pool.shutdown();
			}
		});
	}

	public void close() {
		pool.shutdown();
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void putServers(String key, Class value) {
		this.servers.put(key,value);
	}

	/**
	 * 远端发送过来需要自身处理的信息
	 * @param request 信息
	 */
	protected void sendMessage(final Request request) {
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

					Constructor cons = serviceClass.getConstructor();
					Method method = serviceClass.getMethod(methodName,parameterTypes);

					Object result = method.invoke(cons.newInstance(),arguments);

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
