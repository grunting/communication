package cn.gp.util;

import java.io.FileInputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * jks文件访问类
 */
public class JksTool {


	// 签名文件实例
	private KeyStore keyStore;

	// 含有秘钥的别称
	private String alias;

	// 个人秘钥对
	private KeyPair keyPair;

	// 可信列表
	private Map<String,PublicKey> trustMap;

	private JksTool() {
		super();
	}

	public String getAlias() {
		return alias;
	}

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public Map<String, PublicKey> getTrustMap() {
		return trustMap;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public static JksTool getInstance(String jksPath, String keyPass, String storePass) {
		try {
			JksTool jksTool = new JksTool();
			jksTool.trustMap = new HashMap<String, PublicKey>();

			jksTool.keyStore = KeyStore.getInstance("JKS");
			jksTool.keyStore.load(new FileInputStream(jksPath),keyPass.toCharArray());

			PublicKey publicKey = null;
			PrivateKey privateKey = null;

			Enumeration<String> aliases = jksTool.keyStore.aliases();
			while(aliases.hasMoreElements()) {
				String smallAlias = aliases.nextElement();
				PublicKey smallPublicKey = jksTool.keyStore.getCertificate(smallAlias).getPublicKey();
				PrivateKey smallPrivateKey = (PrivateKey) jksTool.keyStore.getKey(smallAlias,storePass.toCharArray());

				if (smallPrivateKey != null) {
					jksTool.alias = smallAlias;
					publicKey = smallPublicKey;
					privateKey = smallPrivateKey;
				} else {
					jksTool.trustMap.put(smallAlias,smallPublicKey);
				}
			}
			if (privateKey == null) {
				throw new RuntimeException("jks文件中" + jksPath + "没有私钥信息,无法创建个人档案");
			} else {
				jksTool.keyPair = new KeyPair(publicKey,privateKey);
			}
			return jksTool;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
