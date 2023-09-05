package edu.hniu.imchatroom;

import edu.hniu.imchatroom.filter.SystemFilter;
import edu.hniu.imchatroom.interceptor.LoginInterceptor;
import edu.hniu.imchatroom.model.bean.Message;
import edu.hniu.imchatroom.model.enums.ResponseCodeEnum;
import edu.hniu.imchatroom.model.enums.RoleEnum;
import edu.hniu.imchatroom.util.EncryptUtil;
import edu.hniu.imchatroom.util.MoreUtil;
import edu.hniu.imchatroom.util.StringUtil;
import org.junit.jupiter.api.Test;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static edu.hniu.imchatroom.util.VariableUtil.ADMIN_USER_NAME;

public class EntityTests {

    @Test
    void testMD5() throws Exception {
        String password = "123456";
        System.out.println(EncryptUtil.encodeByMd5(password));		// MD5加密
    }

    @Test
    void testRandomCode() {
        System.out.println(StringUtil.getRandomCode(8));
        System.out.println(StringUtil.getRandomCode(true));
        System.out.println(StringUtil.getRandomCode(false));
    }

    @Test
    void testEnum1() {
        System.out.println(RoleEnum.USER);  // User
        System.out.println(RoleEnum.ADMIN); // Admin
        System.out.println(RoleEnum.valueOf("ADMIN").getClass().getTypeName()); // edu.hniu.blueskychatroomdemo.enums.Role
        System.out.println(RoleEnum.valueOf("ADMIN").toString().equals("ADMIN"));
    }

