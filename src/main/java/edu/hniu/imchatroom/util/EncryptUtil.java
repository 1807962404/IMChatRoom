package edu.hniu.imchatroom.util;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 加密工具类
 */
public final class EncryptUtil {
	private EncryptUtil(){}

	private static final String[] hex = {"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"};
	/**
	 * 将明文密码转成MD5密码
	 *  写一个MD5算法,运行结果与MySQL的md5()函数相同，将明文密码转成MD5密码
	 *  如 123456->e10adc3949ba59abbe56e057f20f883e
	 */
	public static String encodeByMd5(String password) throws Exception{
		//Java中MessageDigest类封装了MD5和SHA算法，今天我们只要MD5算法
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		//调用MD5算法，即返回16个byte类型的值
		byte[] byteArray = md5.digest(password.getBytes());
		//注意：MessageDigest只能将String转成byte[]，接下来的事情，由我们程序员来完成
		return byteArrayToHexString(byteArray);
	}
	/**
	 * 将byte[]转在16进制字符串 
	 */
	private static String byteArrayToHexString(byte[] byteArray) {
		StringBuffer sb = new StringBuffer();
		//遍历
		for(byte b : byteArray){//16次
			//取出每一个byte类型，进行转换
			String hex = byteToHexString(b);
			//将转换后的值放入StringBuffer中
			sb.append(hex);
		}
		return sb.toString();
	}
	/**
	 * 将byte转在16进制字符串 
	 */
	private static String byteToHexString(byte b) {//-31转成e1，10转成0a，。。。
		//将byte类型赋给int类型
		int n = b;
		//如果n是负数
		if(n < 0){
			//转正数
			//-31的16进制数，等价于求225的16进制数 
			n = 256 + n;
		}
		//商(14)，数组的下标
		int d1 = n / 16;
		//余(1)，数组的下标
		int d2 = n % 16;
		//通过下标取值
		return hex[d1] + hex[d2];
	}

	// 加密算法
	private static final String algorithm = "DES";
	// 转换模式
	private static final String transformation = "DES";
	// 加解密统一使用的编码方式
	private final static String encoding = "utf-8" ;
	// 密钥
	private static final String secretKey = "1687ABD38950D78E7D55A6095CCBBFB3";
	private static final SecretKey desSecretKey;
	// 密码对象
	private static final Cipher cipher;
	static {
		try {
			// 1、实例化密码对象
			cipher = Cipher.getInstance(transformation);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}

		// 2、实例化DES秘钥材料
		DESKeySpec desKeySpec = null;
		try {
			desKeySpec = new DESKeySpec(secretKey.getBytes());
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}

		// 3、实例化秘钥工厂
		SecretKeyFactory secretKeyFactory = null;
		try {
			secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		// 4、生成DES秘钥
		try {
			desSecretKey = secretKeyFactory.generateSecret(desKeySpec);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
//		System.out.println("生成DES密钥：" + desSecretKey);
	}
	/**
	 * 使用DES加密技术：加密文本
	 * @param text
	 * @return
	 */
	public static String encryptText(String text) throws Exception {

		// 设置模式（ENCRYPT_MODE：加密模式；DECRYPT_MODE：解密模式）和指定秘钥
		cipher.init(Cipher.ENCRYPT_MODE, desSecretKey);

		// 加密文本数据
		byte[] encrypt = cipher.doFinal(text.getBytes(encoding));

		String desEncryptedText = Base64.getEncoder().encodeToString(encrypt);
//		System.out.println("DES加密结果：" + desEncryptedText);

		return desEncryptedText;
	}

	/**
	 * 使用DES解密技术：解密 经过des加密技术的文本
	 * @param encryptedText
	 * @return
	 */
	public static String decryptText(String encryptedText) throws Exception {

		// 设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, desSecretKey);

		// 解密数据
		byte[] decrypt = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

		String desDecryptedText = new String(decrypt, encoding);
//		System.out.println("DES解密结果：" + desDecryptedText);

		return desDecryptedText;
	}
}
