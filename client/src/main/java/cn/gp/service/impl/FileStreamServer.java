package cn.gp.service.impl;

/**
 * 文件传输服务端协议
 */
public interface FileStreamServer {

    /**
     * 服务端处理发送的文件流
     * @param name 接收人
     * @param fileName 文件名
     * @param bytes 数据流
     */
    void send(String name, String fileName, byte[] bytes);
}
