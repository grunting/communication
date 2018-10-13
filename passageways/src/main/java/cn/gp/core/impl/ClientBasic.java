package cn.gp.core.impl;

import cn.gp.channel.ClientChannel;
import cn.gp.handler.Remote;
import cn.gp.service.CheckReadyHook;
import cn.gp.util.Configure;
import cn.gp.util.Constant;
import cn.gp.util.JksTool;

/**
 * 客户端
 */
public class ClientBasic extends SimpleBasic {

    public ClientBasic() {
        super();
        this.configure.setProperties(Configure.getInstance("passageways.properties","client.conf"));
        this.jksTool = JksTool.getInstance(
                configure.getConfigString(Constant.CLIENT_JKS_PATH),
                configure.getConfigString(Constant.CLIENT_JKS_KEYPASS),
                configure.getConfigString(Constant.CLIENT_JKS_KEYPASS)
        );
        this.simpleChannel = new ClientChannel(this);
    }

    /**
     * 设置连接服务器后的钩子操作
     * @param checkReadyHook 钩子函数
     */
    public void setCheckReadyHook(CheckReadyHook checkReadyHook) {

        this.checkReadyHook = checkReadyHook;
        if (getRemote() != null) {
            getRemote().close();
            this.remote = new Remote(this,checkReadyHook);
        }
    }

}
