package cn.gp.handler;

import cn.gp.model.Basic;
import cn.gp.model.Request;
import io.netty.util.internal.ConcurrentSet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送rpc请求给远端
 */
public class Remote {

    // 获取命令号(为处理返回提供唯一标记)
    private static AtomicInteger atomicInteger = new AtomicInteger(1);

    // 发送给远端的队列,是全局唯一的
    private static ExecutorService pool = Executors.newCachedThreadPool();

    // 缓存结果的地方,是全局唯一的
    private static ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer, Object>();
    // 暂时处理返回null的结果,是全局唯一的(void的函数会返回null)
    private static ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                pool.shutdown();
            }
        });
    }

    /**
     * 设置回应(只有响应端会调用)
     * @param index 消息标号
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
     * rpc实体,会生成rpc请求给远端
     * @param serviceInterface 接口类
     * @param <T> 强转为接口类型
     * @return 返回接口实例
     */
    public static <T> T getRemoteProxyObj(final Class<?> serviceInterface) {

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
                        ChannelHandler.sendFinal(request, Basic.getChannel());
                    }
                };
                pool.submit(thread);

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
