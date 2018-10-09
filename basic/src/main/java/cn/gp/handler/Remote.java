package cn.gp.handler;

import cn.gp.core.Basic;
import cn.gp.model.Request;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Constant;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送rpc请求给远端
 */
public class Remote {

	private static final Logger logger = LoggerFactory.getLogger(Remote.class);

	// 本节点
	private Basic basic;

	// 连接准备金就绪后,需要后续执行的部分,钩子函数
	private CheckReadyHook checkReadyHook;

	// 命令序号生成器
	private AtomicInteger atomicInteger = new AtomicInteger(1);

	// 发送给远端队列需要执行的任务
	private ExecutorService pool = Executors.newCachedThreadPool();

	// 缓存结果
	private ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer,Object>();

	// 暂存返回null的结果
	private ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();

	// 如果发送任务出现错误,会在这里暂存
	private ConcurrentMap<Integer,Exception> intermediate = new ConcurrentHashMap<Integer,Exception>();

	/**
	 * 需要本节点和后续钩子函数设置
	 * @param basic 本节点
	 * @param checkReadyHook 钩子函数
     */
	public Remote(Basic basic,CheckReadyHook checkReadyHook) {
		this.basic = basic;
		this.checkReadyHook = checkReadyHook;
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
		if (!pool.isShutdown()) {
			pool.shutdownNow();
		}
	}

	/**
	 * 设置返回值
	 * @param index id序号
	 * @param o 内容
     */
	protected void setResult(Integer index, Object o) {
		if (o == null) {
			result2.add(index);
		} else {
			result.put(index,o);
		}
	}

	/**
	 * 获取执行实例
	 * @param serviceInterface 服务
	 * @param channel 通道
	 * @param <T> 泛型
     * @return 返回实例
     */
	public <T> T getRemoteProxyObj(final Class<?> serviceInterface,final Channel channel) {

		return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

				final String serviceName = serviceInterface.getName();
				String methodName = method.getName();
				Class<?>[] parameterTypes = method.getParameterTypes();

				final Request request = new Request();
				request.setServiceName(serviceName);
				request.setMethodName(methodName);
				request.setParameterTypes(parameterTypes);
				request.setArguments(args);

				Integer id = atomicInteger.getAndAdd(1);
				request.setId(id);

				logger.debug("getRemoteProxyObj1 request:{},channel:{}",request,channel);

				Thread thread = new Thread() {
					@Override
					public void run() {
						super.run();
						try {

							logger.debug("getRemoteProxyObj2 id:{},checkReadyHook:{},basicIsAlive:{},basicIsReady:{},ischeckReadyHookList:{}", Arrays.asList(request.getId(),checkReadyHook,basic.getIsAlive(),basic.getIsReady(),checkReadyHook.getCheckReadyHookList().contains(serviceInterface)));

							if (checkReadyHook != null) {
								if (checkReadyHook.getCheckReadyHookList().contains(serviceInterface)) {
									while(!basic.getIsAlive()) {
										Thread.sleep(100);
									}
								} else {
									while(!basic.getIsReady()) {
										Thread.sleep(100);
									}
								}

							}

							logger.debug("getRemoteProxyObj3 readyHook is clear or basic is ready,id:{}",request.getId());

							ChannelFuture channelFuture;

							// 暂时只发送五次
							int retry = 5;
							while((channelFuture = ChannelHandler.sendFinalChannelFuture(request, channel)) == null) {

								// 判断是否达到就义的次数
								if (retry == 0) {
									break;
								}
								retry --;

								if (channel == null) {

									logger.error("getRemoteProxyObj error channel is null,id:{}",request.getId());
									intermediate.put(request.getId(),new RuntimeException("通道已死"));
									return;
								}
								if (!basic.getIsAlive()) {

									logger.error("getRemoteProxyObj error channel not activated,id:{}",request.getId());
									intermediate.put(request.getId(),new RuntimeException("服务端已关闭"));
									return;
								}
								Thread.sleep(100);
							}

							if (retry == 0) {
								logger.error("getRemoteProxyObj error retryExeceed,id:{}",request.getId());
								intermediate.put(request.getId(),new RuntimeException("id:" + request.getId() + " is falied"));
							}

							// 暂时不设置超时
							channelFuture.get();
							logger.debug("getRemoteProxyObj4 id:{},success",request.getId());

						} catch (Exception e) {

							logger.error("getRemoteProxyObj error id:{},exception:{}",request.getId(),e.getMessage());
							e.printStackTrace();
							intermediate.put(request.getId(),e);
						}
					}
				};
				if (pool.isShutdown()) {

					logger.error("sendMessageServer pool is shutdown,id:{}",request.getId());
					throw new RuntimeException("通道已关闭");
				}
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
					if (intermediate.containsKey(id)) {
						throw intermediate.remove(id);
					}
				}

				logger.debug("getRemoteProxyObj4 id:{},result:{}",request.getId(),o);

				return o;
			}
		});
	}


}
