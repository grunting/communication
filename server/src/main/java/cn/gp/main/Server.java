package cn.gp.main;

import cn.gp.model.Basic;

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
        System.out.println("ServerNetty start");
        System.out.println("ServerNetty name is " + Basic.getName());

        NettyServer.run();

    }
}