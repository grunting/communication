package cn.gp.crypto;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 单向加密采用SHA
 */
public class SHA {

	/**
	 * SHA签名
	 * @param src 签名内容
	 * @return 返回签名,40个字符
	 */
	public static String encodeSHA(byte[] src) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(src);
			return Hex.encodeHexString(md.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
}
