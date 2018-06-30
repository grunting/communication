package cn.gp.handler;

import cn.gp.proto.Order;
import cn.gp.service.FileStreamServerImpl;
import cn.gp.service.RegisterServerImpl;
import cn.gp.service.SendMessageServerImpl;
import cn.gp.service.impl.FileStreamServer;
import cn.gp.service.impl.RegisterServer;
import cn.gp.service.impl.SendMessageServer;
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

    /**
     * 内部类,记录信息应该给哪个通道执行
     */
    private static class Block {
        private Channel channel;
        private Order.Message message;

        public Block(Channel channel,Order.Message message) {
            this.channel = channel;
            this.message = message;
        }

        public Channel getChannel() {
            return channel;
        }

        public Order.Message getMessage() {
            return message;
        }
    }

    // 远程调用会用到的参数
    private static ConcurrentHashMap<String,Class> args = new ConcurrentHashMap<String, Class>();

    // 远程调用会用到的服务实现
    private static ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

    static {

        // 注册的服务
        servers.put(RegisterServer.class.getName(), RegisterServerImpl.class);
        servers.put(SendMessageServer.class.getName(), SendMessageServerImpl.class);
        servers.put(FileStreamServer.class.getName(), FileStreamServerImpl.class);

        // 实现服务时需要的参数
        args.put(String.class.getName(), String.class);
        args.put(ByteString.class.getName(),ByteString.class);
        args.put(byte.class.getName(),byte.class);
        args.put(byte[].class.getName(),byte[].class);
    }

    private static ConcurrentLinkedQueue<Block> sendQueue = new ConcurrentLinkedQueue<Block>();

    /**
     * 服务器处理部分,只有一个
     */
    public void start() {
        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    while(true) {
                        while(sendQueue.isEmpty()) {
                            Thread.sleep(10);
                        }

                        Block block = sendQueue.poll();

                        Channel channel = block.getChannel();

                        Order.Message message = block.getMessage();

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

                        Class<?>[] argsClass = new Class<?>[1];
                        argsClass[0] = Channel.class;

                        Object[] args = new Object[1];
                        args[0] = channel;

                        Constructor cons = serviceClass.getConstructor(argsClass);
                        Method method = serviceClass.getMethod(methodName,parameterTypes);

                        Object result = method.invoke(cons.newInstance(args),arguments);

                        Order.Message.Builder builder = Order.Message.newBuilder();
                        builder.setRandom(id);
                        builder.setReturn(ByteString.copyFrom(ByteAndObject.toByArray(result)));

                        ChannelHandler.sendFinal(builder,channel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
    }

    /**
     * 自身需要处理的信息
     * @param channel 通道
     * @param message 发送的信息
     */
    protected synchronized static void sendMessage(Channel channel,Order.Message message) {
        try {
            Block block = new Block(channel,message);

            while(!sendQueue.offer(block)) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
