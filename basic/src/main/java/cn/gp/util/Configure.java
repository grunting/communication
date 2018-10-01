package cn.gp.util;

import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;

/**
 * 配置类
 */
public class Configure {

	// 配置文件实例
	private Properties properties;

	/**
	 * 实例化一个空的配置文件
	 */
	public Configure() {
		super();
		properties = new Properties();
	}

	/**
	 * 根据名称加载内部文件作为配置文件实例
	 * @param configurePath 配置文件名
	 * @param defaultConfigure 最优先的外部配置键
	 * @return 返回配置实例
	 */
	public static Configure getInstance(String configurePath,String defaultConfigure) {
		Configure configure = new Configure();
		try {

			String config = System.getProperty(defaultConfigure);
			if (config == null) {
				ClassLoader runtime = Thread.currentThread().getContextClassLoader();
				configure.properties.load(runtime.getResourceAsStream(configurePath));
			} else {
				configure.properties.load(new FileInputStream(config));
			}
			return configure;
		} catch (Exception e) {
			e.printStackTrace();
			return configure;
		}
	}

	public Properties getProperties() {
		return properties;
	}

	/**
	 * 根据自定义参数集实例化配置
	 * @param params 参数列表
	 * @return 返回配置实例
     */
	public static Configure getInstance(Map<String,String> params) {
		Configure configure = new Configure();
		configure.properties.putAll(params);
		return configure;
	}

	/**
	 * 获取配置文件内容
	 * @param constant 枚举
	 * @return 返回配置内容
	 */
	public String getConfigString(Constant constant,String defaultValue) {

		if (properties.containsKey(Constant.getRealName(constant))) {
			return properties.getProperty(Constant.getRealName(constant));
		} else {
			return defaultValue;
		}
	}

	/**
	 * 获取配置文件内容,默认空
	 * @param constant 枚举
	 * @return 返回配置内容
	 */
	public String getConfigString(Constant constant) {
		return getConfigString(constant,"");
	}

	/**
	 * 获取配置文件内容
	 * @param constant 枚举
	 * @return 返回配置数字
	 */
	public int getConfigInteger(Constant constant,int defaultValue) {

		if (properties.containsKey(Constant.getRealName(constant))) {
			try {
				return Integer.parseInt(properties.getProperty(Constant.getRealName(constant)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	/**
	 * 获取配置文件内容,默认0
	 * @param constant 枚举
	 * @return 返回配置数字
	 */
	public int getConfigInteger(Constant constant) {
		return getConfigInteger(constant,0);
	}

	/**
	 * 获取配置文件内容
	 * @param constant 枚举
	 * @return 返回配置布尔值
	 */
	public boolean getConfigBoolean(Constant constant,boolean defaultValue) {

		if (properties.contains(Constant.getRealName(constant))) {
			try {
				return Boolean.parseBoolean(properties.getProperty(Constant.getRealName(constant)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultValue;
	}

	/**
	 * 获取配置文件内容,默认false
	 * @param constant 枚举
	 * @return 返回配置布尔值
	 */
	public boolean getConfigBoolean(Constant constant) {
		return getConfigBoolean(constant,false);
	}
}
