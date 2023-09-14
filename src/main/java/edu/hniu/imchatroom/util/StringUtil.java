package edu.hniu.imchatroom.util;

import edu.hniu.imchatroom.interceptor.LoginInterceptor;
import java.util.UUID;

import static edu.hniu.imchatroom.util.VariableUtil.EXCLUSIVE_REQUEST_PATH;
import static edu.hniu.imchatroom.util.VariableUtil.STATIC_RESOURCES_PATH;

/**
 * 字符串工具类
 */
public final class StringUtil {

    /**
     * 根据长度产生随机code
     * @return
     */
    public static String getRandomCode(int size) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < size; i++) {
            // 产生 0~BASE_CODE.length - 1的一个随机下标索引值
            int index = (int) (Math.random() * VariableUtil.BASE_CODE.length());
            // 将指定下标的字符放入到StringBuffer中
            sb.append(VariableUtil.BASE_CODE.charAt(index));
        }

        return sb.toString();
    }

    /**
     * 使用UUID产生随机uuid唯一字符串
     * @return
     */
    public static String getRandomCode(boolean signed) {    // 返回是否有符号的随机字符串
        String str = UUID.randomUUID().toString();
        return signed ? str : str.replaceAll("-", "");
    }

    /**
     * 根据类型获取排除路径：包括 静态资源路径 和 请求路径
     * 将其用 "," 合并起来
     * @param clazz
     * @return
     */
    public static String getExclusivesPath(Class clazz) {
        String[] staticResourcePath = null;

        // 获取LoginInterceptor拦截器的排除路径
        if (LoginInterceptor.class.equals(clazz)) {
            staticResourcePath = STATIC_RESOURCES_PATH;

        } else {
            return null;
        }

        // 合并排除路径
//        System.out.println(Arrays.toString(mergeArrays(staticResourcePath, EXCLUSIVE_REQUEST_PATH)));
        final Object[] EXCLUSIVE_PATH = MoreUtil.mergeArrays(staticResourcePath, EXCLUSIVE_REQUEST_PATH);
        StringBuilder excludedUris = new StringBuilder(500);
        for (Object exclusivePath : EXCLUSIVE_PATH) {
            excludedUris.append(exclusivePath).append(", ");
        }

        return excludedUris.substring(0, excludedUris.length() - 2);
    }

    /**
     * 获取排除路径，以字符串数组格式返回
     * @return
     */
    public static String[] getExclusivesArrayPath(Class clazz) {
        String exclusivesPath = getExclusivesPath(clazz);
        return null != exclusivesPath ? exclusivesPath.split(", ") : null;
    }

    /**
     * 判断子串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return null == str || str.length() == 0;
    }

    /**
     * 判断子串是否不为空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
