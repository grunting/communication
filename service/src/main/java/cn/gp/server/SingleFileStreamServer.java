package cn.gp.server;

/**
 * 文件传输服务端协议
 */
public interface SingleFileStreamServer {

    /**
     * 服务端处理发送的文件流
     * @param target 对方的名字
     * @param fileName 文件名
     * @param bytes 数据流
     * @param index 当前数据包序号
     */
    boolean send(String target, String fileName, byte[] bytes,int index);

    /**
     * 创建文件夹
     * @param target 对方的名字
     * @param path 文件夹相对路径
     */
    boolean createDir(String target,String path);
}
