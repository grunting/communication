package cn.gp.crypto;

import cn.gp.model.Basic;
import cn.gp.util.Configure;
import cn.gp.util.Constant;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 对jks文件的操作
 */
public class JksTool {

    private static KeyStore keyStore;
    private static String alias;
    private static KeyPair keyPair;
    private static Map<String,PublicKey> trustMap;


    static {
        try {
            trustMap = new HashMap<String, PublicKey>();

            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(
                    new FileInputStream(Configure.getConfigString(Constant.CLIENT_JKS_PATH))
                    ,Configure.getConfigString(Constant.CLIENT_JKS_KEYPASS).toCharArray());

            PublicKey publicKey = null;
            PrivateKey privateKey = null;

            Enumeration<String> aliases = keyStore.aliases();
            while(aliases.hasMoreElements()) {
                String smallAlias = aliases.nextElement();
                PublicKey smallPublicKey = keyStore.getCertificate(smallAlias).getPublicKey();
                PrivateKey smallPrivateKey = (PrivateKey) keyStore.getKey(smallAlias, Configure.getConfigString(Constant.CLIENT_JKS_STOREPASS).toCharArray());

                if (smallPrivateKey != null) {
                    alias = smallAlias;
                    publicKey = smallPublicKey;
                    privateKey = smallPrivateKey;
                } else {
                    trustMap.put(smallAlias,publicKey);
                }
            }
            keyPair = new KeyPair(publicKey,privateKey);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取本机秘钥对
     * @return 秘钥对
     */
    public static KeyPair getKeyPair() {
        return keyPair;
    }

    /**
     * 获取拥有秘钥的别称
     * @return 别称
     */
    public static String getAlias() {
        return alias;
    }

    /**
     * 返回签名文件实例
     * @return 签名文件实例
     */
    public static KeyStore getKeyStore() {
        return keyStore;
    }

    /**
     * 获取可信列表
     * @return 可信列表
     */
    public static Map<String,PublicKey> getTrustMap() {
        return trustMap;
    }
}
