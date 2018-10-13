package cn.gp.util;

/**
 * 枚举类,常量定义
 */
public enum  Constant {

    CLIENT_JKS_PATH,                    // 客户端jks文件路径
    CLIENT_JKS_KEYPASS,                 // 客户端jks文件访问密码
    CLIENT_JKS_STOREPASS,               // 客户端jks文件秘钥访问密码

    CLIENT_TARGET_DIR,                  // 客户端临时文件夹
    CLIENT_TARGET_BATCH,                // 客户端传输文件单次传输大小

    CLIENT_NETTY_WRITELIMIT,            // 客户端netty写入限制
    CLIENT_NETTY_READLIMIT,             // 客户端netty读入限制

    CLIENT_SERVER_HOST,                 // 服务器地址
    CLIENT_SERVER_PORT,                 // 服务器端口
    CLIENT_SERVER_RETRY,				// 重试次数
    CLIENT_SERVER_INTERVAL,				// 重试间隔


    SERVER_PORT,                        // 服务器端口

    SERVER_JKS_PATH,                    // 服务端jks文件路径
    SERVER_JKS_KEYPASS,                 // 服务端jks文件访问密码
    SERVER_JKS_STOREPASS,               // 服务端jks文件秘钥访问密码

    SERVER_NETTY_WRITELIMIT,            // 服务端netty写入限制
    SERVER_NETTY_READLIMIT,             // 服务端netty读入限制

    SERVER_RESTART_RETRY,				// 服务器启动失败重试次数
    SERVER_RESTART_INTERVAL;			// 服务器启动失败重试间隔

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

            case CLIENT_TARGET_DIR:
                return "client.target.dir";
            case CLIENT_TARGET_BATCH:
                return "client.target.batch";

            case CLIENT_NETTY_WRITELIMIT:
                return "client.netty.writelimit";
            case CLIENT_NETTY_READLIMIT:
                return "client.netty.readlimit";


            case CLIENT_SERVER_HOST:
                return "client.server.host";
            case CLIENT_SERVER_PORT:
                return "client.server.port";
            case CLIENT_SERVER_RETRY:
                return "client.server.retry";
            case CLIENT_SERVER_INTERVAL:
                return "client.server.interval";


            case SERVER_PORT:
                return "server.port";

            case SERVER_JKS_PATH:
                return "server.jks.path";
            case SERVER_JKS_KEYPASS:
                return "server.jks.keypass";
            case SERVER_JKS_STOREPASS:
                return "server.jks.storepass";

            case SERVER_NETTY_WRITELIMIT:
                return "server.netty.writelimit";
            case SERVER_NETTY_READLIMIT:
                return "server.netty.readlimit";

            case SERVER_RESTART_RETRY:
                return "server.restart.retry";
            case SERVER_RESTART_INTERVAL:
                return "server.restart.interval";

            default:
                return "";
        }
    }
}
