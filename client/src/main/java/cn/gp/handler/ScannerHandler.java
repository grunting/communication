package cn.gp.handler;

import cn.gp.model.Basic;
import cn.gp.model.Friend;
import cn.gp.service.impl.ReportImpl;
import cn.gp.service.impl.SingleGroupImpl;

import java.util.Scanner;
import java.util.Set;

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
                try {
                    send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    /**
     * 执行实体
     */
    public static void send() throws Exception{

        ReportImpl.send();

        while(true) {

            System.out.print("order:");

            Scanner scanner = new Scanner(System.in);
            String orderOrMessage = scanner.nextLine();

            if(orderOrMessage.startsWith("stop")) {
                break;
            }

            String[] split = orderOrMessage.split(":");

            if (split.length == 1) {
                Set<Friend> set = Basic.getIndexTest().getNode("names");
                for (Friend friend : set) {
                    System.out.println(friend.getName());
                }
            }

            if(split.length != 2) {
                continue;
            }

            FileStream fileStream = new FileStreamImpl();

            if (split[0].equals("sendFiles")){
                String[] ss = split[1].split(",");
                fileStream.sendFile(ss[0],ss[1],ss[1]);
            } else {
                System.out.println("发送结果:" + SingleGroupImpl.sendMessage(split[0],split[1]));
            }
        }
        System.exit(0);
    }
}
