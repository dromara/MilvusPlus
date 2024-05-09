package io.github.javpower.milvus.plus.cache;

import io.github.javpower.milvus.plus.core.FieldFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
/**
 * @author xgc
 **/
public class FieldFunctionCache<T,R> {

    public Map<String, FieldFunction<T, R>> propertyToFunctionMap = new HashMap<>();//属性名称获取对应的函数

    public String getMethodName(Field field,String fieldName){
        String capitalizedFieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        String getMethodName = "get" + capitalizedFieldName; // 构建get方法名称

        // 检查字段类型是否为boolean，如果是，get方法名称应以is开头
        if (field.getType() == boolean.class) {
            getMethodName = "is" + capitalizedFieldName;
        }
        return getMethodName;
    }
    public String getFieldName(FieldFunction fieldFunction) {
        for (Map.Entry<String, FieldFunction<T, R>> entry : propertyToFunctionMap.entrySet()) {
            if (entry.getValue().equals(fieldFunction)) {
                return entry.getKey(); // 返回匹配的属性名称
            }
        }
        return null; // 如果没有找到匹配项，返回null
    }
    public FieldFunction<Object, Object> createFunction(Method method) {
        return (instance) -> {
            try {
                return method.invoke(instance);
            } catch (Exception e) {
                // 异常处理
                return null;
            }
        };
    }
}
