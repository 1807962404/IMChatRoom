package edu.hniu.imchatroom.util;

import edu.hniu.imchatroom.model.enums.RoleEnum;

/**
 * 记录共享变量的工具类
 */
public final class VariableUtil {

    // 项目名称
    public static final String CHATROOM_NAME = "IM";
    // 端口号
    public static final Integer PORT = 80;
    // 网络连接地址
    public static final String ADDRESS = "http://127.0.0.1";
//    public static final String ADDRESS = "http://8.130.104.52";
    // 默认密码
    public static final String DEFAULT_PASSWORD = "123456";
    // 登陆用户
    public static final String SIGNINED_USER = "SIGNINED_USER";

    // 系统广播名称
    public static final String BROADCAST_MESSAGE_NAME = "BROADCAST_MESSAGE";
    // 优文摘要名称
    public static final String ARTICLE_MESSAGE_NAME = "ARTICLE_MESSAGE";

    // 用作随机生成字符串的变量
    public static final String BASE_CODE = "0123456789ABCDEFGHIJKMLNOPQRSTUVWXYZabcdefghijkmlnopqrstuvwxyz";
    // 验证码变量
    public static final String CHECK_CODE = "CHECKCODE_SERVER";

    // 管理员名称
    public static final String ADMIN_USER_NAME = RoleEnum.getRoleName(RoleEnum.ADMIN);
    // 普通用户名称
    public static final String COMMON_USER_NAME = RoleEnum.getRoleName(RoleEnum.USER);

    // 需要排除拦截或过滤的静态资源路径
    public static final String[] STATIC_RESOURCES_PATH = new String[] {"/css/**", "/js/**", "/images/**", "/favicon.ico"};
    // 需要排除拦截或过滤的请求资源路径
    public static final String[] EXCLUSIVE_REQUEST_PATH = new String[] {"/login", "/signin", "/user/login", "/user/signin",
            "/user/signup", "/entity/verify-code/*", "/user/active-user-account/*", "/error",
            "/user/reset-password", "/user/reset-password/*", "/user/chat/online-user-count"};
}
