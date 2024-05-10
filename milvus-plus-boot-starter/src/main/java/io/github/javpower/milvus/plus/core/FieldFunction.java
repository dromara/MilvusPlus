package io.github.javpower.milvus.plus.core;

import io.github.javpower.milvus.plus.annotation.MilvusField;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

@FunctionalInterface
public interface FieldFunction<T,R> extends Function<T,R>, Serializable {

    //默认配置
    String defaultSplit = "";
    Integer defaultToType = 0;

    default String getFieldName() {
        String methodName = getMethodName();
        if (methodName.startsWith("get")) {
            methodName = methodName.substring(3);
        }
        return changeFirstCharCase(methodName,false);
    }

    /**
     * 获取实体类的字段名称(实体声明的字段名称)
     */
    default String getFieldNameLine() {
        return getFieldName(this, defaultSplit);
    }

    /**
     * 获取实体类的字段名称
     */

    default String getFieldName(FieldFunction<T, ?> fn) {
        return getFieldName(fn, defaultSplit, defaultToType);
    }
    /**
     * 获取实体类的字段名称
     *
     * @param split 分隔符，多个字母自定义分隔符
     */
    default String getFieldName(FieldFunction<T, ?> fn, String split) {
        return getFieldName(fn, split, defaultToType);
    }

    /**
     * 获取实体类的字段名称
     *
     * @param split  分隔符，多个字母自定义分隔符
     * @param toType 转换方式，多个字母以大小写方式返回 0.不做转换 1.大写 2.小写
     */
    default String getFieldName(FieldFunction<T, ?> fn, String split, Integer toType) {
        SerializedLambda serializedLambda = getSerializedLambdaOne(fn);

        // 从lambda信息取出method、field、class等
        String fieldName = serializedLambda.getImplMethodName().substring("get".length());
        fieldName = fieldName.replaceFirst(fieldName.charAt(0) + "", (fieldName.charAt(0) + "").toLowerCase());
        Field field;
        try {
            field = Class.forName(serializedLambda.getImplClass().replace("/", ".")).getDeclaredField(fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        // 从field取出字段名
        MilvusField collectionField = field.getAnnotation(MilvusField.class);
        if (collectionField != null && StringUtils.isNotBlank(collectionField.name())) {
            return collectionField.name();
        }else {
            //0.不做转换 1.大写 2.小写
            switch (toType) {
                case 1:
                    return fieldName.replaceAll("[A-Z]", split + "$0").toUpperCase();
                case 2:
                    return fieldName.replaceAll("[A-Z]", split + "$0").toLowerCase();
                default:
                    return fieldName.replaceAll("[A-Z]", split + "$0");
            }

        }

    }


    default String getMethodName() {
        return getSerializedLambda().getImplMethodName();
    }

    default Class<?> getFieldClass() {
        return getReturnType();
    }

    default SerializedLambda getSerializedLambda() {
        Method method;
        try {
            method = getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        method.setAccessible(true);
        try {
            return (SerializedLambda) method.invoke(this);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    default SerializedLambda getSerializedLambdaOne(FieldFunction<T, ?> fn) {
        // 从function取出序列化方法
        Method writeReplaceMethod;
        try {
            writeReplaceMethod = fn.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // 从序列化方法取出序列化的lambda信息
        boolean isAccessible = writeReplaceMethod.isAccessible();
        writeReplaceMethod.setAccessible(true);
        SerializedLambda serializedLambda;
        try {
            serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(fn);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        writeReplaceMethod.setAccessible(isAccessible);
        return serializedLambda;
    }

    default Class<?> getReturnType() {
        SerializedLambda lambda = getSerializedLambda();
        Class<?> className;
        try {
            className = Class.forName(lambda.getImplClass().replace("/", "."));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Method method;
        try {
            method = className.getMethod(getMethodName());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return method.getReturnType();
    }
    default   String changeFirstCharCase(String str, boolean capitalize) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        char baseChar = str.charAt(0);
        char updatedChar;
        if (capitalize) {
            updatedChar = Character.toUpperCase(baseChar);
        } else {
            updatedChar = Character.toLowerCase(baseChar);
        }

        if (baseChar == updatedChar) {
            return str;
        } else {
            char[] chars = str.toCharArray();
            chars[0] = updatedChar;
            return new String(chars);
        }
    }

}