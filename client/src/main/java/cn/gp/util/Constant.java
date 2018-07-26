package cn.gp.util;

/**
 * 常量定义
 */
public enum Constant {

    CLIENT_JKS_PATH,                    // 客户端jks文件路径
    CLIENT_JKS_KEYPASS,                 // 客户端jks文件访问密码
    CLIENT_JKS_STOREPASS,               // 客户端jks文件秘钥访问密码

    CLIENT_SECURITY_STRICTVAILDATION,   // 客户端是否是严格验证同服务器的其他客户端

    CLIENT_TARGET_DIR,                  // 客户端临时文件夹

    CLIENT_NETTY_WRITELIMIT,            // 客户端netty写入限制
    CLIENT_NETTY_READLIMIT,             // 客户端netty读入限制

    SERVER_HOST,                        // 服务器地址
    SERVER_PORT;                        // 服务器端口

    /**
     * 获取真正的key串
     * @param constant 枚举
     * @return 返回真正的key串
     */
    protected static String getRealName(Constant constant) {

        switch (constant) {
            case CLIENT_JKS_PATH:
                return "client.jks.path";
            case CLIENT_JKS_KEYPASS:
                return "client.jks.keypass";
            case CLIENT_JKS_STOREPASS:
                return "client.jks.storepass";

            case CLIENT_NETTY_WRITELIMIT:
                return "client.netty.writelimit";
            case CLIENT_NETTY_READLIMIT:
                return "client.netty.readlimit";

            case CLIENT_SECURITY_STRICTVAILDATION:
                return "client.security.strictvaildation";

            case CLIENT_TARGET_DIR:
                return "client.target.dir";

            case SERVER_HOST:
                return "server.host";
            case SERVER_PORT:
                return "server.port";
            default:
                return "";
        }
    }
}
