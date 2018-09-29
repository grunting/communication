package cn.gp.test;

import cn.gp.core.Client;
import cn.gp.client.Report;
import cn.gp.client.Group;

/**
 * 测试
 */
public class Test {
	public static void main(String[] args) throws Exception {
		Client client = Client.getInstance();

		client.putServiceInterFace(Report.class.getName(),ReportImpl2.class);
		client.putServiceInterFace(Group.class.getName(),GroupImpl2.class);

		ReportImpl2.client = client;
		GroupImpl2.client = client;

		Thread.sleep(5000);

		ReportImpl2.send(client);

		Thread.sleep(100000);

		client.close();
	}
}
