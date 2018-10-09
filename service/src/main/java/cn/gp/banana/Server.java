package cn.gp.banana;

import cn.gp.core.impl.ServerNetty;
import cn.gp.server.GroupServer;
import cn.gp.server.RegisterServer;
import cn.gp.server.impl.ChannelHookImpl;
import cn.gp.server.impl.GroupServerImpl;
import cn.gp.server.impl.RegisterServerImpl;
import cn.gp.test.PrintStatus;
import cn.gp.test.PrintTraffic;

/**
 * 服务器测试类
 */
public class Server {
	public static void main(String[] args) {
		ServerNetty server = new ServerNetty();
		server.setChannelHook(new ChannelHookImpl(server));

		server.setConfigPath("basic.properties","server.conf");
		server.init();

		server.putServiceInterface(GroupServer.class.getName(),GroupServerImpl.class);
		server.putServiceInterface(RegisterServer.class.getName(),RegisterServerImpl.class);

		server.start();

		System.out.println("start and ready");
//		PrintTraffic.printTraffic(server.getGlobalTrafficShapingHandler());
//		PrintStatus.printStatus(server);
	}
}
