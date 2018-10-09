package cn.gp.client.impl;

import cn.gp.core.Basic;
import cn.gp.crypto.RSA;
import cn.gp.model.Friend;
import cn.gp.server.RegisterServer;
import cn.gp.client.Report;
import cn.gp.util.IndexTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 实现上报功能
 */
public class ReportImpl implements Report {

	private static final Logger logger = LoggerFactory.getLogger(ReportImpl.class);

	private Basic basic;

	private static IndexTest<Friend> index = new IndexTest<Friend>();

	public ReportImpl(Basic basic) {
		this.basic = basic;
	}

	public static IndexTest<Friend> getIndex() {
		return index;
	}

	/**
	 * 丢失客户端公告
	 * @param channelId 丢失的名称
	 */
	public void lostClient(String channelId) {

		for (Friend friend : index.getNode("channelid",channelId)) {
			if (GroupImpl.passageWays.containsKey(friend.getName())) {
				GroupImpl.passageWays.remove(friend.getName());
			}
			friend.setDie();
			logger.info("friend lost,friend:{}",friend);
		}
	}

	/**
	 * 发现客户端公告
	 * @param name 名称
	 * @param channelId 通信唯一id
	 */
	public void findClient(String name,String channelId) {

		if (!basic.getTrustMap().containsKey(name)) {
			return;
		}

		Friend friend = new Friend(channelId,name,basic.getTrustMap().get(name),index);

		index.setIndex("names",name,friend);
		index.setIndex("channelid",channelId,friend);

		logger.info("friend find,friend:{}",friend);
	}

	/**
	 * 发送本客户端信息给服务器
	 */
	public boolean send() throws Exception {

		RegisterServer registerServer = basic.getRemoteProxyObj(RegisterServer.class);

		// 目前签名中只发布自身名字的签名(通道是加密的)
		byte[] crypto = RSA.encrypt(basic.getName().getBytes(),basic.getKeyPair().getPrivate());

		boolean b = registerServer.addClient(basic.getName(),crypto);

		logger.info("friend report,result:{}",b);

		return b;
	}

	/**
	 * 获取当前客户端总数
	 * @return 数量
	 */
	public int count() {
		return index.getAllNode().size();
	}
}
