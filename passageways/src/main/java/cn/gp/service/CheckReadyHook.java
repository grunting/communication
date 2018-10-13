package cn.gp.service;

import java.util.List;

/**
 * 服务器重试成功后需要有一定的后续操作
 */
public interface CheckReadyHook {

    /**
     * 后续操作的钩子函数
     * @return 返回结果
     */
    boolean checkReadyHook();

    /**
     * 发送远端执行任务时,属于钩子函数的实例发送任务时,不检查服务器是否准备好(只有钩子函数执行完,服务器才会准备好)
     * @return 需要优先于其他服务处理的服务列表
     */
    List<Class> getCheckReadyHookList();

    /**
     * 丢失服务器的后续处理
     */
    void lostServer();
}
