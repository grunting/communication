package cn.gp.service;

import java.security.PublicKey;

/**
 * 注册本客户端服务端协议
 */
public interface RegisterServer {

    /**
     * 处理客户端上报
     * @param name 名称
     * @param publicKey 公钥
     * @param crypto 验证密文
     * @return 返回上报结果(名称可能冲突)
     */
    boolean addClient(String name, PublicKey publicKey, byte[] crypto) throws Exception;
}
