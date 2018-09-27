package cn.gp.service.impl;

import cn.gp.client.Group;
import cn.gp.crypto.AES;
import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.server.GroupServer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多人分组接口实现
 */
public class GroupImpl implements Group {

    // 存放所有通道
    protected static final ConcurrentMap<String,Passageway> passageWays = new ConcurrentHashMap<String, Passageway>();

    /**
     * 对分组的描述
     */
    private static class Passageway {

        // 通讯确认第一阶段时的凭证
        private volatile int siz = Integer.MAX_VALUE;

        // 该分组的秘钥
        private AES aes;

    }

    /**
     * 第一步,检查对方的身份,同时发送一个验证信息出去
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回响应部分
     */
    public byte[] create01(String name,byte[] crypto) throws Exception {
        Set<Friend> friends = Basic.getIndexTest().getNode("names",name);
        if (friends == null || friends.size() < 1) {
            return new byte[0];
        }

        Friend friend = friends.iterator().next();

        byte[] real = RSA.decrypt(crypto,friend.getKey());

        int i1 = Integer.parseInt(new String(real)) + 1;
        Random random = new Random();
        int i2 = random.nextInt(Integer.MAX_VALUE) - 1;
        String tar = String.valueOf(i1) + "," + String.valueOf(i2);

        crypto = RSA.encrypt(tar.getBytes(),Basic.getKeyPair().getPrivate());
        Passageway passageway = new Passageway();
        passageway.siz = i2;


        if (passageWays.containsKey(name)) {
            return new byte[0];
        }
        synchronized (name) {
            if (passageWays.containsKey(name)) {
                return new byte[0];
            }
            passageWays.put(name,passageway);
        }
        return crypto;
    }

    public byte[] create02(String name,byte[] crypto) throws Exception {

        if (!passageWays.containsKey(name)) {
            return new byte[0];
        }
        synchronized (name) {
            if (!passageWays.containsKey(name)) {
                return new byte[0];
            }
            Passageway passageway = passageWays.get(name);

            Set<Friend> friends = Basic.getIndexTest().getNode("names",name);
            if (friends == null || friends.size() < 1) {
                return new byte[0];
            }

            Friend friend = friends.iterator().next();

            byte[] real = RSA.decrypt(crypto,friend.getKey());
            int i1 = Integer.parseInt(new String(real));
            Integer i2 = passageway.siz;

            if (i2 != Integer.MAX_VALUE && i1 - i2 == 1) {

                real = AES.getKey();
                crypto = RSA.encrypt(real,friend.getKey());
                passageway.aes = new AES(real);
                passageway.siz = Integer.MAX_VALUE;

                return crypto;
            } else {
                return new byte[0];
            }
        }
    }

    /**
     * 创建单人通道
     * @param name 分组人员
     * @return 返回成功与否
     * @throws Exception
     */
    public static boolean createGroup(String name) throws Exception {
        Random random = new Random();

        if (passageWays.containsKey(name)) {
            if (passageWays.get(name).siz == Integer.MAX_VALUE) {
                return true;
            } else {
                Thread.sleep(random.nextLong() * 1000);
            }

        }

        synchronized (name) {
            if (passageWays.containsKey(name)) {
                if (passageWays.get(name).siz == Integer.MAX_VALUE) {
                    return true;
                }
            }

            GroupServer groupServer = Remote.getRemoteProxyObj(GroupServer.class);

            // 所以会产生负数?
            int i1 = random.nextInt(Integer.MAX_VALUE) - 1;

            // 用自身的秘钥发送随机数,证明自身
            byte[] crypto = RSA.encrypt(String.valueOf(i1).getBytes(),Basic.getKeyPair().getPrivate());
            crypto = groupServer.createGroup01(name,crypto);

            // 这里长度为0代表对方不信任本客户端
            if (crypto.length == 0) {
                return false;
            }

            // 获取对方的信息,拿到对应的公钥,验证对方
            Set<Friend> friends = Basic.getIndexTest().getNode("names",name);
            if (friends == null || friends.size() < 1) {
                return false;
            }
            Friend friend = friends.iterator().next();
            byte[] real = RSA.decrypt(crypto,friend.getKey());

            // 验证的方法是对发送过去的数字加1
            String[] tar = new String(real).split(",");
            if (Integer.parseInt(tar[0]) - i1 == 1) {

                i1 = Integer.parseInt(tar[1]) + 1;

                // 再次发送,用对方发送过来的数字+1发送
                crypto = RSA.encrypt(String.valueOf(i1).getBytes(),Basic.getKeyPair().getPrivate());

                // 获取对称秘钥
                crypto = groupServer.createGroup02(name,crypto);

                real = RSA.decrypt(crypto,Basic.getKeyPair().getPrivate());
                if (real.length != 0) {
                    Passageway passageway = new Passageway();
                    passageway.aes = new AES(real);
                    passageWays.put(name,passageway);
                    return true;
                }
            }

            return false;
        }
    }

    public boolean receiveMessage(String name,byte[] crypto) throws Exception {
        Passageway passageway = passageWays.get(name);
        if (passageway == null || passageway.aes == null) {
            return false;
        } else {
            byte[] real = passageway.aes.decode(crypto);
            System.out.println(name + ":" + new String(real));
            return true;
        }
    }

    public static boolean sendMessage(String name,String message) throws Exception {

        GroupServer groupServer = Remote.getRemoteProxyObj(GroupServer.class);

        if (createGroup(name)) {

            byte[] crypto = passageWays.get(name).aes.encode(message.getBytes());
            return groupServer.sendMessage(name, crypto);
        } else {
            return false;
        }
    }

}
