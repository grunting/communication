package cn.gp.client.impl;

import cn.gp.client.Report;
import cn.gp.core.Basic;
import cn.gp.service.CheckReadyHook;

/**
 * 钩子函数实现
 */
public class CheckReadyHookImpl implements CheckReadyHook {

	private Basic basic;

	public CheckReadyHookImpl(Basic basic) {
		this.basic = basic;
	}

	public boolean checkReadyHook() throws Exception {
		Report reportImpl = basic.getRemoteProxyObj(Report.class);
		return reportImpl.send();
	}
}
