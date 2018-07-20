package cn.gp.main;

import cn.gp.crypto.JksTool;
import cn.gp.model.Basic;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

/**
 * 客户端
 */
public class Client {

    /**
     * 外部参数控制:
     * -Dclient.config=/root/config/client.properties -配置文件
     *
     * 
     * @param args 参数
     */
    public static void main(String[] args) {

        System.out.println("Client start");
        System.out.println("My name is " + JksTool.getAlias());

        Basic.setName(JksTool.getAlias());
        Basic.setJksPath(Configure.getConfigString(Constant.CLIENT_JKS_PATH));
        Basic.setPasswd(Configure.getConfigString(Constant.CLIENT_JKS_KEYPASS));

        NettyClient.run();
    }
}
