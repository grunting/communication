package cn.gp.service.impl;

import cn.gp.handler.Remote;
import cn.gp.model.Basic;
import cn.gp.model.ClientBean;
import cn.gp.service.FileStream;
import cn.gp.service.FileStreamServer;
import cn.gp.service.Group;
import io.netty.channel.Channel;

import java.util.Set;

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
     * 发送文件
     * @param groupId 分组id
     * @param fileName 文件名
     * @param bytes 数据流
     * @param index 当前数据包序号
     * @return 返回发送结果
     */
    public boolean send(String groupId, String fileName, byte[] bytes, int index) {

        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();
        Set<ClientBean> set = Basic.getIndex().getNode(groupId);

        int count = 0;
        for (ClientBean clientBean : set) {
            if (!clientBean.equals(clientBeanSelf)) {
                FileStream fileStream = Remote.getRemoteProxyObj(FileStream.class,clientBean.getChannel());
                if (fileStream.recoveFile(groupId,clientBeanSelf.getName(),fileName,index,bytes)) {
                    count ++;
                }
            }
        }

        return count == set.size() - 1;
    }

    /**
     * 创建文件夹
     * @param groupId 分组id
     * @param path 相对路径
     * @return
     */
    public boolean createDir(String groupId,String path) {

        ClientBean clientBeanSelf = Basic.getIndex().getNode("channelid",channel.id().asLongText()).iterator().next();
        Set<ClientBean> set = Basic.getIndex().getNode(groupId);

        int count = 0;
        for (ClientBean clientBean : set) {
            if (!clientBean.equals(clientBeanSelf)) {
                FileStream fileStream = Remote.getRemoteProxyObj(FileStream.class,clientBean.getChannel());
                if (fileStream.createDir(path)) {
                    count ++;
                }
            }
        }

        return count == set.size() - 1;
    }
}
