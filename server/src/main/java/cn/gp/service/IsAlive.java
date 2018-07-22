package cn.gp.service;

import io.netty.util.internal.ConcurrentSet;

import java.util.concurrent.ConcurrentMap;

/**
 * 索引会用到的询问接口
 */
public interface IsAlive<K> {

    boolean isAlive();

    void setDie();

    void setProperties(String key, String value,ConcurrentMap<String,ConcurrentMap<String,ConcurrentSet<K>>> index);
}
