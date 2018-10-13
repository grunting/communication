package cn.gp.banana;

import cn.gp.core.impl.ServerBasic;
import cn.gp.server.GroupServer;
import cn.gp.server.RegisterServer;
import cn.gp.server.impl.ChannelHookImpl;
import cn.gp.server.impl.GroupServerImpl;
import cn.gp.server.impl.RegisterServerImpl;
import cn.gp.service.ChannelHook;

/**
 * 服务器
 */
public class Server {

    public static void main(String[] args) throws Exception {

        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                ServerBasic serverBasic = new ServerBasic();
                serverBasic.putServiceInterface(RegisterServer.class.getName(), RegisterServerImpl.class);
                serverBasic.putServiceInterface(GroupServer.class.getName(), GroupServerImpl.class);
                ChannelHook channelHook = new ChannelHookImpl(serverBasic);
                serverBasic.setChannelHook(channelHook);

                serverBasic.start();
            }
        };
        thread.start();

    }
}
