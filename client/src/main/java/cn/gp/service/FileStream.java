package cn.gp.service;

/**
 * 文件传输协议
 */
public interface FileStream {

    /**
     * 发送文件
     * @param groupId 分组id
     * @param path 地址
     * @param atom 根地址
     */
    void sendFile(String groupId, String path,String atom);

    /**
     * 接收文件
     * @param groupId 分组id
     * @param sparker 发送者
     * @param fileName 文件名
     * @param index 当前发送第几块
     * @param data 数据
     */
    boolean recoveFile(String groupId, String sparker, String fileName, int index, byte[] data);

    /**
     * 创建文件夹
     * @param path 相对路径
     * @return 返回执行结果
     */
    boolean createDir(String path);
}
