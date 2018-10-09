package cn.gp.banana;

import cn.gp.client.Group;
import cn.gp.client.Report;
import cn.gp.client.impl.CheckReadyHookImpl;
import cn.gp.client.impl.GroupImpl;
import cn.gp.client.impl.ReportImpl;
import cn.gp.core.impl.ClientNetty;
import cn.gp.service.CheckReadyHook;
import cn.gp.test.ScannerHandler;

import java.util.Random;

/**
 * 客户端测试类
 */
public class Client {

	private static volatile ClientNetty clientNetty;

	private Client() {
		super();
	}

	public static ClientNetty getInstance() {
		if (clientNetty == null) {
			synchronized (Client.class) {
				if (clientNetty == null) {
					ClientNetty cli = new ClientNetty();
					cli.setConfigPath("basic.properties","client.conf");
					cli.init();
					CheckReadyHook checkReadyHook = new CheckReadyHookImpl(cli);
					cli.setCheckReadyHook(checkReadyHook);

					cli.putServiceInterface(Report.class.getName(),ReportImpl.class);
					cli.putServiceInterface(Group.class.getName(),GroupImpl.class);

					cli.start();
					clientNetty = cli;
				}
			}
		}
		return clientNetty;
	}

	/**
	 * 当期服务器的人数
	 * @return 数量
	 */
	public static int count() {
		Report report = getInstance().getRemoteProxyObj(Report.class);
		return report.count();
	}

	/**
	 * 发送信息
	 * @param name
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public static String sendMessage(String name,String message) throws Exception {

		Group group = getInstance().getRemoteProxyObj(Group.class);

		Random random = new Random();

		String don = Float.toString(random.nextFloat());

		if (group.sendMessage(name,don + "~~~" + message)) {
			String result;
			int i = 5;
			while ((result = group.getMessage(name + "-" + don)) == null) {
				if (i -- < 1) {
					break;
				}
				Thread.sleep(10000);
			}
			return result;
		}

		return "对方拒绝了通信,或服务器消失了";
	}

	public static void main(String[] args) throws Exception {



//		System.out.println(Client.sendMessage("client2","测试"));

		ScannerHandler sc = new ScannerHandler(Client.getInstance());
		sc.init();
	}
}
