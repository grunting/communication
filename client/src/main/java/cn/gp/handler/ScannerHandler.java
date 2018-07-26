package cn.gp.handler;

import cn.gp.service.FileStream;
import cn.gp.service.Group;
import cn.gp.service.impl.FileStreamImpl;
import cn.gp.service.impl.GroupImpl;
import cn.gp.service.impl.ReportImpl;

import java.util.Arrays;
import java.util.List;
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

            Group group = new GroupImpl();
            if (split[0].equals("create")) {
                List<String> list = Arrays.asList(split[1].split(","));

                group.createGroup(list);
            } else if (split[0].equals("showFriends")){
                System.out.println(group.showGroupUsers(split[1]));
            } else if (split[0].equals("sendFiles")){
                FileStream fileStream = new FileStreamImpl();
                String[] ss = split[1].split(",");
                fileStream.sendFile(ss[0],ss[1],ss[1]);
            } else {
                System.out.println(group.sendMessage(split[0],split[1].getBytes()));
            }
        }
        System.exit(0);
    }
}
