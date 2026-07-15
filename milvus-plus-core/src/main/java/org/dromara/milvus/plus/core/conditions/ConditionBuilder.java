package org.dromara.milvus.plus.core.conditions;

import org.dromara.milvus.plus.converter.SqlWhereTranslator;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.exception.MilvusPlusException;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 条件构建器抽象基类，用于构建 Milvus boolean expression。
 * <p>
 * 逻辑约定（对齐 MyBatis-Plus 心智）：
 * <ul>
 *   <li>同一 wrapper 上连续 {@code eq/ne/...}：彼此 AND</li>
 *   <li>{@code and(nested)}：{@code (当前条件组) && (nested 条件组)}，nested 内部默认 AND</li>
 *   <li>{@code or(nested)}：{@code (当前条件组) || (nested 条件组)}，nested 内部默认 AND</li>
 *   <li>{@code not()}：对当前全部条件取反</li>
 *   <li>{@code not(nested)}：追加 {@code not (nested 条件组)}</li>
 * </ul>
 */
public abstract class ConditionBuilder<T> {

    protected List<String> filters = new ArrayList<>();
    protected List<String> textMatches = new ArrayList<>();

    protected Map<String, Object> getPropertiesMap(T t) {
        Map<String, Object> propertiesMap = new HashMap<>();
        Class<?> clazz = t.getClass();
        // 含父类字段，避免实体继承场景丢属性
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(t);
                    if (value != null) {
                        propertiesMap.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field", e);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return propertiesMap;
    }


    /**
     * 添加 TEXT_MATCH 条件，使用 FieldFunction，支持多个值。
     *
     * @param fieldName 字段函数
     * @param values    要匹配的值列表
     * @return 当前条件构建器对象
     */
    protected ConditionBuilder<T> textMatch(String fieldName, List<String> values) {
        String joinedValues = String.join(" ", values);
        String match = "TEXT_MATCH(" + wrapFieldName(fieldName) + ", '" + escapeLiteral(joinedValues) + "')";
        textMatches.add(match);
        return this;
    }

    protected ConditionBuilder<T> textMatch(String fieldName, String value) {
        String match = "TEXT_MATCH(" + wrapFieldName(fieldName) + ", '" + escapeLiteral(value) + "')";
        textMatches.add(match);
        return this;
    }

    protected ConditionBuilder<T> textMatch(FieldFunction<T, ?> fieldName, String value) {
        textMatch(fieldName.getFieldName(fieldName), value);
        return this;
    }

    protected ConditionBuilder<T> textMatch(FieldFunction<T, ?> fieldName, List<String> values) {
        textMatch(fieldName.getFieldName(fieldName), values);
        return this;
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
        String filter = String.format("(%s >= %s && %s <= %s)",
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
        filters.add(wrapFieldName(fieldName) + " like '%" + escapeLike(value) + "%'");
        return this;
    }

    public ConditionBuilder<T> jsonContains(String fieldName, Object value) {
        filters.add("JSON_CONTAINS(" + wrapFieldName(fieldName) + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + wrapFieldName(fieldName) + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + wrapFieldName(fieldName) + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public ConditionBuilder<T> arrayContains(String fieldName, Object value) {
        filters.add("ARRAY_CONTAINS(" + wrapFieldName(fieldName) + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + wrapFieldName(fieldName) + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + wrapFieldName(fieldName) + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayLength(String fieldName, int length) {
        filters.add(wrapFieldName(fieldName) + ".length() == " + length);
        return this;
    }

    public ConditionBuilder<T> eq(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    public ConditionBuilder<T> ne(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    public ConditionBuilder<T> gt(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    public ConditionBuilder<T> ge(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    public ConditionBuilder<T> lt(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    public ConditionBuilder<T> le(FieldFunction<T, ?> fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    // Range operation
    public ConditionBuilder<T> between(FieldFunction<T, ?> fieldName, Object start, Object end) {
        String fn = getFieldName(fieldName);
        String filter = String.format("(%s >= %s && %s <= %s)", fn, convertValue(start), fn, convertValue(end));
        filters.add(filter);
        return this;
    }

    // Null check
    public ConditionBuilder<T> isNull(FieldFunction<T, ?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " == null");
        return this;
    }

    public ConditionBuilder<T> isNotNull(FieldFunction<T, ?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " != null");
        return this;
    }

    // In operator
    public ConditionBuilder<T> in(FieldFunction<T, ?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
        filters.add(fn + " in " + valueList);
        return this;
    }

    // Like operator
    public ConditionBuilder<T> like(FieldFunction<T, ?> fieldName, String value) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " like '%" + escapeLike(value) + "%'");
        return this;
    }

    protected ConditionBuilder<T> likeLeft(String fieldName, String value) {
        filters.add(wrapFieldName(fieldName) + " like '" + escapeLike(value) + "%'");
        return this;
    }

    protected ConditionBuilder<T> likeRight(String fieldName, String value) {
        filters.add(wrapFieldName(fieldName) + " like '%" + escapeLike(value) + "'");
        return this;
    }

    protected ConditionBuilder<T> likeLeft(FieldFunction<T, ?> fieldName, String value) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " like '" + escapeLike(value) + "%'");
        return this;
    }

    protected ConditionBuilder<T> likeRight(FieldFunction<T, ?> fieldName, String value) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " like '%" + escapeLike(value) + "'");
        return this;
    }

    // JSON array operations
    public ConditionBuilder<T> jsonContains(FieldFunction<T, ?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("JSON_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAll(FieldFunction<T, ?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> jsonContainsAny(FieldFunction<T, ?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public ConditionBuilder<T> arrayContains(FieldFunction<T, ?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("ARRAY_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAll(FieldFunction<T, ?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayContainsAny(FieldFunction<T, ?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    public ConditionBuilder<T> arrayLength(FieldFunction<T, ?> fieldName, int length) {
        String fn = getFieldName(fieldName);
        filters.add(fn + ".length() == " + length);
        return this;
    }

    /**
     * 直接追加原生 Milvus boolean expression（逃生舱 / 高级用户）。
     * <pre>
     *   .filter("status == 1 && array_contains(tags, \"a\")")
     * </pre>
     */
    protected ConditionBuilder<T> filter(String milvusExpr) {
        if (milvusExpr != null && !milvusExpr.trim().isEmpty()) {
            filters.add(milvusExpr.trim());
        }
        return this;
    }

    /**
     * 同 {@link #filter(String)}，语义别名。
     */
    protected ConditionBuilder<T> where(String milvusExpr) {
        return filter(milvusExpr);
    }

    /**
     * 类 SQL WHERE 子集 → Milvus 表达式（实验能力，非完整 MySQL）。
     * <pre>
     *   .sqlWhere("status = 1 AND name LIKE '%张%' OR type IN ('A','B')")
     * </pre>
     *
     * @see org.dromara.milvus.plus.converter.SqlWhereTranslator
     */
    protected ConditionBuilder<T> sqlWhere(String sqlWhere) {
        try {
            String expr = SqlWhereTranslator.toMilvusExpr(sqlWhere);
            return filter(expr);
        } catch (MilvusPlusException e) {
            throw e;
        } catch (Exception e) {
            throw MilvusPlusException.of("SQL_WHERE_TRANSLATE_FAILED",
                    "Failed to translate sqlWhere: " + sqlWhere + ", cause: " + e.getMessage());
        }
    }

    /**
     * AND 嵌套组：{@code (当前) && (nested)}。
     * nested 内部多条件默认 AND。
     */
    public ConditionBuilder<T> and(ConditionBuilder<T> other) {
        return mergeLogicalGroup(other, "&&");
    }

    /**
     * OR 嵌套组：{@code (当前) || (nested)}。
     * <p>
     * 注意：nested 内部多个条件是 AND，不是 OR。
     * 例如 {@code .eq(a,1).or(w.eq(b,2).eq(c,3))} => {@code (a == 1) || (b == 2 && c == 3)}
     */
    public ConditionBuilder<T> or(ConditionBuilder<T> other) {
        return mergeLogicalGroup(other, "||");
    }

    /**
     * 对当前全部条件取反：{@code not (current)}。
     */
    public ConditionBuilder<T> not() {
        String expr = buildFilterExpression();
        filters.clear();
        textMatches.clear();
        if (!isBlank(expr)) {
            filters.add("not (" + expr + ")");
        }
        return this;
    }

    /**
     * 追加对 nested 条件组的取反：{@code ... && not (nested)}。
     */
    public ConditionBuilder<T> not(ConditionBuilder<T> other) {
        if (other == null) {
            return this;
        }
        String expr = other.buildFilterExpression();
        if (!isBlank(expr)) {
            filters.add("not (" + expr + ")");
        }
        return this;
    }

    /**
     * 合并逻辑组。修复旧实现：
     * <ul>
     *   <li>旧 or：只把 nested 内部用 || 拼上，再与外层 AND —— 语义错误</li>
     *   <li>新 or：外层整体 OR nested 整体；nested 内部保持 AND</li>
     * </ul>
     */
    private ConditionBuilder<T> mergeLogicalGroup(ConditionBuilder<T> other, String op) {
        if (other == null) {
            return this;
        }
        String right = other.buildFilterExpression();
        if (isBlank(right)) {
            return this;
        }
        String left = buildFilterExpression();
        filters.clear();
        textMatches.clear();
        if (isBlank(left)) {
            // 左侧为空时，直接采用右侧（必要时加括号）
            filters.add(needParen(right) ? "(" + right + ")" : right);
            return this;
        }
        // 两侧都包括号，避免与后续条件拼接时优先级错乱
        filters.add("(" + left + ") " + op + " (" + right + ")");
        return this;
    }

    private boolean needParen(String expr) {
        return expr != null && (expr.contains("&&") || expr.contains("||") || expr.contains(" not "));
    }

    /**
     * 实现具体的过滤条件字符串构建逻辑（幂等，不修改内部状态）。
     *
     * @return 构建好的过滤条件字符串
     */
    protected String buildFilters() {
        return buildFilterExpression();
    }

    /**
     * 将当前 filters + textMatches 组装为表达式（不修改列表，可重复调用）。
     * <p>
     * 顶层条件用 AND 连接；若某一段内部含 OR，自动加括号，避免：
     * {@code (a) || (b) && c} 被解析成 {@code a || (b && c)}。
     * 期望 {@code ((a) || (b)) && c}。
     */
    protected String buildFilterExpression() {
        List<String> parts = new ArrayList<>(filters.size() + textMatches.size());
        if (!CollectionUtils.isEmpty(filters)) {
            parts.addAll(filters);
        }
        if (!CollectionUtils.isEmpty(textMatches)) {
            parts.addAll(textMatches);
        }
        if (parts.isEmpty()) {
            return "";
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return parts.stream()
                .map(this::parenIfContainsOr)
                .collect(Collectors.joining(" && "));
    }

    /**
     * 含 || 且未被整体括号包裹时，补括号，保证与后续 AND 组合时优先级正确。
     */
    private String parenIfContainsOr(String expr) {
        if (expr == null || expr.isEmpty()) {
            return expr;
        }
        if (!expr.contains("||")) {
            return expr;
        }
        String trimmed = expr.trim();
        if (trimmed.startsWith("(") && trimmed.endsWith(")") && isBalancedOuterParen(trimmed)) {
            return expr;
        }
        return "(" + expr + ")";
    }

    /**
     * 判断首尾括号是否匹配包裹整个表达式。
     */
    private boolean isBalancedOuterParen(String expr) {
        int depth = 0;
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0 && i < expr.length() - 1) {
                    return false;
                }
                if (depth < 0) {
                    return false;
                }
            }
        }
        return depth == 0;
    }

    private String getFieldName(FieldFunction<T, ?> fieldFunction) {
        return fieldFunction.getFieldName(fieldFunction);
    }

    // Helper methods
    protected String convertValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "'" + escapeLiteral((String) value) + "'";
        }
        if (value instanceof Character) {
            return "'" + escapeLiteral(String.valueOf(value)) + "'";
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        // 其他类型按字符串字面量处理，避免 toString 无引号导致表达式非法
        return "'" + escapeLiteral(String.valueOf(value)) + "'";
    }

    protected String convertValues(List<?> values) {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
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

    protected ConditionBuilder<T> addFilter(FieldFunction<T, ?> fieldName, String op, Object value) {
        filters.add(wrapFieldName(getFieldName(fieldName)) + " " + op + " " + convertValue(value));
        return this;
    }

    /**
     * 字符串字面量转义（单引号）。
     */
    protected String escapeLiteral(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    /**
     * like 模式转义：先转义字面量，再保留业务侧 %/_ 通配意图由调用方控制。
     * 这里只防注入式引号破坏。
     */
    protected String escapeLike(String value) {
        return escapeLiteral(value);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
