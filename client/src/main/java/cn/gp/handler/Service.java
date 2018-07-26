package cn.gp.handler;

import cn.gp.model.Basic;
import cn.gp.model.Request;
import cn.gp.service.*;
import cn.gp.service.impl.FileStreamImpl;
import cn.gp.service.impl.GroupImpl;
import cn.gp.service.impl.ReportImpl;

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
        servers.put(Report.class.getName(), ReportImpl.class);
        servers.put(FileStream.class.getName(), FileStreamImpl.class);
        servers.put(Group.class.getName(), GroupImpl.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                super.run();
                pool.shutdown();
            }
        });

    }

    /**
     * 远端发送过来需要自身处理的信息
     * @param request 信息
     */
    protected static void sendMessage(final Request request) {

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

                    ChannelHandler.sendFinal(request1, Basic.getChannel());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        pool.submit(thread);
    }
}
