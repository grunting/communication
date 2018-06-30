package cn.gp.handler;

import cn.gp.proto.Order;
import cn.gp.util.ByteAndObject;
import com.google.protobuf.ByteString;
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

    // 获取命令号(为处理返回提供唯一标记)
    private static AtomicInteger atomicInteger = new AtomicInteger(1);

    // 通道
    private static Channel channel;

    // 发送给远端的队列,是全局唯一的
    private static ConcurrentLinkedQueue<Order.Message.Builder> sendQueue = new ConcurrentLinkedQueue<Order.Message.Builder>();

    // 缓存结果的地方,是全局唯一的
    private static ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer, Object>();
    // 暂时处理返回null的结果,是全局唯一的(void的函数会返回null)
    private static ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();

    /**
     * 处理对远程的请求,客户端是惟一的
     * @param channel 通道
     */
    public void start(Channel channel) {
        Remote.channel = channel;
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    while(true) {
                        while(sendQueue.isEmpty()) {
                            Thread.sleep(10);
                        }

                        Order.Message.Builder message = sendQueue.poll();

                        ChannelHandler.sendFinal(message,Remote.channel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
    }

    /**
     * 将需要发送的消息交给队列
     * @param message 消息
     */
    public static void sendMessage(Order.Message.Builder message) {
        try {
            while(!sendQueue.offer(message)) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Object[] arguments = args;

                Order.Message.Builder builder = Order.Message.newBuilder();
                builder.setServiceName(serviceName);
                builder.setMethodName(methodName);
                for(int i = 0;i < arguments.length;i ++) {
                    builder.putMapargs(i + "_" + parameterTypes[i].getName(), ByteString.copyFrom(ByteAndObject.toByArray(arguments[i])));
                }

                Integer id = atomicInteger.getAndAdd(1);
                builder.setRandom(id + "");


                sendMessage(builder);

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
