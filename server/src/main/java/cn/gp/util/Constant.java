package cn.gp.util;

/**
 * 常量定义
 */
public enum Constant {

    SERVER_PORT,                // 服务器端口

    SERVER_JKS_PATH,            // 客户端jks文件路径
    SERVER_JKS_PASS,            // 客户端jks文件访问密码

    SERVER_NETTY_WRITELIMIT,    // 客户端netty写入限制
    SERVER_NETTY_READLIMIT;     // 客户端netty读入限制



    /**
     * 获取真正的key串
     * @param constant 枚举
     * @return 返回真正的key串
     */
    protected static String getRealName(Constant constant) {

        switch (constant) {
            case SERVER_PORT:
                return "server.port";

            case SERVER_JKS_PATH:
                return "server.jks.path";
            case SERVER_JKS_PASS:
                return "server.jks.pass";

            case SERVER_NETTY_WRITELIMIT:
                return "server.netty.writelimit";
            case SERVER_NETTY_READLIMIT:
                return "server.netty.readlimit";
            default:
                return "";
        }
    }
}
