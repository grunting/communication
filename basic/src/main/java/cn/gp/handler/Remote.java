package cn.gp.handler;

import cn.gp.model.Request;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送rpc请求给远端
 */
public class Remote {

	// 命令序号生成器
	private AtomicInteger atomicInteger = new AtomicInteger(1);

	// 发送给远端队列需要执行的任务
	private ExecutorService pool = Executors.newCachedThreadPool();

	// 缓存结果
	private ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer,Object>();

	// 暂存返回null的结果
	private ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();

	public Remote() {
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

	protected void setResult(Integer index, Object o) {
		if (o == null) {
			result2.add(index);
		} else {
			result.put(index,o);
		}
	}

	public <T> T getRemoteProxyObj(final Class<?> serviceInterface,final Channel channel) {

		return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				String serviceName = serviceInterface.getName();
				String methodName = method.getName();
				Class<?>[] parameterTypes = method.getParameterTypes();

				final Request request = new Request();
				request.setServiceName(serviceName);
				request.setMethodName(methodName);
				request.setParameterTypes(parameterTypes);
				request.setArguments(args);

				Integer id = atomicInteger.getAndAdd(1);
				request.setId(id);

				Thread thread = new Thread() {
					@Override
					public void run() {
						super.run();
						try {
							ChannelHandler.sendFinalChannelFuture(request,channel);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				pool.submit(thread);

				Object o = null;
				while(true) {
					Thread.sleep(5);
					if (result.containsKey(id)) {
						o = result.remove(id);
						break;
					}
					if (result2.contains(id)) {
						result2.remove(id);
						break;
					}
				}

				return o;
			}
		});
	}


}
