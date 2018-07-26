package cn.gp.service;

/**
 * 文件传输服务端协议
 */
public interface FileStreamServer {

    /**
     * 服务端处理发送的文件流
     * @param groupId 分组id
     * @param fileName 文件名
     * @param bytes 数据流
     * @param index 当前数据包序号
     */
    boolean send(String groupId, String fileName, byte[] bytes,int index);

    /**
     * 创建文件夹
     * @param groupId 分组id
     * @param path 文件夹相对路径
     */
    boolean createDir(String groupId,String path);
}
