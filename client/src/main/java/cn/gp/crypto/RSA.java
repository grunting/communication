package cn.gp.crypto;


import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import com.google.protobuf.ByteString;
import org.apache.commons.codec.binary.Base64;


/**
 * 非对称加密采用RSA
 */
public class RSA {

    /**
     * 根据秘钥生成proto的公钥的byte类型数据
     * @param keyPair 秘钥对
     * @return 返回ByteString类型的公钥(便于传输)
     */
    public static ByteString getPublicKeyOfByteString(KeyPair keyPair) {
        byte[] publicOfKey = keyPair.getPublic().getEncoded();
        return ByteString.copyFrom(Base64.encodeBase64(publicOfKey));
    }

    /**
     * 转成PublicKey实例
     * @param publicKey 公钥
     * @return 公钥实例
     * @throws Exception
     */
    public static PublicKey getPublicKey(byte[] publicKey) throws Exception{

        byte[] keyBytes=Base64.decodeBase64(publicKey);
        X509EncodedKeySpec keySpec=new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory=KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 转成PrivateKey实例
     * @param privateKey 私钥
     * @return 私钥实例
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(byte[] privateKey) throws Exception{
        byte[ ] keyBytes=Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec=new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory= KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 根据长度生成密钥对
     * @param keyLength 生成秘钥对强度
     * @return 秘钥对
     * @throws Exception
     */
    public static KeyPair genKeyPair(int keyLength) throws Exception{
        KeyPairGenerator keyPairGenerator= KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keyLength);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 公钥加密
     * @param content 明文
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception
     */
    public static byte[] encrypt(byte[] content, PublicKey publicKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");//java默认"RSA"="RSA/ECB/PKCS1Padding"
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(content);
    }

    /**
     * 私钥解密
     * @param content 密文
     * @param privateKey 秘钥
     * @return 明文
     * @throws Exception
     */
    public static byte[] decrypt(byte[] content, PrivateKey privateKey) throws Exception{
        Cipher cipher=Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(content);
    }

    /**
     * 测试
     * @param args 空
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        KeyPair keyPair = RSA.genKeyPair(1024);

        PublicKey publicKey = keyPair.getPublic();

        byte[] key = AES.getKey();
        System.out.println(Arrays.toString(key));


        byte[] crypto = RSA.encrypt(key,publicKey);
        System.out.println(new String(crypto));

        String test = Base64.encodeBase64String(crypto);
        System.out.println(test);

        byte[] real = RSA.decrypt(Base64.decodeBase64(test),keyPair.getPrivate());
        System.out.println(Arrays.toString(real));
    }
}
