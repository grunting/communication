package cn.gp.server;

/**
 * 分组接口
 */
public interface GroupServer {


    /**
     * 创建分组第一步
     * @param name 对方的名称
     * @param crypto 加密内容
     * @return 返回对方的响应信息
     */
    byte[] createGroup01(String name, byte[] crypto) throws Exception;

    /**
     * 创建分组第二步
     * @param name 对方的名称
     * @param crypto 加密内容
     * @return 返回加密的对称秘钥
     */
    byte[] createGroup02(String name, byte[] crypto) throws Exception;

    /**
     * 发送信息
     * @param name 发送目标
     * @param message 加密信息
     * @return 返回成功与否
     * @throws Exception
     */
    boolean sendMessage(String name,byte[] message) throws Exception;

}
