package cn.gp.client.impl;

import cn.gp.client.Group;
import cn.gp.client.Report;
import cn.gp.core.impl.SimpleBasic;
import cn.gp.server.RegisterServer;
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

    private SimpleBasic simpleBasic;

    public CheckReadyHookImpl(SimpleBasic simpleBasic) {
        this.simpleBasic = simpleBasic;
    }

    public boolean checkReadyHook() {

        logger.debug("checkReadyHook");

        Report reportImpl = simpleBasic.getRemoteProxyObj(Report.class);
        return reportImpl.send();
    }

    /**
     * 丢失服务器的后续处理
     */
    public void lostServer() {
        Report report = simpleBasic.getRemoteProxyObj(Report.class);
        report.lostClientAll();
        Group group = simpleBasic.getRemoteProxyObj(Group.class);
        group.lostClientAll();
    }

    public List<Class> getCheckReadyHookList() {

        logger.debug("getCheckReadyHookList");

        List<Class> list = new ArrayList<Class>();
        list.add(simpleBasic.getServiceInterface(Report.class.getName()));
        list.add(RegisterServer.class);
        return list;
    }
}
