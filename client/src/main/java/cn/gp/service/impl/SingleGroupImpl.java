package cn.gp.service.impl;

import cn.gp.client.SingleGroup;
import cn.gp.crypto.AES;
import cn.gp.crypto.RSA;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.server.SingleGroupServer;

import java.util.Random;

/**
 * 单人分组客户端实现
 */
public class SingleGroupImpl implements SingleGroup {

    /**
     * 第一步,检查对方的身份,同时发送一个验证信息出去
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return
     */
    public byte[] create01(String name, byte[] crypto) throws Exception {

        // 获取发起人的信息
        Friend friend = Basic.getIndexTest().getNode("names",name).iterator().next();

        // 解密,如果出错就出错
        byte[] real = RSA.decrypt(crypto,friend.getKey());

        int i1 = Integer.parseInt(new String(real)) + 1;
        Random random = new Random();
        int i2 = random.nextInt(Integer.MAX_VALUE) - 1;
        String tar = String.valueOf(i1) + "," + String.valueOf(i2);

        // 使用自身私钥加密,证明自己
        byte[] crypto2 = RSA.encrypt(tar.getBytes(),Basic.getKeyPair().getPrivate());

        // 暂存第一阶段信息
        Basic.getSendMessageBefore().put(name,i2);

        return crypto2;
    }

    /**
     * 第二步,检查对方身份,成功则给出对称秘钥,不成功则给出空秘钥
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回加密的对称秘钥
     * @throws Exception
     */
    public byte[] create02(String name, byte[] crypto) throws Exception {

        // 获取发起人的信息
        Friend friend = Basic.getIndexTest().getNode("names",name).iterator().next();

        // 解密发送方的信息
        byte[] real = RSA.decrypt(crypto,friend.getKey());
        int i1 = Integer.parseInt(new String(real));
        Integer i2 = Basic.getSendMessageBefore().remove(name);

        // 验证
        if (i2 != null && i1 - i2 == 1) {

            // 生成对称秘钥发送回去并进行记录
            real = AES.getKey();
            crypto = RSA.encrypt(real,friend.getKey());
            Basic.getSendMessage().put(name,new AES(real));

            return crypto;
        } else {
            return new byte[0];
        }
    }

    /**
     * 创建单人的分组
     * @param name 创建分组的对方
     * @return 返回创建成功与否
     */
    public static boolean createGroup (String name) throws Exception {

        if (Basic.getSendMessage().containsKey(name)) {
            return true;
        }

        SingleGroupServer singleGroupServer = Remote.getRemoteProxyObj(SingleGroupServer.class);

        Random random = new Random();
        // 小概率会得到极大值
        int i1 = random.nextInt(Integer.MAX_VALUE) - 1;


        // 用自身的秘钥发送随机数,证明自身
        byte[] crypto = RSA.encrypt(String.valueOf(i1).getBytes(), Basic.getKeyPair().getPrivate());
        crypto = singleGroupServer.createGroup01(name,crypto);

        // 获取对方的信息,拿到对应的公钥,验证对方
        Friend friend = Basic.getIndexTest().getNode("names",name).iterator().next();
        byte[] real = RSA.decrypt(crypto,friend.getKey());

        // 验证的方法是对发送过去的数字加1
        String[] tar = new String(real).split(",");
        if (Integer.parseInt(tar[0]) - i1 == 1) {

            i1 = Integer.parseInt(tar[1]) + 1;

            // 再次发送,用对方发送过来的数字+1发送
            crypto = RSA.encrypt(String.valueOf(i1).getBytes(),Basic.getKeyPair().getPrivate());

            // 获取对称秘钥
            crypto = singleGroupServer.createGroup02(name,crypto);

            real = RSA.decrypt(crypto,Basic.getKeyPair().getPrivate());
            if (real.length != 0) {
                Basic.getSendMessage().put(name,new AES(real));
                return true;
            }
        }
        return false;
    }

    /**
     * 接收信息
     * @param name 发送方名称
     * @param crypto 加密信息
     * @return 返回接收成功与否
     * @throws Exception
     */
    public boolean receiveMessage(String name, byte[] crypto) throws Exception {
        byte[] real = Basic.getSendMessage().get(name).decode(crypto);
        System.out.println(name + ":" + new String(real));
        return true;
    }

    /**
     * 发送信息
     * @param name 对方的名字
     * @param message 信息本身
     * @return 返回发送成功与否
     * @throws Exception
     */
    public static boolean sendMessage (String name,String message) throws Exception {

        int i = 5;
        while (!createGroup(name)) {
            if (i < 1) {
                break;
            }
            i --;
        }
        if (i == 0) {
            return false;
        }

        SingleGroupServer singleGroupServer = Remote.getRemoteProxyObj(SingleGroupServer.class);

        byte[] crypto = Basic.getSendMessage().get(name).encode(message.getBytes());
        return singleGroupServer.sendMessage(name,crypto);
    }
}
