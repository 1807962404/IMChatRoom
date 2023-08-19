package edu.hniu.imchatroom.util;

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
}
