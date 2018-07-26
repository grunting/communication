package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.Group;
import cn.gp.service.GroupServer;
import io.netty.channel.Channel;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对分组操作的实现
 */
public class GroupServerImpl implements GroupServer {

    // 分组id的分配
    private static final AtomicInteger integer = new AtomicInteger(1);

    // 发起者的通道
    private Channel channel;

    public GroupServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 创建分组申请
     * @param group 用户列表以及其对应的加密密码
     * @return 分组id
     */
    public String createGroup(Map<String,byte[]> group){

        Set<ClientBean> set = new HashSet<ClientBean>();

        for (String name : group.keySet()) {
            set.addAll(Basic.getIndex().getNode("names",name));
        }

        String id = "group" + integer.getAndAdd(1);

        for (ClientBean clientBean : set) {
            Group target = Remote.getRemoteProxyObj(Group.class,clientBean.getChannel());
            boolean b = target.recoveGroup(id,group.get(clientBean.getName()));
            if (b) {
                Basic.getIndex().setIndex(id,clientBean.getName(),clientBean);
            }
        }

        if (Basic.getIndex().getNode(id).isEmpty()) {
            return null;
        } else {
            ClientBean clientBean = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();
            Basic.getIndex().setIndex(id,clientBean.getName(),clientBean);

            return id;
        }
    }

    /**
     * 查看指定分组内成员列表
     * @param groupId 分组id
     * @return 返回用户列表
     */
    public Set<String> showGroupUsers(String groupId) {

        Set<ClientBean> set = Basic.getIndex().getNode(groupId);
        Set<String> result = new HashSet<String>();
        for (ClientBean clientBean : set) {
            result.add(clientBean.getName() + ":" + clientBean.getChannelId());
        }

        return result;
    }

    /**
     * 发送信息
     * @param groupId 分组id
     * @param message 消息
     * @return 返回发送结果
     */
    public boolean sendMessage(String groupId, byte[] message) {

        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();

        int count = 0;
        for (ClientBean clientBean : Basic.getIndex().getNode(groupId)) {
            if (!clientBean.equals(clientBeanSelf)) {
                Group group = Remote.getRemoteProxyObj(Group.class,clientBean.getChannel());
                if (group.recoveMessage(groupId,clientBeanSelf.getName(),message)) {
                    count ++;
                }
            }
        }

        return count > 0;
    }
}
