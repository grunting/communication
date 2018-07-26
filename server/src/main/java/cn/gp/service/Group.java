package cn.gp.service;

import java.util.List;
import java.util.Set;

/**
 * 客户端分组接口
 */
public interface Group {

    /**
     * 客户端创建分组神情
     * @return 返回创建结果
     */
    boolean createGroup(List<String> names) throws Exception;

    /**
     * 接收分组请求
     * @param groupId 分组id
     * @param crypto 秘钥
     * @return 返回接收成功与否
     */
    boolean recoveGroup(String groupId,byte[] crypto);

    /**
     * 查看当前用户参加了多少分组
     * @return 分组列表
     */
    Set<String> showSelfGroups();

    /**
     * 查看指定分组内成员列表
     * @param groupId 分组id
     * @return 返回用户列表
     */
    Set<String> showGroupUsers(String groupId);

    /**
     * 向分组发送消息
     * @param groupId 分组id
     * @param message 信息
     * @return 返回发送成功与否
     */
    boolean sendMessage(String groupId,byte[] message);

    /**
     * 接受分组发送过来的消息
     * @param groupId 分组id
     * @param sparker 发送者
     * @param message 信息
     * @return 返回接收成功与否
     */
    boolean recoveMessage(String groupId, String sparker,byte[] message);
}
