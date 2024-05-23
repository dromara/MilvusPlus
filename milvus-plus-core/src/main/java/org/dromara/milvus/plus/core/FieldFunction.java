package org.dromara.milvus.plus.core;

import org.dromara.milvus.plus.annotation.MilvusField;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

@FunctionalInterface
public interface FieldFunction<T, R> extends Function<T, R>, Serializable {

    // 默认分隔符
    String DEFAULT_SPLIT = "";

    // 默认转换类型：0-不转换，1-大写，2-小写
    int DEFAULT_TO_TYPE = 0;


    /**
     * 获取实体类的字段名称（带分隔符和转换类型）
     * @param split  分隔符
     * @param toType 转换类型
     */
    default String getFieldName(FieldFunction<T, ?> fn, String split, int toType) {
        SerializedLambda lambda = getSerializedLambda(fn);
        String implClass = lambda.getImplClass().replace("/", ".");
        String implMethodName = lambda.getImplMethodName();
        ConversionCache conversionCache = MilvusCache.milvusCache.get(implClass);
        String fieldName = conversionCache != null ?
                conversionCache.getPropertyCache().methodToPropertyMap.get(implMethodName) :
                extractFieldName(implClass, implMethodName);
        if (StringUtils.isNotEmpty(fieldName)) {
            return fieldName;
        }
        return transformFieldName(fieldName, split, toType);
    }
    /**
     * 获取给定函数对象的序列化lambda表达式。
     *
     * @param function 要序列化的函数对象
     * @return 返回一个Optional对象，其中包含序列化后的lambda表达式
     */
    default SerializedLambda getSerializedLambda(FieldFunction<T, ?> function) {
        // 检查传入的函数对象是否为null
        return Optional.ofNullable(function)
                .map(this::extractSerializedLambda)
                .orElseThrow(() -> new RuntimeException("传入的函数对象为null或没有writeReplace方法"));
    }

    /**
     * 私有方法，用于提取函数对象的序列化lambda表达式。
     *
     * @param function 要序列化的函数对象
     * @return 序列化后的lambda表达式
     */
    default SerializedLambda extractSerializedLambda(FieldFunction<T, ?> function) {
        Method writeReplaceMethod;
        try {
            // 获取writeReplace方法，该方法是lambda表达式序列化的关键
            writeReplaceMethod = function.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            // 如果没有找到writeReplace方法，抛出运行时异常
            throw new RuntimeException("未找到writeReplace方法", e);
        }
        boolean isAccessible = writeReplaceMethod.isAccessible();
        try {
            // 设置writeReplace方法为可访问，以便可以调用它
            writeReplaceMethod.setAccessible(true);
            // 调用writeReplace方法并返回序列化后的lambda表达式
            return (SerializedLambda) writeReplaceMethod.invoke(function);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // 如果在访问或调用writeReplace方法时发生异常，抛出运行时异常
            throw new RuntimeException("调用writeReplace方法失败", e);
        } finally {
            // 恢复writeReplace方法的原始访问性，以避免潜在的安全问题
            writeReplaceMethod.setAccessible(isAccessible);
        }
    }


    /**
     * 将首字母大写或小写
     */
    default String capitalizeFirstLetter(String str, boolean capitalize) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        char firstChar = str.charAt(0);
        char newChar = capitalize ? Character.toUpperCase(firstChar) : Character.toLowerCase(firstChar);
        return newChar + str.substring(1);
    }

    /**
     * 提取字段名称
     */
    default String extractFieldName(String className, String methodName) {
        String fieldName = capitalizeFirstLetter(methodName.substring(3), false);
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            MilvusField annotation = field.getAnnotation(MilvusField.class);
            return annotation != null && StringUtils.isNotBlank(annotation.name()) ? annotation.name() : fieldName;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换字段名称
     */
    default String transformFieldName(String fieldName, String split, int toType) {
        switch (toType) {
            case 1:
                return fieldName.replaceAll("([A-Z])", split + "$1").toUpperCase();
            case 2:
                return fieldName.replaceAll("([A-Z])", split + "$1").toLowerCase();
            default:
                return fieldName.replaceAll("([A-Z])", split + "$1");
        }
    }



    /**
     * 获取实体类的字段名称
     */
    default String getFieldName(FieldFunction<T, ?> fn) {
        return getFieldName(fn, DEFAULT_SPLIT, DEFAULT_TO_TYPE);
    }

}