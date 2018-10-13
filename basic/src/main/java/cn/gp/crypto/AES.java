package cn.gp.crypto;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * 对称加密采用AES
 */
public class AES {

    private Cipher cipher;
    private SecretKey secretKey;

    /**
     * 根据随机生成的key生成加密对象
     * @param key 随机key
     */
    public AES(byte[] key) {
        cipher = getCipher();
        secretKey = getSecretKey(key);
    }

    /**
     * 根据自定义key生成加密对象
     * @param key 自定义key
     */
    public AES(String key) {
        cipher = getCipher();
        secretKey = getSecretKey(key);
    }

    /**
     * 生成base64整理的byte
     * @return 返回随机key
     */
    public static byte[] getKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.encodeBase64(secretKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据指定字符串生成密钥对象
     * @return 秘钥
     */
    private SecretKey getSecretKey(String strKey) {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(strKey.getBytes());

            //创建密钥生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            //初始化密钥
            keyGenerator.init(random);
            //生成密钥
            return keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据key生成SecretKey
     * @param key 随机key
     * @return 返回SecretKey
     */
    private SecretKey getSecretKey(byte[] key) {
        byte[] realkey = Base64.decodeBase64(key);
        return new SecretKeySpec(realkey,"AES");
    }


    /**
     * 生成getCipher
     * @return 返回Cipher
     */
    private static Cipher getCipher() {
        try {
            return Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES加密算法
     * @param src 明文
     * @return 密文
     */
    public byte[] encode(byte[] src) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] resultBytes = cipher.doFinal(src);
            return Base64.encodeBase64(resultBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 算法解密
     * @param src 密文
     * @return 明文
     */
    public byte[] decode(byte[] src) {
        try {
            byte[] realsrc = Base64.decodeBase64(src);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(realsrc);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

