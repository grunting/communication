package cn.gp.handler;

import cn.gp.model.Request;
import io.netty.channel.Channel;
import io.netty.util.internal.ConcurrentSet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送rpc请求给远端
 */
public class Remote {

    // 缓存结果的地方
    private static ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer, Object>();
    // 暂时处理返回null的结果
    private static ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();

    // 获取命令号
    private static AtomicInteger atomicInteger = new AtomicInteger(1);

    /**
     * 每个客户端运行一个
     * @param channel 通道
     * @param sendQueue 需要处理的队列
     */
    public void start(final Channel channel, final ConcurrentLinkedQueue<Request> sendQueue) {
        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();
                try {
                    while(true) {
                        while(sendQueue.isEmpty()) {
                            Thread.sleep(10);
                        }

                        Request request = sendQueue.poll();
                        ChannelHandler.sendFinal(request, channel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    /**
     * 设置回应
     * @param index id序号
     * @param o 内容
     */
    protected static void setResult(Integer index,Object o) {
        if(o == null) {
            result2.add(index);
        } else {
            result.put(index,o);
        }
    }

    /**
     * 实现远程调用
     * @param serviceInterface 接口类
     * @param sendQueue 处理该消息的队列
     * @param <T> 强转为接口类型
     * @return 返回接口实例
     */
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface, final ConcurrentLinkedQueue<Request> sendQueue) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                String serviceName = serviceInterface.getName();
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();

                Request request = new Request();
                request.setServiceName(serviceName);
                request.setMethodName(methodName);
                request.setParameterTypes(parameterTypes);
                request.setArguments(args);

                Integer id = atomicInteger.getAndAdd(1);
                request.setId(id);

                sendQueue.offer(request);

                Object o;
                while(true) {
                    Thread.sleep(5);
                    if(result.containsKey(id)) {
                        o = result.remove(id);
                        break;
                    }
                    if(result2.contains(id)) {
                        result2.remove(id);
                        o = null;
                        break;
                    }
                }

                return o;
            }
        });
    }
}
