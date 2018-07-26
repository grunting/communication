package cn.gp.service.impl;

import cn.gp.crypto.AES;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.service.FileStream;
import cn.gp.service.FileStreamServer;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 简易文件传输协议实现
 */
public class FileStreamImpl implements FileStream {

    // 存放临时文件的地方
    private static final String targetDir = Configure.getConfigString(Constant.CLIENT_TARGET_DIR);

    /**
     * 发送文件
     * @param groupId 分组id
     * @param path 地址
     */
    public void sendFile(String groupId, String path,String atom) {

        FileStreamServer fileStreamServer = Remote.getRemoteProxyObj(FileStreamServer.class);

        File file = new File(path);
        if(file.isDirectory()) {
            String[] fileNames = file.list();
            boolean b = fileStreamServer.createDir(groupId,file.getAbsolutePath().replace(atom + File.separator,""));
            if (b) {
                for(String fileName : fileNames) {
                    sendFile(groupId,file.getAbsolutePath() + File.separator + fileName,atom);
                }
            }
        } else if(file.isFile()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);

                int index = 0;
                byte[] data = new byte[1024 * 2];

                // 2k发送一次
                while(fileInputStream.read(data) != -1) {

                    AES aes = new AES(Basic.getGroups().get(groupId));
                    byte[] crypto = aes.encode(data);

                    // 发送不成功则重复发,重试5次,超过则认为整体发送失败
                    int retry = 0;

                    while(!fileStreamServer.send(groupId,file.getAbsolutePath().replace(atom + File.separator,""),crypto,index)) {
                        if (retry++ > 5) {
                            index = -1;
                            break;
                        }
                    }
                    if (index == -1) {
                        break;
                    }
                    index ++;
                }

                fileInputStream.close();
                fileStreamServer.send(groupId,file.getName(),data,-1);

                // 告诉另一端发送结果
                if (index != -1) {
                    System.out.println("finshed:" + file.getName());
                } else {
                    System.out.println("bad:" + file.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建文件夹
     * @param path 相对路径
     * @return 返回创建结果
     */
    public boolean createDir(String path) {
        System.out.println("createPath:" + path);
        try {
            File file = new File(targetDir + File.separator + path);
            int retry = 0;
            while(!file.mkdir()) {
                if (retry++ > 5) {
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 接收文件
     * @param groupId 分组id
     * @param sparker 发送者
     * @param index 当前发送第几块
     * @param data 数据
     * @return 返回接收成功与否
     */
    public boolean recoveFile(String groupId, String sparker,String fileName, int index, byte[] data) {
        return writeFile(groupId,sparker,fileName,index,data);
    }

    private static Map<String,FileInfo> files = new HashMap<String, FileInfo>();

    private static class FileInfo {
        FileOutputStream fileOutputStream;
        int index;
    }

    /**
     * 全局的写文件函数
     * @param groupId 分组id
     * @param sparker 发送者
     * @param fileName 文件名
     * @param index 当前发送第几块
     * @param data 数据
     * @return 返回成功与否
     */
    public synchronized static boolean writeFile(String groupId, String sparker,String fileName, int index, byte[] data) {

        String id = groupId + ":" + sparker + ":sendFile:" + fileName;

        if (files.containsKey(id)) {
            try {
                if (index == -1) {
                    FileInfo fileInfo = files.remove(id);
                    FileOutputStream fileOutputStream = fileInfo.fileOutputStream;
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } else {

                    FileInfo fileInfo = files.get(id);

                    // 支持重发,但是不支持乱序发
                    if (fileInfo.index + 1 == index) {
                        FileOutputStream fileOutputStream = fileInfo.fileOutputStream;

                        AES aes = new AES(Basic.getGroups().get(groupId));
                        byte[] real = aes.decode(data);

                        fileOutputStream.write(real);
                        fileOutputStream.flush();
                        fileInfo.index = index;
                    }
                }
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                files.remove(id);
                return false;
            }
        } else {
            try {
                if (index == 0) {

                    FileOutputStream fileOutputStream = new FileOutputStream(targetDir + File.separator + fileName);

                    AES aes = new AES(Basic.getGroups().get(groupId));
                    byte[] real = aes.decode(data);

                    fileOutputStream.write(real);
                    fileOutputStream.flush();

                    FileInfo fileInfo = new FileInfo();
                    fileInfo.fileOutputStream = fileOutputStream;
                    fileInfo.index = index;

                    files.put(id,fileInfo);

                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
