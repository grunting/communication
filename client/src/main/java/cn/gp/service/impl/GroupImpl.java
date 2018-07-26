package cn.gp.service.impl;

import cn.gp.crypto.AES;
import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.service.Group;
import cn.gp.service.GroupServer;

import java.util.*;

/**
 * 客户端分组实现
 */
public class GroupImpl implements Group {

    public Set<String> showSelfGroups() {
        return Basic.getGroups().keySet();
    }

    public Set<String> showGroupUsers(String groupId) {

        GroupServer groupServer = Remote.getRemoteProxyObj(GroupServer.class);

        return groupServer.showGroupUsers(groupId);
    }

    /**
     * 发起分组请求
     * @param names 分组列表
     * @return 返回分组情况
     * @throws Exception 加密可能出错
     */
    public boolean createGroup(List<String> names) throws Exception {

        GroupServer groupServer = Remote.getRemoteProxyObj(GroupServer.class);

        byte[] key = AES.getKey();

        Map<String,byte[]> map = new HashMap<String,byte[]>();

        for (String name : names) {
            Set<Friend> set = Basic.getIndexTest().getNode("names",name);
            for (Friend friend : set) {
                map.put(name,RSA.encrypt(key,friend.getKey()));
                break;
            }
        }

        String groupId = groupServer.createGroup(map);

        if (groupId == null) {
            return false;
        } else {
            System.out.println();
            System.out.println("创建分组:" + groupId);
            System.out.print("order:");

            Basic.getGroups().put(groupId,key);
            return true;
        }
    }

    /**
     * 回应其他客户端的分组要求
     * @param groupId 分组id
     * @param crypto 秘钥
     * @return 返回接收成功与否
     */
    public boolean recoveGroup(String groupId, byte[] crypto) {

        try {
            byte[] real = RSA.decrypt(crypto,Basic.getKeyPair().getPrivate());

            Basic.getGroups().put(groupId,real);
            System.out.println();
            System.out.println("加入分组:" + groupId);
            System.out.print("order:");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMessage(String groupId, byte[] message) {
        GroupServer groupServer = Remote.getRemoteProxyObj(GroupServer.class);

        AES aes = new AES(Basic.getGroups().get(groupId));
        byte[] crypto = aes.encode(message);

        return groupServer.sendMessage(groupId,crypto);
    }

    /**
     * 接收消息
     * @param groupId 分组id
     * @param sparker 发送者
     * @param message 内容
     * @return 返回接收成功与否
     */
    public boolean recoveMessage(String groupId, String sparker, byte[] message) {

        AES aes = new AES(Basic.getGroups().get(groupId));
        byte[] realMessage = aes.decode(message);

        System.out.println();
        System.out.println(groupId + ":" + sparker + ":" + new String(realMessage));
        System.out.print("order:");

        return true;
    }
}
