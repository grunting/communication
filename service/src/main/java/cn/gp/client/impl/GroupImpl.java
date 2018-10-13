package cn.gp.client.impl;

import cn.gp.client.Group;
import cn.gp.core.impl.SimpleBasic;
import cn.gp.crypto.AES;
import cn.gp.crypto.RSA;
import cn.gp.model.Friend;
import cn.gp.server.GroupServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多人分组接口实现
 */
public class GroupImpl implements Group {

    private static final Logger logger = LoggerFactory.getLogger(GroupImpl.class);

    // 存放所有通道
    protected static final ConcurrentMap<String,Passageway> passageWays = new ConcurrentHashMap<String, Passageway>();

    private SimpleBasic simpleBasic;

    public GroupImpl(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
    }

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
     * 舍弃所有通道
     */
    public void lostClientAll() {
        passageWays.clear();
    }

    /**
     * 第一步,检查对方的身份,同时发送一个验证信息出去
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回响应部分
     */
    public byte[] create01(String name,byte[] crypto) throws Exception {

        logger.debug("create01stage1 name:{},friends:{}",name,ReportImpl.getIndex().getAllNode().toString());

        Set<Friend> friends = ReportImpl.getIndex().getNode("names",name);
        if (friends == null || friends.size() < 1) {

            logger.debug("create01stage2 friends is null or empty");
            return new byte[0];
        }

        Friend friend = friends.iterator().next();

        logger.debug("create01stage3 friend:{}",friend.toString());

        byte[] real = RSA.decrypt(crypto,friend.getKey());

        logger.debug("create01stage4 syc:{}",new String(real));

        int i1 = Integer.parseInt(new String(real)) + 1;
        Random random = new Random();
        int i2 = random.nextInt(Integer.MAX_VALUE) - 1;
        String tar = String.valueOf(i1) + "," + String.valueOf(i2);

        crypto = RSA.encrypt(tar.getBytes(),simpleBasic.getKeyPair().getPrivate());
        Passageway passageway = new Passageway();
        passageway.siz = i2;

        if (passageWays.containsKey(name)) {

            logger.debug("create01stage5 passageWages have {}",name);
            return new byte[0];
        }
        synchronized (name) {
            if (passageWays.containsKey(name)) {

                logger.debug("create01stage6 passageWages have {}",name);
                return new byte[0];
            }
            passageWays.put(name,passageway);
        }

        logger.debug("create01stage7 create01success,{}",name);
        return crypto;
    }

    /**
     * 第二步
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回响应部分
     * @throws Exception
     */
    public byte[] create02(String name,byte[] crypto) throws Exception {

        logger.debug("create02stage1 name:{}",name);
        if (!passageWays.containsKey(name)) {

            logger.debug("create02stage2 passageWays not have {}",name);
            return new byte[0];
        }
        synchronized (name) {
            if (!passageWays.containsKey(name)) {

                logger.debug("create02stage3 passageWays not have {}",name);
                return new byte[0];
            }
            Passageway passageway = passageWays.get(name);

            Set<Friend> friends = ReportImpl.getIndex().getNode("names",name);
            if (friends == null || friends.size() < 1) {

                logger.debug("create02stage4 friends is null or empty");
                return new byte[0];
            }

            Friend friend = friends.iterator().next();

            logger.debug("create02stage5 friend:{}",friend.toString());

            byte[] real = RSA.decrypt(crypto,friend.getKey());
            int i1 = Integer.parseInt(new String(real));
            Integer i2 = passageway.siz;

            if (i2 != Integer.MAX_VALUE && i1 - i2 == 1) {

                real = AES.getKey();
                crypto = RSA.encrypt(real,friend.getKey());
                passageway.aes = new AES(real);
                passageway.siz = Integer.MAX_VALUE;

                logger.debug("create02stage6 success");
                return crypto;
            } else {

                logger.debug("create02stage6 fail i2:{},i1:{}",i2,i1);
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
    public boolean createGroup(String name) throws Exception {
        Random random = new Random();

        logger.debug("createGroup1 name:{}",name);
        if (passageWays.containsKey(name)) {
            if (passageWays.get(name).siz == Integer.MAX_VALUE) {

                logger.debug("createGroup2 success",name);
                return true;
            } else {
                Thread.sleep(random.nextLong() * 1000);
            }
        }

        synchronized (name) {
            if (passageWays.containsKey(name)) {
                if (passageWays.get(name).siz == Integer.MAX_VALUE) {

                    logger.debug("createGroup3 success",name);
                    return true;
                }
            }

            GroupServer groupServer = simpleBasic.getRemoteProxyObj(GroupServer.class);

            // 所以会产生负数?
            int i1 = random.nextInt(Integer.MAX_VALUE) - 1;

            // 用自身的秘钥发送随机数,证明自身
            byte[] crypto = RSA.encrypt(String.valueOf(i1).getBytes(),simpleBasic.getKeyPair().getPrivate());
            crypto = groupServer.createGroup01(name,crypto);

            // 这里长度为0代表对方不信任本客户端或指定联系人不存在
            if (crypto == null ||crypto.length == 0) {
                return false;
            }
            logger.debug("createGroup4 stage one is success,crypto's length:{}",name,crypto.length);

            // 获取对方的信息,拿到对应的公钥,验证对方
            Set<Friend> friends = ReportImpl.getIndex().getNode("names",name);
            if (friends == null || friends.size() < 1) {
                return false;
            }
            Friend friend = friends.iterator().next();
            byte[] real = RSA.decrypt(crypto,friend.getKey());

            // 验证的方法是对发送过去的数字加1
            String[] tar = new String(real).split(",");
            logger.debug("createGroup5 friend:{},i2:{},i1:{}",Arrays.asList(friend.toString(),tar[0],i1));
            if (Integer.parseInt(tar[0]) - i1 == 1) {

                i1 = Integer.parseInt(tar[1]) + 1;

                // 再次发送,用对方发送过来的数字+1发送
                crypto = RSA.encrypt(String.valueOf(i1).getBytes(),simpleBasic.getKeyPair().getPrivate());

                // 获取对称秘钥
                crypto = groupServer.createGroup02(name,crypto);

                real = RSA.decrypt(crypto,simpleBasic.getKeyPair().getPrivate());
                logger.debug("createGroup6 stage two is success,aesKey's length:{}",real.length);
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

    /**
     * 接收信息
     * @param name 发送方名称
     * @param crypto 加密信息
     * @return 接收成功与否
     * @throws Exception
     */
    public boolean receiveMessage(String name,byte[] crypto) throws Exception {

        Passageway passageway = passageWays.get(name);
        if (passageway == null || passageway.aes == null) {
            logger.debug("receiveMessage false name:{}",name);
            return false;
        } else {
            logger.debug("receiveMessage success name:{},passageWays:{}",name, passageWays.toString());
            byte[] real = passageway.aes.decode(crypto);
            System.out.println(name + ":" + new String(real));

            return true;
        }
    }

    /**
     * 发送信息
     * @param name 对方的名字
     * @param message 信息
     * @return 接收成功与否
     * @throws Exception
     */
    public boolean sendMessage(String name,String message) {

        GroupServer groupServer = simpleBasic.getRemoteProxyObj(GroupServer.class);
        try {
            if (createGroup(name)) {
                byte[] crypto = passageWays.get(name).aes.encode(message.getBytes());
                logger.debug("sendMessage name:{},passageWays:{}",name,passageWays.toString());
                return groupServer.sendMessage(name, crypto);
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
