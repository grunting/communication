package cn.gp.client;

/**
 * 分组接口
 */
public interface Group {

    /**
     * 需要对第一步发送过来的字符串用自身的私钥加密,证明自己
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回random + 1,random2
     */
    byte[] create01(String name, byte[] crypto) throws Exception;

    /**
     * 需要对第二步发送过来的字符串用自身的私钥加密,证明自己
     * @param name 发起连接的人
     * @param crypto 发送过来的随机数(证明对方的身份)
     * @return 返回对称秘钥
     */
    byte[] create02(String name, byte[] crypto) throws Exception;

    /**
     * 发送信息
     * @param name 发送方名称
     * @param crypto 加密信息
     * @return 返回接收成功
     * @throws Exception
     */
    boolean receiveMessage(String name,byte[] crypto) throws Exception;

	/**
     * 发送信息
     * @param name 对方的名字
     * @param message 信息
     * @return 成功与否
     * @throws Exception
     */
    boolean sendMessage(String name,String message) throws Exception;

    String getMessage(String nameAndTime) throws Exception;
}
