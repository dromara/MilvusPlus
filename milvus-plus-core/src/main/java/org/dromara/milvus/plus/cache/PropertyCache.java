package org.dromara.milvus.plus.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author xgc
 **/
public class PropertyCache {

    public Map<String, String> functionToPropertyMap = new HashMap<>(); //属性名称->集合属性名称

    public Map<String, Boolean> nullableToPropertyMap = new HashMap<>(); //属性名称->是否允许为空

    public Map<String, String> methodToPropertyMap = new HashMap<>(); //属性get方法名称->集合属性名称

    public Set<String> metaFunctionSet = new HashSet<>(); //动态字段属性名称

    public Map<String, String> metaMethodMap = new HashMap<>(); //属性get方法名称

    // 根据值查找第一个匹配的键
    public String findKeyByValue(String value) {
        for (Map.Entry<String, String> entry : functionToPropertyMap.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey(); // 返回与值匹配的第一个键
            }
        }
        return null; // 如果没有找到匹配的键，返回null
    }
}
