package cn.gp.model;

import cn.gp.crypto.JksTool;
import cn.gp.util.IndexTest;

/**
 * 基础信息类
 */
public class Basic {

    private static String name;

    // 记录当前channel和客户端的对应关系
    private static final IndexTest<ClientBean> index = new IndexTest<ClientBean>();

    static {
        name = JksTool.getAlias();
    }

    /**
     * 给出索引项
     * @return 返回索引根
     */
    public static IndexTest<ClientBean> getIndex() {
        return index;
    }

    /**
     * 本机名
     * @return 返回本机名称
     */
    public static String getName() {
        return name;
    }
}
