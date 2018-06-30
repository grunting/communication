package cn.gp.service;



import cn.gp.handler.Remote;
import cn.gp.service.impl.FileStream;
import cn.gp.service.impl.FileStreamServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 简易文件传输协议实现
 */
public class FileStreamImpl implements FileStream {

    // 输出目录
    public static String target = "";

    /**
     * 发送文件
     * @param name 接收人
     * @param path 文件地址
     */
    public void sendFile(String name,String path) {
        File file = new File(path);

        if(file.isDirectory()) {
            String[] fileNames = file.list();
            for(String fileName : fileNames) {
                sendFile(name,file.getAbsolutePath() + File.separator + fileName);
            }
        } else if(file.isFile()) {

            try {

                FileInputStream fileInputStream = new FileInputStream(file);

                byte[] data = new byte[1024 * 100];

                // 2k发送一次
                FileStreamServer fileStreamServer = Remote.getRemoteProxyObj(FileStreamServer.class);
                while(fileInputStream.read(data) != -1) {
                    fileStreamServer.send(name,file.getName(),data);
                }

                fileInputStream.close();

            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将得到的数据流按文件名追加到文件中
     * @param name 文件名
     * @param data 数据流
     */
    public void recoveFile(String name,byte[] data) {
        System.out.println(name);
        try {
            FileOutputStream fos= new FileOutputStream(target + File.separator + name,true);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
