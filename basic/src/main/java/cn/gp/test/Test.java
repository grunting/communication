package cn.gp.test;

import cn.gp.core.Client;
import cn.gp.client.Report;

/**
 * 测试
 */
public class Test {
	public static void main(String[] args) throws Exception {
		Client client = Client.getInstance();

		client.putServiceInterFace(Report.class.getName(),ReportImpl.class);

		ReportImpl.send(client);

		Thread.sleep(10000);

		client.close();
	}
}
