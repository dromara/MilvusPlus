package io.github.javpower.milvus.plus.core.conditions;

import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.FieldFunctionCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.converter.SearchRespConverter;
import io.github.javpower.milvus.plus.core.FieldFunction;
import io.github.javpower.milvus.plus.model.MilvusResp;
import io.github.javpower.milvus.plus.service.MilvusClient;
import io.github.javpower.milvus.plus.util.SpringUtils;
import io.milvus.exception.MilvusException;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author xgc
 **/
public class MilvusWrapper<T> {


    /**
     * 创建搜索构建器实例
     * @param collectionName 集合名称
     * @return 返回搜索构建器
     */
    public LambdaSearchWrapper<T> lambda() {
        // 获取实例化的类的类型参数T
        Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class<T> entityType = (Class<T>) type;
        // 从实体类上获取@MilvusCollection注解
        MilvusCollection collectionAnnotation = entityType.getAnnotation(MilvusCollection.class);
        if (collectionAnnotation == null) {
            throw new IllegalStateException("Entity type " + entityType.getName() + " is not annotated with @MilvusCollection.");
        }
        ConversionCache<?, ?> conversionCache = MilvusCache.milvusCache.get(entityType);
        String collectionName = conversionCache.getCollectionName();
        // 使用SpringUtil获取MilvusClient实例
        MilvusClient client = SpringUtils.getBean(MilvusClient.class);
        // 使用注解中的集合名称创建LambdaSearchWrapper实例
        return new LambdaSearchWrapper<>(collectionName, client,conversionCache,entityType);
    }

    /**
     * 搜索构建器内部类，用于构建搜索请求
     */
    @Data
    public static class LambdaSearchWrapper<T> {
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
            FieldFunctionCache<?, ?> fieldFunctionCache = conversionCache.getFieldFunctionCache();
            String fn = fieldFunctionCache.getFieldName(fieldFunction);
            PropertyCache propertyCache = conversionCache.getPropertyCache();
            String fieldName = propertyCache.functionToPropertyMap.get(fn);
            return fieldName;
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
         * @throws MilvusException 如果搜索执行失败
         */
        public MilvusResp<T> query() throws MilvusException {
            SearchReq searchReq = build();
            SearchResp search = client.client.search(searchReq);
            MilvusResp<T> tMilvusResp = SearchRespConverter.convertSearchRespToMilvusResp(search, entityType);
            return tMilvusResp;
        }
    }
}