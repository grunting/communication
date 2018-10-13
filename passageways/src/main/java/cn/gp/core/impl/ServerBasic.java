package cn.gp.core.impl;

import cn.gp.channel.ServerChannel;
import cn.gp.service.ChannelHook;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import cn.gp.util.JksTool;

/**
 * 服务器通道模块
 */
public class ServerBasic extends SimpleBasic {

    public ServerBasic() {
        super();
        this.configure.setProperties(Configure.getInstance("passageways.properties","server.conf"));
        this.jksTool = JksTool.getInstance(
                configure.getConfigString(Constant.SERVER_JKS_PATH),
                configure.getConfigString(Constant.SERVER_JKS_KEYPASS),
                configure.getConfigString(Constant.SERVER_JKS_KEYPASS)
        );
        this.simpleChannel = new ServerChannel(this);
    }

    /**
     * 设置通道钩子
     * @param channelHook 钩子接口
     */
    public void setChannelHook(ChannelHook channelHook) {

        this.channelHook = channelHook;
    }
}
