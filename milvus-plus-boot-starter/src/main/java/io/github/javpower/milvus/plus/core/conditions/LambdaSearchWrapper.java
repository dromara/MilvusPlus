package io.github.javpower.milvus.plus.core.conditions;

import com.alibaba.fastjson.JSON;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.converter.SearchRespConverter;
import io.github.javpower.milvus.plus.core.FieldFunction;
import io.github.javpower.milvus.plus.model.MilvusResp;
import io.github.javpower.milvus.plus.service.MilvusClient;
import io.milvus.exception.MilvusException;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
     * 搜索构建器内部类，用于构建搜索请求
     */
@Data
@Slf4j
public  class LambdaSearchWrapper<T> {
    private ConversionCache<?, ?> conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String annsField;
    private int topK;
    private List<String> filters = new ArrayList<>();
    private List<List<Float>> vectors = new ArrayList<>();
    private long offset;
    private long limit;
    private int roundDecimal;
    private String searchParams;
    private long guaranteeTimestamp;
    private ConsistencyLevel consistencyLevel;
    private boolean ignoreGrowing;
    private MilvusClient client;

    public LambdaSearchWrapper(String collectionName, MilvusClient client,ConversionCache<?, ?> conversionCache,Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache=conversionCache;
        this.entityType=entityType;
    }

    public LambdaSearchWrapper() {

    }

    // addVector
    public LambdaSearchWrapper<T> addVector(List<Float> vector) {
        vectors.add(vector);
        return this;
    }
    public LambdaSearchWrapper<T> vector(List<Float> vector) {
        vectors.add(vector);
        return this;
    }

