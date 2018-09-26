package cn.gp.service.impl;

import cn.gp.client.SingleFileStream;
import cn.gp.crypto.AES;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.server.SingleFileStreamServer;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 传输文件
 */
public class SingleFileStreamImpl implements SingleFileStream {

    // 存放临时文件的地址
    private static final String targetDir = Configure.getConfigString(Constant.CLIENT_TARGET_DIR);

    // 记录整个节点的数据传输句柄
    private static Map<String,FileInfo> files = new HashMap<String, FileInfo>();

    /**
     * 内部类,为了实现数据传输句柄相对数据
     */
    private static class FileInfo {
        // 写入文件的入口
        FileOutputStream fileOutputStream;

        // 当前序号
        int index;
    }

    /**
     * 创建文件夹
     * @param path 相对路径
     * @return 返回创建成功与否
     */
    public boolean createDir(String path) {
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

    public boolean recoveFile(String target, String sparker, String fileName, int index, byte[] data) {
        return writeFile(target,sparker,fileName,index,data);
    }

    public void sendFile(String target, String path, String atom) throws Exception {

        int i = 5;
        while (!SingleGroupImpl.createGroup(target)) {
            if (i < 1) {
                break;
            }
            i --;
        }
        if (i == 0) {
            return;
        }

        SingleFileStreamServer fileStreamServer = Remote.getRemoteProxyObj(SingleFileStreamServer.class);
        File file = new File(path);

        // 递归发送文件
        if(file.isDirectory()) {
            String[] fileNames = file.list();

            // 远程创建文件夹
            boolean b = fileStreamServer.createDir(target,file.getAbsolutePath().replace(atom + File.separator,""));
            if (b) {
                for(String fileName : fileNames) {
                    sendFile(target,file.getAbsolutePath() + File.separator + fileName,atom);
                }
            }
        } else if(file.isFile()) {

            // 真实传递文件(分批传送)
            try {
                FileInputStream fileInputStream = new FileInputStream(file);

                // 分批序号与数据本身
                int index = 0;
                int len = 0;
                byte[] data = new byte[Configure.getConfigInteger(Constant.CLIENT_TARGET_BATCH)];

                // 2k发送一次
                while(true) {

                    int realLen = fileInputStream.read(data);

                    if (realLen == -1) {
                        break;
                    }

                    if (realLen < Configure.getConfigInteger(Constant.CLIENT_TARGET_BATCH)) {
                        data = Arrays.copyOf(data,realLen);
                    }

                    // 加密
                    AES aes = Basic.getSendMessage().get(target);
                    byte[] crypto = aes.encode(data);

                    // 发送不成功则重复发,重试5次,超过则认为整体发送失败
                    int retry = 0;
                    while(!fileStreamServer.send(target,file.getAbsolutePath().replace(atom + File.separator,""),crypto,index)) {
                        if (retry++ > 5) {
                            index = -1;
                            break;
                        }
                    }
                    len += data.length;

                    System.out.println("target:" + file.getName() + " 传输进度:" + len + "/" + file.length());

                    if (index == -1) {
                        break;
                    }
                    index ++;
                }

                // 分批序号-1代表正常完成
                fileInputStream.close();
                fileStreamServer.send(target,file.getName(),data,-1);

                // 告诉自己发送结果
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
     * 全局的写文件函数
     * @param groupId 分组id
     * @param sparker 发送者
     * @param fileName 文件名
     * @param index 当前发送第几块
     * @param data 数据
     * @return 返回成功与否
     */
    public synchronized static boolean writeFile(String groupId, String sparker,String fileName, int index, byte[] data) {

        // 生成id
        String id = groupId + ":" + sparker + ":sendFile:" + fileName;

        // 如果文件句柄存在
        if (files.containsKey(id)) {
            try {

                // 传输完成,忽略数据本身(可以改成最后一次传递-1,减少调用次数)
                if (index == -1) {
                    FileInfo fileInfo = files.remove(id);
                    FileOutputStream fileOutputStream = fileInfo.fileOutputStream;
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } else {

                    // 获取文件句柄相对数据
                    FileInfo fileInfo = files.get(id);

                    // 支持重发,但是不支持乱序发
                    if (fileInfo.index + 1 == index) {
                        FileOutputStream fileOutputStream = fileInfo.fileOutputStream;

                        AES aes = Basic.getSendMessage().get(sparker);
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

        // 文件句柄不存在
        } else {
            try {

                // 这种时候只接受第一次发送
                if (index == 0) {

                    FileOutputStream fileOutputStream = new FileOutputStream(targetDir + File.separator + fileName);

                    AES aes = Basic.getSendMessage().get(sparker);
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


    public static void send(String target, String path) throws Exception {

        SingleFileStreamImpl singleFileStream = new SingleFileStreamImpl();
        singleFileStream.sendFile(target,path,path);
    }
}
