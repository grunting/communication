package cn.gp.handler;

import cn.gp.service.FileStreamImpl;
import cn.gp.service.ReportImpl;
import cn.gp.service.SendMessageImpl;
import cn.gp.service.impl.Report;
import cn.gp.service.impl.SendMessage;

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

        System.out.print("input target dir:");

        Scanner scanner = new Scanner(System.in);
        String tar = scanner.nextLine();
        FileStreamImpl.target = tar;

        while(true) {

            System.out.print("order:");

            String orderOrMessage = scanner.nextLine();

            if(orderOrMessage.startsWith("stop")) {
                break;
            }

            String[] split = orderOrMessage.split(":");

            FileStreamImpl fileStream = new FileStreamImpl();
            fileStream.sendFile(split[0],split[1]);
        }
        System.exit(0);
    }
}
