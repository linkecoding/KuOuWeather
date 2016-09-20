package com.codekong.kuouweather.util;

import java.lang.reflect.Field;

/**
 * Created by 尚振鸿 on 2016-09-20.
 * 一些常用的方法封转类
 */
public class ClassUtil {
    /**
     * 通过反射获取动态字符串对应的资源id
     * @param variableName
     * @param c
     * @return
     */
    public static int getResId(String variableName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(variableName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
