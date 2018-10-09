package cn.gp.test;

import cn.gp.banana.Client;
import cn.gp.core.impl.ServerNetty;
import cn.gp.model.ClientBean;
import cn.gp.server.RegisterServer;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 一个打印当前读写总量的工具
 */
public class PrintStatus {

    private static final Logger logger = LoggerFactory.getLogger(PrintStatus.class);

    public static void printStatus(final ServerNetty serverNetty) {
        new Thread(new Runnable() {

            public void run() {

                RegisterServer registerServer = serverNetty.getRemoteProxyObj(RegisterServer.class);
                int i = 0;
                while(true) {
                    Set<ClientBean> clientBeanSet = registerServer.getClientBean();
                    i ++;
                    for (ClientBean clientBean : clientBeanSet) {
                        if (clientBean.getChannel() != null) {
                            logger.info(i + " name is "
                                    + clientBean.getName() + " active is "
                                    + clientBean.getChannel().isActive() + " open is "
                                    + clientBean.getChannel().isOpen() + " registered is "
                                    + clientBean.getChannel().isRegistered() + " writable is "
                                    + clientBean.getChannel().isWritable());
                        } else {
                            logger.info(i + " channel is null");
                        }
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
