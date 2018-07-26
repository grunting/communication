package cn.gp.service;

import java.util.Map;
import java.util.Set;

/**
 * 对分组操作的实现
 */
public interface GroupServer {

    /**
     * 创建分组申请
     * @param group 用户列表以及其对应的加密密码
     * @return 分组id
     */
    String createGroup(Map<String,byte[]> group);

    /**
     * 查看指定分组内成员列表
     * @param groupId 分组id
     * @return 返回用户列表
     */
    Set<String> showGroupUsers(String groupId);

    /**
     * 向分组发送数据
     * @param groupId 分组id
     * @param message 消息
     * @return 返回发送成功与否
     */
    boolean sendMessage(String groupId,byte[] message);
}
