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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 处理远端发送过来的请求(实现本地rpc服务端)
 */
public class Service {

    // 远程调用会用到的服务实现
    private static ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();

    private static ExecutorService pool = Executors.newCachedThreadPool();

    static {

        // 注册的服务
        servers.put(RegisterServer.class.getName(), RegisterServerImpl.class);
        servers.put(SendMessageServer.class.getName(), SendMessageServerImpl.class);
        servers.put(FileStreamServer.class.getName(), FileStreamServerImpl.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                pool.shutdown();
            }
        });

    }

    /**
     * 自身需要处理的信息
     * @param channel 通道
     * @param request 发送的信息
     */
    protected synchronized static void sendMessage(final Channel channel, final Request request) {
        try {

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
                        argsClass[0] = Channel.class;
                        Object[] args = new Object[1];
                        args[0] = channel;

                        Constructor cons = serviceClass.getConstructor(argsClass);
                        Method method = serviceClass.getMethod(methodName,parameterTypes);

                        Object result = method.invoke(cons.newInstance(args),arguments);

                        Request request1 = new Request();
                        request1.setId(id);
                        request1.setResult(result);

                        ChannelHandler.sendFinal(request1,channel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            pool.submit(thread);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
