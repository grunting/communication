package cn.gp.main;

import cn.gp.crypto.AES;
import cn.gp.model.Basic;

import java.util.Scanner;

/**
 * 客户端
 */
public class Client {

    /**
     * 暂时不接受任何参数
     * @param args 参数
     */
    public static void main(String[] args) {

        System.out.println("client");

        Scanner scanner = new Scanner(System.in);

        System.out.print("input your name:");
        String name = scanner.nextLine();
        Basic.setName(name);

        System.out.print("input server key:");
        String key = scanner.nextLine();
        Basic.setServerKey(key);

        // 记录服务器密码
        Basic.setAes(new AES(Basic.getServerKey()));

        NettyClient.run();

    }
}
