package cn.gp.main;

import cn.gp.model.Basic;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

/**
 * 服务器
 */
public class Server {

    /**
     * 暂时不接受任何参数
     * @param args 参数
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        // 启动准备
        System.out.println("Server start");

        Basic.setJksPath(Configure.getConfigString(Constant.SERVER_JKS_PATH));
        Basic.setPasswd(Configure.getConfigString(Constant.SERVER_JKS_PASS));

        NettyServer.run();

    }
}