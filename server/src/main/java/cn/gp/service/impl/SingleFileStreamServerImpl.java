package cn.gp.service.impl;

import cn.gp.client.SingleFileStream;
import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.server.SingleFileStreamServer;
import io.netty.channel.Channel;

/**
 * 简单的一对一文件传输
 */
public class SingleFileStreamServerImpl implements SingleFileStreamServer {

    // 先拿着发起者的通道
    private Channel channel;

    public SingleFileStreamServerImpl(Channel channel){
        this.channel = channel;
    }

    /**
     * 传送数据
     * @param target 对方的名字
     * @param fileName 文件名
     * @param bytes 数据流
     * @param index 当前数据包序号
     * @return 返回成功与否
     */
    public boolean send(String target, String fileName, byte[] bytes, int index) {

        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();
        ClientBean clientBeanTarget = Basic.getIndex().getNode("names",target).iterator().next();

        SingleFileStream fileStream = Remote.getRemoteProxyObj(SingleFileStream.class,clientBeanTarget.getChannel());
        return fileStream.recoveFile(target,clientBeanSelf.getName(),fileName,index,bytes);
    }

    /**
     * 创建文件夹
     * @param target 分组id
     * @param path 文件夹相对路径
     * @return 返回创建成功与否
     */
    public boolean createDir(String target, String path) {

        ClientBean clientBeanTarget = Basic.getIndex().getNode("names",target).iterator().next();

        SingleFileStream fileStream = Remote.getRemoteProxyObj(SingleFileStream.class,clientBeanTarget.getChannel());
        return fileStream.createDir(path);
    }
}
