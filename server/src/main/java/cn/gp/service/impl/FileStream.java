package cn.gp.service.impl;

/**
 * 文件传输协议
 */
public interface FileStream {

    /**
     * 发送文件
     * @param name 接收人
     * @param path 文件地址
     */
    void sendFile(String name, String path);

    /**
     * 接收文件
     * @param name 文件名
     * @param data 数据流
     */
    void recoveFile(String name, byte[] data);
}
