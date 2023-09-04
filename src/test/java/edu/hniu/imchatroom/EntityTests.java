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

import java.util.Arrays;
import java.util.List;

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
}
