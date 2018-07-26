package cn.gp.main;

import cn.gp.model.Basic;

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
        System.out.println("My name is " + Basic.getName());

        NettyClient.run();
    }
}
