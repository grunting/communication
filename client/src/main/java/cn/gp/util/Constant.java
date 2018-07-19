package cn.gp.util;

/**
 * 常量定义
 */
public enum Constant {

    CLIENT_NAME,                // 客户端名称

    CLIENT_JKS_PATH,            // 客户端jks文件路径
    CLIENT_JKS_PASS,            // 客户端jks文件访问密码

    CLIENT_NETTY_WRITELIMIT,    // 客户端netty写入限制
    CLIENT_NETTY_READLIMIT,     // 客户端netty读入限制

    SERVER_HOST,                // 服务器地址
    SERVER_PORT;                // 服务器端口

    /**
     * 获取真正的key串
     * @param constant 枚举
     * @return 返回真正的key串
     */
    protected static String getRealName(Constant constant) {

        switch (constant) {
            case CLIENT_NAME:
                return "client.name";
            case CLIENT_JKS_PATH:
                return "client.jks.path";
            case CLIENT_JKS_PASS:
                return "client.jks.pass";
            case CLIENT_NETTY_WRITELIMIT:
                return "client.netty.writelimit";
            case CLIENT_NETTY_READLIMIT:
                return "client.netty.readlimit";

            case SERVER_HOST:
                return "server.host";
            case SERVER_PORT:
                return "server.port";
            default:
                return "";
        }
    }
}
