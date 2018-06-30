package cn.gp.util;

import java.io.*;

/**
 * 工具类
 */
public class ByteAndObject {

    /**
     * object转换为bytes
     * @param obj 对象
     * @return 字节数组
     */
    public static byte[] toByArray(Object obj) {

        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * bytes转为object
     * @param bytes 字节数组
     * @return object对象
     */
    public static Object toObject(byte[] bytes) {
        Object obj = null;
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
