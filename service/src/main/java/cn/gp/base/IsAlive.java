package cn.gp.base;

/**
 * 索引会用到的询问接口
 */
public interface IsAlive {

    /**
     * 当前节点是否存活
     * @return 存活与否
     */
    boolean isAlive();

    /**
     * 设置本节点死亡并进行后续操作
     */
    void setDie();

    /**
     * 给予自身属性
     * @param key 键
     * @param value 值
     */
    void setProperties(String key, String value);
}
