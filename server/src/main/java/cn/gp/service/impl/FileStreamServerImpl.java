package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.FileStream;
import cn.gp.service.FileStreamServer;
import io.netty.channel.Channel;

/**
 * 文件传输服务端实现
 */
public class FileStreamServerImpl implements FileStreamServer {

    // 先拿着发起者的通道
    private Channel channel;

    public FileStreamServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 发送消息给指定得人
     * @param name 接收人
     * @param fileName 文件名
     * @param bytes 数据流
     */
    public void send(String name, String fileName, byte[] bytes) {
        ClientBean target = null;
        for(ClientBean clientBean : Basic.getChannelMap().values()) {
            if (clientBean.getName().equals(name)) {
                target = clientBean;
            }
        }

        FileStream fileStream = Remote.getRemoteProxyObj(FileStream.class,target.getChannel());
        fileStream.recoveFile(fileName,bytes);
    }
}
