package cn.gp.handler;

import cn.gp.model.Request;
import cn.gp.service.impl.FileStreamServerImpl;
import cn.gp.service.impl.RegisterServerImpl;
import cn.gp.service.impl.SendMessageServerImpl;
import cn.gp.service.FileStreamServer;
import cn.gp.service.RegisterServer;
import cn.gp.service.SendMessageServer;
import io.netty.channel.Channel;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
        private Request request;

        public Block(Channel channel,Request request) {
            this.channel = channel;
            this.request = request;
        }

        public Channel getChannel() {
            return channel;
        }

        public Request getRequest() {
            return request;
        }
    }

    // 远程调用会用到的服务实现
    private static ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

    static {

        // 注册的服务
        servers.put(RegisterServer.class.getName(), RegisterServerImpl.class);
        servers.put(SendMessageServer.class.getName(), SendMessageServerImpl.class);
        servers.put(FileStreamServer.class.getName(), FileStreamServerImpl.class);

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
                        Request request = block.getRequest();

                        Integer id = request.getId();
                        String serviceName = request.getServiceName();
                        String methodName = request.getMethodName();
                        Class<?>[] parameterTypes = request.getParameterTypes();
                        Object[] arguments = request.getArguments();

                        Class serviceClass = servers.get(serviceName);

                        Class<?>[] argsClass = new Class<?>[1];
                        argsClass[0] = Channel.class;
                        Object[] args = new Object[1];
                        args[0] = channel;

                        Constructor cons = serviceClass.getConstructor(argsClass);
                        Method method = serviceClass.getMethod(methodName,parameterTypes);

                        Object result = method.invoke(cons.newInstance(args),arguments);

                        request = new Request();
                        request.setId(id);
                        request.setResult(result);

                        ChannelHandler.sendFinal(request,channel);
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
     * @param request 发送的信息
     */
    protected synchronized static void sendMessage(Channel channel,Request request) {
        try {
            Block block = new Block(channel,request);

            while(!sendQueue.offer(block)) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
