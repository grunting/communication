package cn.gp.service;

import com.google.protobuf.ByteString;

/**
 * 注册本客户端服务端协议
 */
public interface RegisterServer {

    /**
     * 处理客户端上报
     * @param name 名称
     * @param byteString 公钥
     * @return 返回上报结果(名称可能冲突)
     */
    boolean addClient(String name, ByteString byteString);
}