    // Common comparison operations
    public LambdaSearchWrapper<T> eq(String fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    public LambdaSearchWrapper<T> ne(String fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    public LambdaSearchWrapper<T> gt(String fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    public LambdaSearchWrapper<T> ge(String fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    public LambdaSearchWrapper<T> lt(String fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    public LambdaSearchWrapper<T> le(String fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    // Range operation
    public LambdaSearchWrapper<T> between(String fieldName, Object start, Object end) {
        String filter = String.format("%s >= %s && %s <= %s", fieldName, convertValue(start), fieldName, convertValue(end));
        filters.add(filter);
        return this;
    }

    // Null check
    public LambdaSearchWrapper<T> isNull(String fieldName) {
        filters.add(fieldName + " == null");
        return this;
    }

    public LambdaSearchWrapper<T> isNotNull(String fieldName) {
        filters.add(fieldName + " != null");
        return this;
    }

    // In operator
    public LambdaSearchWrapper<T> in(String fieldName, List<?> values) {
        String valueList = values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
        filters.add(fieldName + " in " + valueList);
        return this;
    }

    // Like operator
    public LambdaSearchWrapper<T> like(String fieldName, String value) {
        filters.add(fieldName + " like '%" + value + "%'");
        return this;
    }

    // JSON array operations
    public LambdaSearchWrapper<T> jsonContains(String fieldName, Object value) {
        filters.add("JSON_CONTAINS(" + fieldName + ", " + convertValue(value) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + fieldName + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public LambdaSearchWrapper<T> arrayContains(String fieldName, Object value) {
        filters.add("ARRAY_CONTAINS(" + fieldName + ", " + convertValue(value) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + fieldName + ", " + valueList + ")");
        return this;
    }

    public LambdaSearchWrapper<T> arrayLength(String fieldName, int length) {
        filters.add(fieldName + ".length() == " + length);
        return this;
    }



    public LambdaSearchWrapper<T> eq(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "==", value);
    }

    public LambdaSearchWrapper<T> ne(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "!=", value);
    }

    public LambdaSearchWrapper<T> gt(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, ">", value);
    }

    public LambdaSearchWrapper<T> ge(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, ">=", value);
    }

    public LambdaSearchWrapper<T> lt(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "<", value);
    }

    public LambdaSearchWrapper<T> le(FieldFunction<T,?> fieldName, Object value) {
        return addFilter(fieldName, "<=", value);
    }

    // Range operation
    public LambdaSearchWrapper<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        String fn = getFieldName(fieldName);
        String filter = String.format("%s >= %s && %s <= %s", fn, convertValue(start), fn, convertValue(end));
        filters.add(filter);
        return this;
    }

    // Null check
    public LambdaSearchWrapper<T> isNull(FieldFunction<T,?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " == null");
        return this;
    }

    public LambdaSearchWrapper<T> isNotNull(FieldFunction<T,?> fieldName) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " != null");
        return this;
    }

    // In operator
    public LambdaSearchWrapper<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
        filters.add(fn + " in " + valueList);
        return this;
    }

    // Like operator
    public LambdaSearchWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        String fn = getFieldName(fieldName);
        filters.add(fn + " like '%" + value + "%'");
        return this;
    }

    // JSON array operations
    public LambdaSearchWrapper<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("JSON_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("JSON_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    // Array operations
    public LambdaSearchWrapper<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        String fn = getFieldName(fieldName);
        filters.add("ARRAY_CONTAINS(" + fn + ", " + convertValue(value) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ALL(" + fn + ", " + valueList + ")");
        return this;
    }
    public LambdaSearchWrapper<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        String fn = getFieldName(fieldName);
        String valueList = convertValues(values);
        filters.add("ARRAY_CONTAINS_ANY(" + fn + ", " + valueList + ")");
        return this;
    }

    public LambdaSearchWrapper<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        String fn = getFieldName(fieldName);
        filters.add(fn + ".length() == " + length);
        return this;
    }

    // Logic operations
    public LambdaSearchWrapper<T> and(LambdaSearchWrapper<T> other) {
        filters.add("(" + String.join(" && ", other.filters) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> or(LambdaSearchWrapper<T> other) {
        filters.add("(" + String.join(" || ", other.filters) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> not() {
        filters.add("not (" + String.join(" && ", filters) + ")");
        return this;
    }

    public LambdaSearchWrapper<T> limit(Long limit) {
        this.setLimit(limit);
        return this;
    }
    public LambdaSearchWrapper<T> topK(Integer topK) {
        this.setTopK(topK);
        return this;
    }


    // Helper methods
    private String convertValue(Object value) {
        if (value instanceof String) {
            return "'" + value.toString().replace("'", "\\'") + "'";
        }
        return value.toString();
    }

    private String convertValues(List<?> values) {
        return values.stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private LambdaSearchWrapper<T> addFilter(String fieldName, String op, Object value) {
        filters.add(fieldName + " " + op + " " + convertValue(value));
        return this;
    }
    private LambdaSearchWrapper<T> addFilter(FieldFunction<T, ?> fieldFunction, String op, Object value) {
        String fieldName = getFieldName(fieldFunction);
        filters.add(fieldName + " " + op + " " + convertValue(value));
        return this;
    }
    private String getFieldName(FieldFunction<T, ?> fieldFunction) {
        return fieldFunction.getFieldName(fieldFunction);
    }

    /**
     * 构建完整的搜索请求
     * @return 搜索请求对象
     */
    private SearchReq build() {
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(collectionName)
                .annsField(annsField)
                .topK(topK);
        if (!vectors.isEmpty()) {
            builder.data(vectors);
        }
        String filterStr = filters.stream().collect(Collectors.joining(" && "));
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        // Set other parameters as needed
        return builder.build();
    }

    /**
     * 执行搜索
     * @return 搜索响应对象
     */
    public MilvusResp<T> query() throws MilvusException {
        SearchReq searchReq = build();
        log.info("build query param-->{}", JSON.toJSONString(searchReq));
        SearchResp search = client.client.search(searchReq);
        MilvusResp<T> tMilvusResp = SearchRespConverter.convertSearchRespToMilvusResp(search, entityType);
        return tMilvusResp;
    }
}