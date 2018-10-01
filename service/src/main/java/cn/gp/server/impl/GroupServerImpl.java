package cn.gp.server.impl;

import cn.gp.client.Group;
import cn.gp.core.Basic;
import cn.gp.model.ClientBean;
import cn.gp.server.GroupServer;
import io.netty.channel.Channel;

/**
 * 分组接口
 */
public class GroupServerImpl implements GroupServer {

	// 发起者的通道
	private Channel channel;

	private Basic basic;

	public GroupServerImpl(Channel channel,Basic basic){
		this.channel = channel;
		this.basic = basic;
	}

	/**
	 * 创建分组第一步
	 * @param name 对方的名称
	 * @param crypto 加密内容
	 * @return 返回对方的响应信息
	 * @throws Exception
	 */
	public byte[] createGroup01(String name, byte[] crypto) throws Exception {

		if (!RegisterServerImpl.index.contains("names",name)) {
			return new byte[0];
		}

		ClientBean clientBeanTarget = RegisterServerImpl.index.getNode("names",name).iterator().next();
		Group group = basic.getRemoteProxyObj(Group.class,clientBeanTarget.getChannel());
		ClientBean clientBeanSelf = RegisterServerImpl.index.getNode("channelid",channel.id().asLongText()).iterator().next();

		return group.create01(clientBeanSelf.getName(),crypto);
	}

	/**
	 * 创建分组第二步
	 * @param name 对方的名称
	 * @param crypto 加密内容
	 * @return 返回加密过的对称秘钥
	 * @throws Exception
	 */
	public byte[] createGroup02(String name, byte[] crypto) throws Exception {

		ClientBean clientBeanTarget = RegisterServerImpl.index.getNode("names",name).iterator().next();
		Group group = basic.getRemoteProxyObj(Group.class,clientBeanTarget.getChannel());
		ClientBean clientBeanSelf = RegisterServerImpl.index.getNode("channelid",channel.id().asLongText()).iterator().next();

		return group.create02(clientBeanSelf.getName(),crypto);
	}

	/**
	 * 发送信息
	 * @param name 发送目标
	 * @param message 加密信息
	 * @return 返回发送成功与否
	 * @throws Exception
	 */
	public boolean sendMessage(String name, byte[] message) throws Exception {
		ClientBean clientBeanTarget = RegisterServerImpl.index.getNode("names",name).iterator().next();
		Group group = basic.getRemoteProxyObj(Group.class,clientBeanTarget.getChannel());
		ClientBean clientBeanSelf = RegisterServerImpl.index.getNode("channelid",channel.id().asLongText()).iterator().next();

		return group.receiveMessage(clientBeanSelf.getName(),message);
	}
}
