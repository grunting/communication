package cn.gp.handler;

import cn.gp.channel.SimpleChannel;
import cn.gp.core.impl.SimpleBasic;
import cn.gp.model.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 本地需要处理的信息
 */
public class LocalProcess {

    private static final Logger logger = LoggerFactory.getLogger(LocalProcess.class);

    private SimpleBasic simpleBasic;
    private ConcurrentHashMap<String,Class> servers = new ConcurrentHashMap<String, Class>();
    private ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * 需要本节点实例做实例化
     * @param simpleBasic 当前节点
     */
    public LocalProcess(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                super.run();
                if(!pool.isShutdown()) {
                    pool.isShutdown();
                }
            }
        });
    }

    /**
     * 关闭函数
     */
    public void close() {
        if (!pool.isShutdown()) {
            pool.shutdown();
        }
    }

    /**
     * 返回本地的服务
     * @param key 服务名(接口全地址)
     */
    public Class getServers(String key) {
        return this.servers.get(key);
    }

    /**
     * 设置本地服务
     * @param key 服务名
     * @param value 服务的实现实例
     */
    public void putServers(String key, Class value) {
        this.servers.put(key,value);
    }

    /**
     * 远端发送过来需要自身处理的信息
     * @param channel 通道
     * @param request 信息
     */
    public void sendMessage(final Channel channel, final Request request) {

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

                    Class<?>[] argsClass = new Class[0];
                    Constructor[] cons = serviceClass.getConstructors();
                    for (Constructor con : cons) {
                        argsClass = con.getParameterTypes();
                    }
                    Object[] args = new Object[argsClass.length];
                    for (int i = 0;i < argsClass.length;i ++) {

                        String ba = SimpleBasic.class.getName();
                        if (ba.equals(argsClass[i].getName())) {
                            args[i] = simpleBasic;
                        }

                        ba = SimpleChannel.class.getName();
                        if (ba.equals(argsClass[i].getName())) {
                            args[i] = simpleBasic.getSimpleChannel();
                        }

                        ba = Channel.class.getName();
                        if (ba.equals(argsClass[i].getName())) {
                            args[i] = channel;
                        }
                    }

                    logger.debug("start local mission,request:{},class:{}",request.toString(),serviceClass);

                    Constructor constructor = serviceClass.getDeclaredConstructor(argsClass);
                    Method method = serviceClass.getMethod(methodName,parameterTypes);
                    Object result = method.invoke(constructor.newInstance(args),arguments);

                    Request request1 = new Request();
                    request1.setId(id);
                    request1.setResult(result);

                    ChannelFuture channelFuture;
                    while((channelFuture = ChannelHandler.sendFinalChannelFuture(request1,channel)) == null) {
                        Thread.sleep(10);
                    }

                    channelFuture.get();
                    logger.debug("local mission success,request:{}",request1.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

        };
        pool.submit(thread);
    }
}
