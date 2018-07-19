package cn.gp.util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * 配置项简易实现
 */
public class Configure {

    private static final Properties properties = new Properties();

    static {
        try {
            ClassLoader runtime = Thread.currentThread().getContextClassLoader();

            String config = System.getProperty("client.config");
            if(config == null) {
                properties.load(runtime.getResourceAsStream("client.properties"));
            } else {
                properties.load(new FileInputStream(config));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件失败");
            System.exit(1);
        }
    }

    /**
     * 获取配置文件内容
     * @param constant 枚举
     * @return 返回配置内容
     */
    public static String getConfigString(Constant constant) {
        return properties.getProperty(Constant.getRealName(constant));
    }

    /**
     * 获取配置文件内容
     * @param constant 枚举
     * @return 返回配置数字
     */
    public static int getConfigInteger(Constant constant) {
        return Integer.parseInt(properties.getProperty(Constant.getRealName(constant)));

    }
}
