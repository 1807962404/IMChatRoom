package edu.hniu.imchatroom.util;

import edu.hniu.imchatroom.model.enums.ResponseCodeEnum;
import edu.hniu.imchatroom.model.enums.RoleEnum;

import java.util.Arrays;

/**
 * 记录共享变量的工具类
 */
public final class VariableUtil {

    public static final String CHATROOM_NAME = "IM";
    public static final String SIGNINED_USER = "SIGNINED_USER";
    // 建立WebSocket连接时用
    public static final String SIGNINED_USER_WS_CODE = "SIGNINED_USER_WS_CODE";

    // 系统广播名称
    public static final String BROADCAST_MESSAGE_NAME = "BROADCAST_MESSAGE";

    // 用作随机生成字符串的变量
    public static final String BASE_CODE = "0123456789ABCDEFGHIJKMLNOPQRSTUVWXYZabcdefghijkmlnopqrstuvwxyz";
    // 验证码变量
    public static final String CHECK_CODE = "CHECKCODE_SERVER";

    // 响应状态码（成功、警告、失败）
    public static final Integer RESPONSE_SUCCESS_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.SUCCESS);
    public static final Integer RESPONSE_WARNING_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.WARNING);
    public static final Integer RESPONSE_FAILED_CODE = ResponseCodeEnum.getCode(ResponseCodeEnum.FAILED);

    // 管理员名称
    public static final String ADMIN_USER_NAME = RoleEnum.getRoleName(RoleEnum.ADMIN);
    // 普通用户名称
    public static final String COMMON_USER_NAME = RoleEnum.getRoleName(RoleEnum.USER);

    // 需要排除拦截或过滤的静态资源路径
    public static final String[] STATIC_RESOURCES_PATH = new String[] {"/css/**", "/js/**", "/images/**", "/favicon.ico"};
    // 需要排除拦截或过滤的请求资源路径
    public static final String[] EXCLUSIVE_REQUEST_PATH = new String[] {"/login", "/signin", "/user/login", "/user/signin",
            "/user/signup", "/entity/verify-code/*", "/user/active-user-account/*", "/user/logout", "/error"};
}
