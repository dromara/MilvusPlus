package org.dromara.milvus.plus.core.conditions;

import org.dromara.milvus.plus.core.FieldFunction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件构建器抽象基类，用于构建条件。
 */
public abstract class ConditionBuilder<T> {

    protected List<String> filters = new ArrayList<>();
    protected Map<String, Object> getPropertiesMap(T t) {
        Map<String, Object> propertiesMap = new HashMap<>();
        Class<?> clazz = t.getClass();
        Field[] fields = clazz.getDeclaredFields(); // 获取所有属性
        for (Field field : fields) {
            try {
                field.setAccessible(true); // 确保私有属性也可以访问
                String fieldName = field.getName(); // 获取属性名
                Object value = field.get(t); // 获取属性值
                if(value!=null){
                    propertiesMap.put(fieldName, value); // 将属性名和属性值放入Map
                }
            } catch (IllegalAccessException e) {
                // 异常处理，实际开发中可以根据需要记录日志或进行其他处理
                throw new RuntimeException("Error accessing field", e);
            }
        }
        return propertiesMap; // 返回包含属性名和属性值的Map
    }

    /**
     * 添加等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> eq(String fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    /**
     * 添加不等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> ne(String fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    /**
     * 添加大于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> gt(String fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    /**
     * 添加大于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> ge(String fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    /**
     * 添加小于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> lt(String fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    /**
     * 添加小于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> le(String fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    /**
     * 添加范围条件。
     *
     * @param fieldName 字段名
     * @param start     范围开始值
     * @param end       范围结束值
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> between(String fieldName, Object start, Object end) {
        String filter = String.format("%s >= %s && %s <= %s",
                wrapFieldName(fieldName), convertValue(start), wrapFieldName(fieldName), convertValue(end));
        filters.add(filter);
        return this;
    }

    /**
     * 添加空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> isNull(String fieldName) {
        filters.add(wrapFieldName(fieldName) + " == null");
        return this;
    }

    /**
     * 添加非空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> isNotNull(String fieldName) {
        filters.add(wrapFieldName(fieldName) + " != null");
        return this;
    }

    /**
     * 添加IN条件。
     *
     * @param fieldName 字段名
     * @param values    要检查的值列表
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> in(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add(wrapFieldName(fieldName) + " in " + valueList);
        return this;
    }

    /**
     * 添加LIKE条件。
     *
     * @param fieldName 字段名
     * @param value     要匹配的模式
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> like(String fieldName, String value) {
        filters.add(wrapFieldName(fieldName) + " like '%" + value + "%'");
        return this;
    }

    public ConditionBuilder<T> jsonContains(String fieldName, Object value) {
        filters.add("JSON_CONTAINS(" + fieldName + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + fieldName + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public ConditionBuilder<T> arrayContains(String fieldName, Object value) {
        filters.add("ARRAY_CONTAINS(" + fieldName + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayLength(String fieldName, int length) {
        filters.add(fieldName + ".length() == " + length);
        return this;
    }

    public ConditionBuilder<T> eq(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    public ConditionBuilder<T> ne(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    public ConditionBuilder<T> gt(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    public ConditionBuilder<T> ge(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    public ConditionBuilder<T> lt(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    public ConditionBuilder<T> le(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    // Range operation
    public ConditionBuilder<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        String fn = getFieldName(fieldName);
        String filter = String.format("%s >= %s && %s <= %s", fn, convertValue(start), fn, convertValue(end));
        filters.add(filter);
        return this;
    }

    // Null check
    public ConditionBuilder<T> isNull(FieldFunction<T,?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " == null");
        return this;
    }

    public ConditionBuilder<T> isNotNull(FieldFunction<T,?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " != null");
        return this;
    }

    // In operator
    public ConditionBuilder<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
        filters.add(fn + " in " + valueList);
        return this;
    }

    // Like operator
    public ConditionBuilder<T> like(FieldFunction<T,?> fieldName, String value) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " like '%" + value + "%'");
        return this;
    }

    // JSON array operations
    public ConditionBuilder<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("JSON_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public ConditionBuilder<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("ARRAY_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }
    public ConditionBuilder<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        String fn = getFieldName(fieldName);
        filters.add(fn + ".length() == " + length);
        return this;
    }

    // Logic operations
    public ConditionBuilder<T> and(ConditionBuilder<T> other) {
        filters.add("(" + String.join(" && ", other.filters) + ")");
        return this;
    }

    public ConditionBuilder<T> or(ConditionBuilder<T> other) {
        filters.add("(" + String.join(" || ", other.filters) + ")");
        return this;
    }

    public ConditionBuilder<T> not() {
        filters.add("not (" + String.join(" && ", filters) + ")");
        return this;
    }


    /**
     * 实现具体的过滤条件字符串构建逻辑。
     * 需要子类重写此方法。
     *
     * @return 构建好的过滤条件字符串
     */
    protected String buildFilters(){
        return filters.stream().collect(Collectors.joining(" && "));
    }

    private String getFieldName(FieldFunction<T, ?> fieldFunction) {
        return fieldFunction.getFieldName(fieldFunction);
    }

    // Helper methods
    protected String convertValue(Object value) {
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "\\'") + "'";
        }
        return value.toString();
    }

    protected String convertValues(List<?> values) {
        return values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    protected String wrapFieldName(String fieldName) {
        return fieldName; // 根据实际情况，可能需要添加引号或其他包装
    }

    protected ConditionBuilder<T> addFilter(String fieldName, String op, Object value) {
        filters.add(wrapFieldName(fieldName) + " " + op + " " + convertValue(value));
        return this;
    }
    protected ConditionBuilder<T> addFilter(FieldFunction<T,?> fieldName, String op, Object value) {
        filters.add(wrapFieldName(getFieldName(fieldName)) + " " + op + " " + convertValue(value));
        return this;
    }
}