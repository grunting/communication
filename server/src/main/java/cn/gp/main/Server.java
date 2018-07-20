package cn.gp.main;

import cn.gp.crypto.JksTool;

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
        System.out.println("Server name is " + JksTool.getAlias());

        NettyServer.run();

    }
}