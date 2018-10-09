package cn.gp.server.impl;

import cn.gp.client.Group;
import cn.gp.core.Basic;
import cn.gp.model.ClientBean;
import cn.gp.server.GroupServer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分组接口
 */
public class GroupServerImpl implements GroupServer {

	private static final Logger logger = LoggerFactory.getLogger(GroupServerImpl.class);

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

		logger.debug("createGroup01stage1 name:{}",name);

		if (!RegisterServerImpl.index.contains("names",name)) {

			logger.debug("createGroup01stage2 name:{},index:{}",name,RegisterServerImpl.index.getAllNode().toString());
			return new byte[0];
		}

		ClientBean clientBeanTarget = RegisterServerImpl.index.getNode("names",name).iterator().next();
		Group group = basic.getRemoteProxyObj(Group.class,clientBeanTarget.getChannel());
		ClientBean clientBeanSelf = RegisterServerImpl.index.getNode("channelid",channel.id().asLongText()).iterator().next();

		logger.debug("createGroup01stage3 name:{}",name);
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

		logger.debug("createGroup02 name:{}",name);
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

		logger.debug("sendMessage name:{},message:{}",name,new String(message));
		return group.receiveMessage(clientBeanSelf.getName(),message);
	}
}
