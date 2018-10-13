package cn.gp.banana;

import cn.gp.client.Group;
import cn.gp.client.Report;
import cn.gp.client.impl.CheckReadyHookImpl;
import cn.gp.client.impl.GroupImpl;
import cn.gp.client.impl.ReportImpl;
import cn.gp.core.impl.ClientBasic;
import cn.gp.service.CheckReadyHook;

/**
 * 客户端
 */
public class Client {

    private static volatile Client client;

    private volatile ClientBasic clientBasic;

    private Client() {
        super();
    }

    public static Client getInstance() {
        if (client == null) {
            synchronized (Client.class) {
                if (client == null) {

                    Client client1 = new Client();
                    client1.clientBasic = new ClientBasic();
                    client1.clientBasic.putServiceInterface(Report.class.getName(), ReportImpl.class);
                    client1.clientBasic.start();

                    client = client1;
                }
            }
        }
        return client;
    }


    public static void main(String[] args) throws Exception {

        Thread thread1 = new Thread() {
            @Override
            public void run() {
                super.run();
                ClientBasic clientBasic = new ClientBasic();
                clientBasic.putServiceInterface(Report.class.getName(), ReportImpl.class);
                clientBasic.putServiceInterface(Group.class.getName(), GroupImpl.class);

                CheckReadyHook checkReadyHook = new CheckReadyHookImpl(clientBasic);

                clientBasic.setCheckReadyHook(checkReadyHook);
                clientBasic.start();

                try {
                    while(true) {
                        Thread.sleep(1000);
                        Report report = clientBasic.getRemoteProxyObj(Report.class);
                        System.out.println(clientBasic.getSimpleChannel().isReady() +" friends:" + report.getAllFriends().toString());
                        if (clientBasic.getSimpleChannel().isLink()) {
//                            Group group = clientBasic.getRemoteProxyObj(Group.class);
//                            System.out.println("send message to client1 ,status:" + group.sendMessage("client1","my name is client2"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread1.start();
    }

}
