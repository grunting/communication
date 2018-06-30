package cn.gp.main;

import cn.gp.crypto.AES;
import cn.gp.model.Basic;

import java.util.Scanner;

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
        Scanner scanner = new Scanner(System.in);
        System.out.print("enter serverKey:");
        String key = scanner.nextLine();
        Basic.setKey(key);
        Basic.setAes(new AES(key));

        NettyServer.run();

    }
}
