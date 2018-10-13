package cn.gp.handler;

import cn.gp.core.impl.SimpleBasic;
import cn.gp.model.Request;
import cn.gp.service.CheckReadyHook;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发送rpc任务给远端
 */
public class Remote {

    private static final Logger logger = LoggerFactory.getLogger(Remote.class);

    private SimpleBasic simpleBasic;
    private CheckReadyHook checkReadyHook;
    private AtomicInteger atomicInteger = new AtomicInteger(1);
    private ExecutorService pool = Executors.newCachedThreadPool();
    private ConcurrentMap<Integer,Object> result = new ConcurrentHashMap<Integer,Object>();
    private ConcurrentSet<Integer> result2 = new ConcurrentSet<Integer>();
    private ConcurrentMap<Integer,Exception> intermediate = new ConcurrentHashMap<Integer,Exception>();

    /**
     * 初始化
     * @param simpleBasic 节点信息
     */
    public Remote(SimpleBasic simpleBasic,CheckReadyHook checkReadyHook) {
        this.simpleBasic = simpleBasic;
        this.checkReadyHook = checkReadyHook;
        Runtime.getRuntime().addShutdownHook(new Thread(){
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
            pool.shutdown();
        }
    }

    /**
     * 获得执行结果
     * @param index id序列号
     * @param o 内容
     */
    protected void setResult(Integer index,Object o) {
        if (o == null) {
            result2.add(index);
        } else {
            result.put(index,o);
        }
    }

    /**
     * 实例化接口
     * @param serviceInterface 接口名
     * @param channel 通道
     * @param sleepTime 等待返回值的检测间隔时间
     * @param retry 等待返回值的检测次数
     * @param <T> 接口
     * @return 返回接口实例
     */
    public <T> T getRemoteProxyObj(final Class<?> serviceInterface, final Channel channel, final long sleepTime, final int retry) {

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

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {

                            if (checkReadyHook != null) {

                                logger.debug("checkReadyHook's check,service:{},priority:{}",serviceInterface,checkReadyHook.getCheckReadyHookList());

                                if (checkReadyHook.getCheckReadyHookList().contains(serviceInterface)) {
                                    while(!simpleBasic.getSimpleChannel().isLink()) {
                                        Thread.sleep(100);
                                    }
                                } else {
                                    while(!simpleBasic.getSimpleChannel().isReady()) {
                                        Thread.sleep(100);
                                    }
                                }
                            }

                            ChannelFuture channelFuture;
                            int retry = 5;
                            while((channelFuture = ChannelHandler.sendFinalChannelFuture(request,channel)) == null) {
                                try {
                                    Thread.sleep(100);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                };
                pool.submit(thread);

                Object o = null;

                int single = retry;
                while(true) {
                    Thread.sleep(sleepTime);

                    if (single < 1) {
                        break;
                    }
                    single --;

                    if(result.containsKey(id)) {
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

                return o;
            }
        });
    }


}
