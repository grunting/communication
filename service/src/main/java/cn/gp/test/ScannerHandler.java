package cn.gp.test;

import cn.gp.client.Group;
import cn.gp.core.Basic;

import java.util.Scanner;

/**
 * 简单的命令行响应部分
 */
public class ScannerHandler {

	private Basic basic;

	public ScannerHandler(Basic basic) {
		this.basic = basic;
	}

	/**
	 * 开启线程去截获命令行输入
	 */
	public void init() {
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
	public void send() throws Exception{

		while(true) {

			System.out.print("order:");

			Scanner scanner = new Scanner(System.in);
			String orderOrMessage = scanner.nextLine();

			if(orderOrMessage.startsWith("stop")) {
				basic.close();
				break;
			}

			String[] split = orderOrMessage.split(":");

			if(split.length != 2) {
				continue;
			}

			Group group = basic.getRemoteProxyObj(Group.class);

			try {
				System.out.println("发送结果:" + group.sendMessage(split[0],split[1]));
				System.out.println("服务器状态:" + basic.getIsReady());
			} catch (Exception e) {
				e.printStackTrace();
			}



//			if (split[0].equals("sendFiles")){
//				String[] ss = split[1].split(",");
//				SingleFileStreamImpl.send(ss[0],ss[1]);
//			} else {
//				System.out.println("发送结果:" + GroupImpl.sendMessage(split[0],split[1]));
//			}
		}
		System.exit(0);
	}
}
