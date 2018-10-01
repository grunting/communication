package cn.gp.service;

/**
 * 服务器重试成功后需要有一定的后续操作
 */
public interface CheckReadyHook {

	/**
	 * 后续操作的钩子函数
	 * @return 返回结果
	 */
	boolean checkReadyHook() throws Exception;
}
