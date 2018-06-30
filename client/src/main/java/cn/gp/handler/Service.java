package cn.gp.handler;

import cn.gp.proto.Order;
import cn.gp.service.FileStreamImpl;
import cn.gp.service.ReportImpl;
import cn.gp.service.SendMessageImpl;
import cn.gp.service.impl.FileStream;
import cn.gp.service.impl.Report;
import cn.gp.service.impl.SendMessage;
import cn.gp.util.ByteAndObject;
import com.google.protobuf.ByteString;
import io.netty.channel.Channel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 处理远端发送过来的请求(实现本地rpc服务端)
 */
public class Service {

    // 远程调用会用到的参数
    private static ConcurrentHashMap<String,Class> args = new ConcurrentHashMap<String, Class>();

    // 远程调用会用到的服务实现
    private static ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

    static {

        // 注册的服务
        servers.put(Report.class.getName(), ReportImpl.class);
        servers.put(SendMessage.class.getName(), SendMessageImpl.class);
        servers.put(FileStream.class.getName(), FileStreamImpl.class);

        // 实现服务时需要的参数
        args.put(String.class.getName(), String.class);
        args.put(ByteString.class.getName(),ByteString.class);
        args.put(byte.class.getName(),byte.class);
        args.put(byte[].class.getName(),byte[].class);
    }

    // 远端通道
    private static Channel channel;

    // 自身需要处理的队列,是全局唯一的
    private static ConcurrentLinkedQueue<Order.Message> sendQueue = new ConcurrentLinkedQueue<Order.Message>();

    /**
     * 处理远端发来的请求,客户端全局唯一
     * @param channel 通道
     */
    public void start(Channel channel) {
        Service.channel = channel;
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    while(true) {
                        while(sendQueue.isEmpty()) {
                            Thread.sleep(10);
                        }

                        Order.Message message = sendQueue.poll();


                        // 对调用者的准备,包括服务名、参数类型数组、调用函数、参数数组,调用完成后的返回
                        String id = message.getRandom();

                        String serviceName = message.getServiceName();
                        String methodName = message.getMethodName();

                        Map<String,ByteString> map = message.getMapargsMap();

                        Class<?>[] parameterTypes = new Class<?>[map.keySet().size()];
                        Object[] arguments = new Object[map.keySet().size()];
                        for(String key : map.keySet()) {
                            String[] split = key.split("_");

                            parameterTypes[Integer.parseInt(split[0])] = args.get(split[1]);

                            arguments[Integer.parseInt(split[0])] = ByteAndObject.toObject(map.get(key).toByteArray());
                        }

                        Class serviceClass = servers.get(serviceName);

                        Constructor cons = serviceClass.getConstructor();
                        Method method = serviceClass.getMethod(methodName,parameterTypes);

                        Object result = method.invoke(cons.newInstance(),arguments);

                        Order.Message.Builder builder = Order.Message.newBuilder();
                        builder.setRandom(id);
                        builder.setReturn(ByteString.copyFrom(ByteAndObject.toByArray(result)));

                        ChannelHandler.sendFinal(builder,Service.channel);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
    }

    /**
     * 远端发送过来需要自身处理的信息
     * @param message 信息
     */
    protected static void sendMessage(Order.Message message) {
        try {
            while(!sendQueue.offer(message)) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
