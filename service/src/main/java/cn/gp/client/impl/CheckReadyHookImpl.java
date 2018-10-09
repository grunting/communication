package cn.gp.client.impl;

import cn.gp.client.Report;
import cn.gp.core.Basic;
import cn.gp.server.RegisterServer;
import cn.gp.server.impl.RegisterServerImpl;
import cn.gp.service.CheckReadyHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 钩子函数实现
 */
public class CheckReadyHookImpl implements CheckReadyHook {

	private static final Logger logger = LoggerFactory.getLogger(CheckReadyHookImpl.class);

	private Basic basic;

	public CheckReadyHookImpl(Basic basic) {
		this.basic = basic;
	}

	public boolean checkReadyHook() throws Exception {

		logger.debug("checkReadyHook");

		Report reportImpl = basic.getRemoteProxyObj(Report.class);
		return reportImpl.send();
	}

	public List<Class> getCheckReadyHookList() {

		logger.debug("getCheckReadyHookList");

		List<Class> list = new ArrayList<Class>();
		list.add(basic.getServiceInterface(Report.class.getName()));
		list.add(RegisterServer.class);
		return list;
	}
}
