package cn.gp.client;

/**
 * 文件传输协议
 */
public interface SingleFileStream {

    /**
     * 发送文件
     * @param target 对方的名字
     * @param path 地址
     * @param atom 根地址
     */
    void sendFile(String target, String path,String atom) throws Exception;

    /**
     * 接收文件
     * @param target 对方的名字
     * @param sparker 发送者
     * @param fileName 文件名
     * @param index 当前发送第几块
     * @param data 数据
     */
    boolean recoveFile(String target, String sparker, String fileName, int index, byte[] data);

    /**
     * 创建文件夹
     * @param path 相对路径
     * @return 返回执行结果
     */
    boolean createDir(String path);
}