    @Test
    void testEnum2() {
        System.out.println(ResponseCodeEnum.SUCCESS);
        System.out.println(ResponseCodeEnum.SUCCESS.getClass().getTypeName());
        System.out.println(ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS));
        System.out.println(ResponseCodeEnum.getCode(ResponseCodeEnum.FAILED));
        System.out.println(ResponseCodeEnum.getDesc(ResponseCodeEnum.WARNING));
    }

    @Test
    void testEnum3() {
        System.out.println(ADMIN_USER_NAME);
        System.out.println(ADMIN_USER_NAME.getClass().getTypeName());
    }

    @Test
    void testGetExclusivePath() {
        String param = StringUtil.getExclusivesPath(LoginInterceptor.class);
        System.out.println(param);
        String[] temp = param.split(", ");
        System.out.println(Arrays.toString(temp));

        temp = StringUtil.getExclusivesArrayPath(SystemFilter.class);
        System.out.println(Arrays.toString(temp));

       /* String[] strings = {"/css/**", "/js/**", "/images/**", "/favicon.ico"};
        String[] strings1 = {"/login", "/signin", "/user/login", "/user/signin",
                "/user/signup", "/verifyCode/*", "/user/activeUserAccount/*", "/user/logout", "/error", "/main"};
        String[] newString = new String[strings.length + strings1.length];
        System.arraycopy(strings, 0, newString, 0, strings.length);
        System.arraycopy(strings1, 0, newString, strings.length, strings1.length);
        System.out.println(Arrays.toString(newString));*/
    }

    @Test
    void testExcludeHiddenItems() {
        List<Object> objects = MoreUtil.excludeItems(
                List.of(
                        new Message("asd", "asdasdf", null, "1"),
                        new Message("gdhj", "fghgfd", null, "1"),
                        new Message("fghgj", "hgvbcvsd", null, "0"),
                        new Message("dfhghjgh", "ghjcvbn", null, "1"),
                        new Message("xcuiofd", "3dsfcgv", null, "0")
                ), true
        );
        objects.forEach(obj -> System.out.println(obj));

        objects = MoreUtil.excludeItems(
                new Message("fghgj", "hgvbcvsd", null, "0"), true
        );
        objects.forEach(obj -> System.out.println(obj));
    }

    @Test
    void testEncodeText() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String value = "你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy";
        System.out.println("待加密值：" + value);
        // 加密算法
        String algorithm = "DES";
        // 转换模式
        String transformation = "DES";
        // --- 生成秘钥 ---
        // 实例化秘钥生成器
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        // 初始化秘钥长度
        keyGenerator.init(56);
        // 生成秘钥
        SecretKey secretKey = keyGenerator.generateKey();
        // 实例化DES秘钥材料
        DESKeySpec desKeySpec = new DESKeySpec(secretKey.getEncoded());
        // 实例化秘钥工厂
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(algorithm);
        // 生成DES秘钥
        SecretKey desSecretKey = secretKeyFactory.generateSecret(desKeySpec);
        System.out.println("DES秘钥：" + Base64.getEncoder().encodeToString(desSecretKey.getEncoded()));

        // 实例化密码对象
        Cipher cipher = Cipher.getInstance(transformation);
        // 设置模式（ENCRYPT_MODE：加密模式；DECRYPT_MODE：解密模式）和指定秘钥
        cipher.init(Cipher.ENCRYPT_MODE, desSecretKey);
        // 加密
        byte[] encrypt = cipher.doFinal(value.getBytes());
        System.out.println("DES加密结果：" + Base64.getEncoder().encodeToString(encrypt));
        // 解密
        // 设置为解密模式
        cipher.init(Cipher.DECRYPT_MODE, desSecretKey);
        byte[] decrypt = cipher.doFinal(encrypt);
        System.out.println("DES解密结果：" + new String(decrypt));
    }

    @Test
    void testEncrypt() throws Exception {
        String value = "你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy你好，我叫Lucy";

        /*String encryptedText = EncryptUtil.encryptText(value);
        String result = EncryptUtil.decryptText(encryptedText);*/
//        String result = EncryptUtil.decryptText("Oq+clTgAjvfJzTuILjY0dCcGnj2wboKFuvVvj8fWzo8r9ITj0V1V/gugsZNAd8o7Z6KzQOXzbszfUFYe1nwbnqb8SHUSrbf/ceLfXFudaHXb6QjnaECCZoK8StCFx70/c5ENMSU0F1S8mWMLy5pnyratwbkhDS7ltXMAhwUlkDdWk5HWgS1fDSYHYXEkJkbMnlU9ZFaFSIs6r5yVOACO98nNO4guNjR0JwaePbBugoW69W+Px9bOjyv0hOPRXVX+C6Cxk0B3yjtnorNA5fNuzN9QVh7WfBuepvxIdRKtt/9x4t9cW51oddvpCOdoQIJmgrxK0IXHvT9zkQ0xJTQXVLyZYwvLmmfKtq3BuSENLuW1cwCHBSWQN1aTkdaBLV8NJgdhcSQmRsyeVT1kVoVIizqvnJU4AI73yc07iC42NHQnBp49sG6Chbr1b4/H1s6PK/SE49FdVf4LoLGTQHfKO2eis0Dl827M31BWHtZ8G56m/Eh1Eq23/3Hi31xbnWh12+kI52hAgmaCvErQhce9P3ORDTElNBdUvJljC8uaZ8o9G8jE/LQjhw==");
       /* System.out.println("==>");
        secretKeyStringMap.forEach((secretKey, encryptedText) -> System.out.println(secretKey + "= " + encryptedText));
        System.out.println("desDecryptedText= " + result);*/

        Message message1 = new Message();
        message1.setContent("你好");
        Message message2 = new Message();
        message2.setContent("我叫ming");
        Message message3 = new Message();
        message3.setContent("Lux");
        Message message4 = new Message();
        message4.setContent("今天天气不错");

        System.out.println("Message 加密中...");
        List<Message> messages = List.of(message1, message2, message3, message4);
        for (Message message : messages) {
            String encryptedText = EncryptUtil.encryptText(String.valueOf(message.getContent()));
            message.setContent(encryptedText);
        }
        messages.forEach(message -> System.out.println(message));

        System.out.println("\nMessage 解密中...");
        for (Message message : messages) {
            String decryptedText = EncryptUtil.decryptText(String.valueOf(message.getContent()));
            message.setContent(decryptedText);
        }
        messages.forEach(message -> System.out.println(message));
    }


}
