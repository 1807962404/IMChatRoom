package edu.hniu.imchatroom.util;

import edu.hniu.imchatroom.model.enums.StatusCodeEnum;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 更多工具类
 */
public class MoreUtil {

    /**
     * 合并数组
     * @param arrays
     * @return
     * @param <T>
     */
    public static <T> T[] mergeArrays(T[] ...arrays) {
        if (null == arrays)
            return null;

        // 记录总共传入了几个数组，以及所有数组内元素个数的总和
        int arraysCount = 0, elemLength = 0;
        for (int i = 0; i < arrays.length; i++) {
            arraysCount ++;
            for (int j = 0; j < arrays[i].length; j++) {
                elemLength ++;
            }
        }

//        System.out.println(arraysCount + ", " + elemLength);      // 打印传入的数组个数，以及所有数组内的元素总和
        // 创建合并数组
        T[] mergedArray = (T[]) new Object[elemLength];
        int mergedCount = 0;    // 记录复制的元素长度
        for (int i = 0; i < arraysCount; i++) {
            T[] srcArray = arrays[i];
            System.arraycopy(srcArray, 0, mergedArray, mergedCount, srcArray.length);
            mergedCount += srcArray.length;
//            System.out.println(Arrays.toString(mergedArray));
        }

//        System.out.println(mergedArray.getClass().getTypeName());     // java.lang.Object[]
        return mergedArray;
    }

    /**
     * 保存文件至指定目录下
     * @param srcData
     * @param destPath
     * @return
     */
    private boolean saveFileToDestPath(byte[] srcData, String destPath) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(destPath));
            bufferedOutputStream.write(srcData);    // 写入指定目录下
            return true;

        } catch (IOException e) {
            System.out.println("文件保存失败！{}" + e.getMessage());
            return false;

        } finally {
            if (null != bufferedOutputStream) {
                try {
                    bufferedOutputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 排除【隐藏】数据
     * @param item：排除非隐藏数据
     * @param isExcludeHiddenItem：排除隐藏数据
     * @return
     */
    public static List<Object> excludeItems(Object item, boolean isExcludeHiddenItem) {

        List<Object> results = new ArrayList<>();
        if (item instanceof List) {
            List<Object> tempItems = (List<Object>) item;
            Iterator<Object> iterator = tempItems.iterator();
            while (iterator.hasNext()) {
                Object nextItem = iterator.next();
                String displayStatus = resolveIsExcludedItem(nextItem);
                storeToResultItems(results, nextItem, displayStatus, isExcludeHiddenItem);
            }

        } else {
            String displayStatus = resolveIsExcludedItem(item);
            storeToResultItems(results, item, displayStatus, isExcludeHiddenItem);
        }

        return results;
    }

    /**
     * 解析数据
     * @param item
     * @return
     */
    private static String resolveIsExcludedItem(Object item) {
        Class<?> aClass = item.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        Iterator<Field> iterator = Arrays.stream(declaredFields).iterator();
        while (iterator.hasNext()) {
            Field nextFiled = iterator.next();
            nextFiled.setAccessible(true);

            if (nextFiled.getName().equals("displayStatus")) {
                try {
                    String displayStatus = String.valueOf(nextFiled.get(item));
                    return displayStatus;

                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    /**
     * 存储数据
     * @param results
     * @param item
     * @param displayStatus
     * @param isExcludeHiddenItem
     */
    private static void storeToResultItems(List<Object> results, Object item,
                                           String displayStatus, boolean isExcludeHiddenItem) {

        if (null != results && null != item) {
            if (isExcludeHiddenItem) {  // 排除隐藏数据
                if (displayStatus.equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.NORMAL)))
                    results.add(item);

            } else {
                if (displayStatus.equals(StatusCodeEnum.getStatusCode(StatusCodeEnum.ABNORMAL)))
                    results.add(item);
            }
        }
    }
}
