package cn.gp.test;

import cn.gp.core.Client;
import cn.gp.crypto.RSA;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;

/**
 * 实现上报功能
 */
public class ReportImpl implements Report {

	/**
	 * 丢失客户端公告
	 * @param channelId 丢失的名称
	 */
	public void lostClient(String channelId) {

		System.out.println(channelId);
	}

	/**
	 * 发现客户端公告
	 * @param name 名称
	 * @param channelId 通信唯一id
	 */
	public void findClient(String name,String channelId) {

		System.out.println(name + " : " + channelId);

	}

	/**
	 * 发送本客户端信息给服务器
	 */
	public static void send(Client client) throws Exception {

		RegisterServer registerServer = client.getRemoteProxyObj(RegisterServer.class);

		// 目前签名中只发布自身名字的签名(通道是加密的)

		byte[] crypto = RSA.encrypt(client.getName().getBytes(),client.getKeyPair().getPrivate());

		boolean b = registerServer.addClient(client.getName(),crypto);
		if(!b) {
			System.out.println("服务器拒绝了本节点的注册");
			System.exit(2);
		}
	}
}
