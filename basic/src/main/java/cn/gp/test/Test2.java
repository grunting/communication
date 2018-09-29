package cn.gp.test;

import cn.gp.core.Server;
import cn.gp.server.GroupServer;
import cn.gp.server.RegisterServer;

/**
 * Created by gaopeng on 2018/9/29.
 */
public class Test2 {
    public static void main(String[] args) {

        Server server = Server.getInstance();

        server.putServiceInterFace(GroupServer.class.getName(),GroupServerImpl2.class);
        server.putServiceInterFace(RegisterServer.class.getName(),RegisterServerImpl2.class);

        GroupServerImpl2.server = server;
        RegisterServerImpl2.server = server;


    }
}
