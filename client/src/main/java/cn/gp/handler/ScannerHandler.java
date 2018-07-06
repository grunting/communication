package cn.gp.handler;

import cn.gp.service.impl.ReportImpl;
import cn.gp.service.impl.SendMessageImpl;
import cn.gp.service.SendMessage;

import java.util.Scanner;

/**
 * 简单的命令行响应部分
 */
public class ScannerHandler {

    /**
     * 开启线程去截获命令行输入
     */
    public static void run() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                send();
            }
        };
        thread.start();
    }

    /**
     * 执行实体
     */
    public static void send() {

        ReportImpl.send();

        while(true) {

            System.out.print("order:");

            Scanner scanner = new Scanner(System.in);
            String orderOrMessage = scanner.nextLine();

            if(orderOrMessage.startsWith("stop")) {
                break;
            }

            String[] split = orderOrMessage.split(":");

            SendMessage sendMessage = new SendMessageImpl();
            sendMessage.sendMessage(split[0],split[1]);
        }
        System.exit(0);
    }
}
