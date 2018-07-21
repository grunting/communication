package cn.gp.util;

import cn.gp.service.IsAlive;
import io.netty.util.internal.ConcurrentSet;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 对索引的简单实现
 */
public class IndexTest<K extends IsAlive> {

    private ConcurrentMap<String,ConcurrentMap<String,ConcurrentSet<K>>> index;

    /**
     * 是否存在这个索引
     * @param key 键
     * @param value 值
     * @return 是否存在这个索引
     */
    public boolean contains(String key,String value) {
        if (index == null) {
            return false;
        }

        ConcurrentMap<String,ConcurrentSet<K>> values = null;
        if (index.containsKey(key)) {
            values = index.get(key);
        } else {
            return false;
        }

        return values.containsKey(value);
    }

    /**
     * 是否存在这个索引
     * @param key 键
     * @return 是否存在这个索引
     */
    public boolean contains(String key) {
        if (index == null) {
            return false;
        }

        return index.containsKey(key);
    }



    /**
     * 添加索引
     * @param key 键
     * @param value 值
     * @param node 内容
     */
    public synchronized void setIndex(String key,String value,K node) {

        if (index == null) {
            index = new ConcurrentHashMap<String, ConcurrentMap<String, ConcurrentSet<K>>>();
        }

        ConcurrentMap<String,ConcurrentSet<K>> values;
        if (index.containsKey(key)) {
            values = index.get(key);
        } else {
            values = new ConcurrentHashMap<String, ConcurrentSet<K>>();
        }

        ConcurrentSet<K> nodes;
        if (values.containsKey(value)) {
            nodes = values.get(value);
        } else {
            nodes = new ConcurrentSet<K>();
        }

        nodes.add(node);
        values.put(value,nodes);
        index.put(key,values);
    }

    /**
     * 获取所有节点
     * @return 节点集合
     */
    public Set<K> getAllNode() {
        Set<K> result = new HashSet<K>();
        if (index != null) {
            for (String key : index.keySet()) {
                ConcurrentMap<String,ConcurrentSet<K>> values = index.get(key);
                for (String value : values.keySet()) {
                    ConcurrentSet<K> con = values.get(value);
                    for (K k : con) {
                        if (k.isAlive()) {
                            result.add(k);
                        } else {
                            con.remove(k);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 根据键获取集合
     * @param key 键
     * @return 集合
     */
    public Set<K> getNode(String key) {
        Set<K> result = new HashSet<K>();
        ConcurrentMap<String, ConcurrentSet<K>> con = index.get(key);
        if (con != null) {
            for (String value : con.keySet()) {
                ConcurrentSet<K> ks = con.get(value);
                for (K k : ks) {
                    if (k.isAlive()) {
                        result.add(k);
                    } else {
                        con.get(value).remove(k);
                    }
                }
            }
        }

        return result;
    }

    /**
     * 根据键值获取集合
     * @param key 键
     * @param value 值
     * @return 集合
     */
    public Set<K> getNode(String key,String value) {
        Set<K> result = new HashSet<K>();
        ConcurrentMap<String, ConcurrentSet<K>> con1 = index.get(key);
        if (con1 != null) {
            ConcurrentSet<K> con2 = con1.get(value);
            if (con2 != null) {
                for (K k : con2) {
                    if (k.isAlive()) {
                        result.add(k);
                    } else {
                        con2.remove(k);
                    }
                }
            }
        }
        return result;
    }


//    static class Test implements IsAlive {
//
//        private String name;
//        private boolean b;
//
//        public Test(String name) {
//            this.name = name;
//        }
//
//        public void setAlive(boolean b) {
//            this.b = b;
//        }
//
//        @Override
//        public String toString() {
//            return name;
//        }
//
//        public boolean isAlive() {
//            return this.b;
//        }
//    }
//
//    public static void main(String[] args) {
//
//        IndexTest<Test> indexTest = new IndexTest<Test>();
//
//        Test t1 = new Test("a");
//        t1.setAlive(true);
//        Test t2 = new Test("b");
//        t2.setAlive(true);
//        Test t3 = new Test("c");
//        t3.setAlive(true);
//
//        indexTest.setIndex("group1","1",t1);
//        indexTest.setIndex("group1","2",t2);
//
//        indexTest.setIndex("group2","1",t2);
//        indexTest.setIndex("group2","2",t3);
//
//        indexTest.setIndex("group3","1",t1);
//        indexTest.setIndex("group3","2",t3);
//
//        System.out.println(indexTest.getNode("group1"));
//        System.out.println(indexTest.getNode("group2"));
//        System.out.println(indexTest.getNode("group3"));
//
//        t2.setAlive(false);
//
//        System.out.println(indexTest.getNode("group1"));
//        System.out.println(indexTest.getNode("group2"));
//        System.out.println(indexTest.getNode("group3"));
//
//    }
}