package cn.gp.test;

import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

import java.util.concurrent.TimeUnit;

/**
 * 一个打印当前读写总量的工具
 */
public class PrintTraffic {

	public static void printTraffic(final GlobalTrafficShapingHandler trafficShapingHandler) {
		new Thread(new Runnable() {

			public void run() {
				while(true) {
					TrafficCounter trafficCounter = trafficShapingHandler.trafficCounter();
					try {
						TimeUnit.SECONDS.sleep(5);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					final long totalRead = trafficCounter.cumulativeReadBytes();
					final long totalWrite = trafficCounter.cumulativeWrittenBytes();
					System.out.println(trafficCounter + ", Total read:" + (totalRead >> 10) + " KB, Total write:" + (totalWrite >> 10) + " KB");
				}
			}
		}).start();
	}
}
