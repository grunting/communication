package cn.gp.main;

import cn.gp.model.Basic;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

/**
 * 客户端
 */
public class Client {

    /**
     * 暂时不接受任何参数
     * @param args 参数
     */
    public static void main(String[] args) {

        System.out.println("Client start");

        System.out.println("My name is " + Configure.getConfigString(Constant.CLIENT_NAME));

        Basic.setName(Configure.getConfigString(Constant.CLIENT_NAME));
        Basic.setJksPath(Configure.getConfigString(Constant.CLIENT_JKS_PATH));
        Basic.setPasswd(Configure.getConfigString(Constant.CLIENT_JKS_PASS));

//        记录服务器密码
//        Basic.setAes(new AES(Basic.getServerKey()));

        NettyClient.run();
    }
}
