package org.dromara.milvus.plus.core.conditions;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.BaseRanker;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.converter.SearchRespConverter;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.util.GsonUtil;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索构建器内部类，用于构建搜索请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class LambdaQueryWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaQueryWrapper<T>, T> {
    private ConversionCache conversionCache;
    private List<String> outputFields;
    private Class<T> entityType;
    private String collectionName;
    private String collectionAlias;
    private List<String> partitionNames = new ArrayList<>();

    private String annsField;
    private int topK;
    private List<BaseVector> vectors = new ArrayList<>();
    private long offset;
    private long limit;
    private int roundDecimal = -1;
    private long guaranteeTimestamp;
    private ConsistencyLevel consistencyLevel;
    private boolean ignoreGrowing;
    private MilvusClientV2 client;
    private Map<String, Object> searchParams = new HashMap<>(16);

    private List<LambdaQueryWrapper<T>> hybridWrapper=new ArrayList<>();

    private BaseRanker ranker;

    public LambdaQueryWrapper() {

    }
    public LambdaQueryWrapper<T> hybrid(LambdaQueryWrapper<T> wrapper) {
        this.hybridWrapper.add(wrapper);
        return this;
    }

    /**
     * 添加集合别名
     *
     * @param collectionAlias 别名
     * @return this
     */
    public LambdaQueryWrapper<T> alias(String collectionAlias) {
        this.collectionAlias = collectionAlias;
        return this;
    }

    public LambdaQueryWrapper<T> partition(String... partitionName) {
        this.partitionNames.addAll(Arrays.asList(partitionName));
        return this;
    }
    public LambdaQueryWrapper<T>  consistencyLevel(ConsistencyLevel level){
        this.consistencyLevel=level;
        return this;
    }

    public LambdaQueryWrapper<T> partition(FieldFunction<T, ?>... partitionName) {
        Iterator<FieldFunction<T, ?>> iterator = new ArrayIterator<>(partitionName);
        while (iterator.hasNext()) {
            FieldFunction<T, ?> p = iterator.next();
            this.partitionNames.add(p.getFieldName(p));
        }
        return this;
    }

    public LambdaQueryWrapper<T> partition(Collection<FieldFunction<T, ?>> partitionName) {
        if (CollectionUtils.isEmpty(partitionName)) {
            throw new RuntimeException("partition collection is empty");
        }
        partitionName.forEach(f -> this.partitionNames.add(f.getFieldName(f)));
        return this;
    }

    public LambdaQueryWrapper<T> searchParams(Map<String, Object> searchParams) {
        this.searchParams.putAll(searchParams);
        return this;
    }
    public LambdaQueryWrapper<T> radius(Object radius){
        this.searchParams.put("radius",radius);
        return this;
    }
    public LambdaQueryWrapper<T> rangeFilter(Object rangeFilter){
        this.searchParams.put("range_filter",rangeFilter);
        return this;
    }
    public LambdaQueryWrapper<T> metricType(Object metric_type){
        this.searchParams.put("metric_type",metric_type);
        return this;
    }
    public LambdaQueryWrapper<T> roundDecimal(int roundDecimal){
        this.roundDecimal=roundDecimal;
        return this;
    }

    /**
     * 添加等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> eq(String fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> eq(boolean condition,String fieldName, Object value) {
        if(condition){
            super.eq(fieldName,value);
        }
        return this;
    }


    /**
     * 添加不等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> ne(String fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> ne(boolean condition,String fieldName, Object value) {
        if(condition){
            super.ne(fieldName,value);
        }
        return this;
    }

    /**
     * 添加大于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> gt(String fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> gt(boolean condition,String fieldName, Object value) {
        if(condition){
            super.gt(fieldName,value);
        }
        return this;
    }

    /**
     * 添加大于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> ge(String fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> ge(boolean condition,String fieldName, Object value) {
        if(condition){
            super.ge(fieldName,value);
        }
        return this;
    }

    /**
     * 添加小于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> lt(String fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> lt(boolean condition,String fieldName, Object value) {
        if(condition){
            super.lt(fieldName,value);
        }
        return this;
    }

    /**
     * 添加小于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> le(String fieldName, Object value) {
        super.le(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> le(boolean condition,String fieldName, Object value) {
        if(condition){
            super.le(fieldName,value);
        }
        return this;
    }

    /**
     * 添加范围条件。
     *
     * @param fieldName 字段名
     * @param start     范围开始值
     * @param end       范围结束值
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> between(String fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }
    public LambdaQueryWrapper<T> between(boolean condition,String fieldName, Object start, Object end) {
        if(condition){
            super.between(fieldName,start,end);
        }
        return this;
    }

    /**
     * 添加空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> isNull(String fieldName) {
        super.isNull(fieldName);
        return this;
    }

    /**
     * 添加非空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> isNotNull(String fieldName) {
        super.isNotNull(fieldName);
        return this;
    }

    /**
     * 添加IN条件。
     *
     * @param fieldName 字段名
     * @param values    要检查的值列表
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> in(String fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> in(boolean condition,String fieldName, List<?> values) {
        if(condition){
            super.in(fieldName,values);
        }
        return this;
    }

    /**
     * 添加LIKE条件。
     *
     * @param fieldName 字段名
     * @param value     要匹配的模式
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> like(String fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> like(boolean condition,String fieldName, String value) {
        if(condition){
            super.like(fieldName,value);
        }
        return this;
    }
    public LambdaQueryWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> like(boolean condition, FieldFunction<T, ?> fieldName, String value) {
        if (condition) {
            super.like(fieldName, value);
        }
        return this;
    }
    ////
    public LambdaQueryWrapper<T> likeLeft(String fieldName, String value) {
        super.likeLeft(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> likeLeft(boolean condition,String fieldName, String value) {
        if(condition){
            super.likeLeft(fieldName,value);
        }
        return this;
    }
    public LambdaQueryWrapper<T> likeLeft(FieldFunction<T,?> fieldName, String value) {
        super.likeLeft(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> likeLeft(boolean condition, FieldFunction<T, ?> fieldName, String value) {
        if (condition) {
            super.likeLeft(fieldName, value);
        }
        return this;
    }
    ////
    public LambdaQueryWrapper<T> likeRight(String fieldName, String value) {
        super.likeRight(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> likeRight(boolean condition,String fieldName, String value) {
        if(condition){
            super.likeRight(fieldName,value);
        }
        return this;
    }
    public LambdaQueryWrapper<T> likeRight(FieldFunction<T,?> fieldName, String value) {
        super.likeRight(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> likeRight(boolean condition, FieldFunction<T, ?> fieldName, String value) {
        if (condition) {
            super.likeRight(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> jsonContains(String fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> jsonContains(boolean condition,String fieldName, Object value) {
        if(condition){
            super.jsonContains(fieldName,value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> jsonContainsAll(boolean condition,String fieldName, List<?> values) {
        if(condition){
            super.jsonContainsAll(fieldName,values);
        }
        return this;
    }
    public LambdaQueryWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> jsonContainsAny(boolean condition,String fieldName, List<?> values) {
        if(condition){
            super.jsonContainsAny(fieldName,values);
        }
        return this;
    }

    // Array operations
    public LambdaQueryWrapper<T> arrayContains(String fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> arrayContains(boolean condition,String fieldName, Object value) {
        if(condition){
            super.arrayContains(fieldName,value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> arrayContainsAll(boolean condition,String fieldName, List<?> values) {
        if(condition){
            super.arrayContainsAll(fieldName,values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> arrayContainsAny(boolean condition,String fieldName, List<?> values) {
        if(condition){
            super.arrayContainsAny(fieldName,values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayLength(String fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }
    public LambdaQueryWrapper<T> arrayLength(boolean condition,String fieldName, int length) {
        if(condition){
            super.arrayLength(fieldName,length);
        }
        return this;
    }

    public LambdaQueryWrapper<T> eq(FieldFunction<T,?> fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> ne(FieldFunction<T,?> fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> gt(FieldFunction<T,?> fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> ge(FieldFunction<T,?> fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> lt(FieldFunction<T,?> fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> le(FieldFunction<T,?> fieldName, Object value) {
        super.le(fieldName,value);
        return this;
    }

    // Range operation
    public LambdaQueryWrapper<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    // Null check
    public LambdaQueryWrapper<T> isNull(FieldFunction<T,?> fieldName) {
        super.isNull(fieldName);
        return this;
    }

    public LambdaQueryWrapper<T> isNotNull(FieldFunction<T,?> fieldName) {
        super.isNotNull(fieldName);
        return this;
    }

    // In operator
    public LambdaQueryWrapper<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }


    // JSON array operations
    public LambdaQueryWrapper<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaQueryWrapper<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaQueryWrapper<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    public LambdaQueryWrapper<T> eq(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.eq(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> ne(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.ne(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> gt(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.gt(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> ge(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.ge(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> lt(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.lt(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> le(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.le(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> between(boolean condition, FieldFunction<T, ?> fieldName, Object start, Object end) {
        if (condition) {
            super.between(fieldName, start, end);
        }
        return this;
    }

    public LambdaQueryWrapper<T> in(boolean condition, FieldFunction<T, ?> fieldName, List<?> values) {
        if (condition) {
            super.in(fieldName, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> jsonContains(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.jsonContains(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAll(boolean condition, FieldFunction<T, ?> fieldName, List<?> values) {
        if (condition) {
            super.jsonContainsAll(fieldName, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAny(boolean condition, FieldFunction<T, ?> fieldName, List<?> values) {
        if (condition) {
            super.jsonContainsAny(fieldName, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayContains(boolean condition, FieldFunction<T, ?> fieldName, Object value) {
        if (condition) {
            super.arrayContains(fieldName, value);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAll(boolean condition, FieldFunction<T, ?> fieldName, List<?> values) {
        if (condition) {
            super.arrayContainsAll(fieldName, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAny(boolean condition, FieldFunction<T, ?> fieldName, List<?> values) {
        if (condition) {
            super.arrayContainsAny(fieldName, values);
        }
        return this;
    }

    public LambdaQueryWrapper<T> arrayLength(boolean condition, FieldFunction<T, ?> fieldName, int length) {
        if (condition) {
            super.arrayLength(fieldName, length);
        }
        return this;
    }

    // Logic operations
    public LambdaQueryWrapper<T> and(ConditionBuilder<T> other) {
        super.and(other);
        return this;
    }

    public LambdaQueryWrapper<T> or(ConditionBuilder<T> other) {
        super.or(other);
        return this;
    }

    public LambdaQueryWrapper<T> not() {
        super.not();
        return this;
    }

    public LambdaQueryWrapper<T> annsField(String annsField){
        this.annsField=annsField;
        return this;
    }
    public LambdaQueryWrapper<T> annsField(FieldFunction<T,?> annsField){
        this.annsField=annsField.getFieldName(annsField);
        return this;
    }
    public LambdaQueryWrapper<T> ranker(BaseRanker ranker){
        this.ranker=ranker;
        return this;
    }
    public LambdaQueryWrapper<T> vector(List<Float> vector) {
        BaseVector baseVector = new FloatVec(vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(String annsField,List<Float> vector) {
        this.annsField=annsField;
        BaseVector baseVector = new FloatVec(vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(FieldFunction<T,?> annsField, List<? extends Float> vector) {
        this.annsField=annsField.getFieldName(annsField);
        BaseVector baseVector = new FloatVec((List<Float>) vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> textVector(FieldFunction<T,?> annsField, String vector) {
        this.annsField=annsField.getFieldName(annsField)+"_sparse";
        BaseVector baseVector = new EmbeddedText(vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> textVector(String annsField,String vector) {
        this.annsField=annsField+"_sparse";
        BaseVector baseVector = new EmbeddedText(vector);
        vectors.add(baseVector);
        return this;
    }

    public LambdaQueryWrapper<T> vector(BaseVector vector) {
        vectors.add(vector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(String annsField,BaseVector vector) {
        this.annsField=annsField;
        vectors.add(vector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(FieldFunction<T,?> annsField, BaseVector vector) {
        this.annsField=annsField.getFieldName(annsField);
        vectors.add(vector);
        return this;
    }
    public LambdaQueryWrapper<T> limit(Long limit) {
        this.setLimit(limit);
        return this;
    }
    public LambdaQueryWrapper<T> offset(Long offset) {
        this.setOffset(offset);
        return this;
    }

    public LambdaQueryWrapper<T> topK(Integer topK) {
        this.setTopK(topK);
        return this;
    }

    /**
     * 添加 TEXT_MATCH 条件，使用 FieldFunction，支持多个值。
     *
     * @param fieldName 字段函数
     * @param values    要匹配的值列表
     * @return 当前条件构建器对象
     */
    public LambdaQueryWrapper<T> textMatch(String fieldName, List<String> values) {
        super.textMatch(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(String fieldName, String value) {
        super.textMatch(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(FieldFunction<T,?> fieldName, String value) {
        super.textMatch(fieldName,value);
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(FieldFunction<T,?> fieldName, List<String> values) {
        super.textMatch(fieldName,values);
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(boolean condition,String fieldName, List<String> values) {
        if(condition){
            super.textMatch(fieldName,values);
        }
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(boolean condition,String fieldName, String value) {
        if(condition){
            super.textMatch(fieldName,value);
        }
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(boolean condition,FieldFunction<T,?> fieldName, String value) {
        if(condition){
            super.textMatch(fieldName,value);
        }
        return this;
    }
    public LambdaQueryWrapper<T> textMatch(boolean condition,FieldFunction<T,?> fieldName, List<String> values) {
        if(condition){
            super.textMatch(fieldName,values);
        }
        return this;
    }

    /**
     * 构建完整的搜索请求
     * @return 搜索请求对象
     */
    private SearchReq buildSearch() {
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName);
        if (annsField != null && !annsField.isEmpty()) {
            builder.annsField(annsField);
        }
        if(consistencyLevel!=null){
            builder.consistencyLevel(consistencyLevel);
        }
        if (!vectors.isEmpty()) {
            builder.data(vectors);
        }
        String filterStr = buildFilters();
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        if (topK > 0) {
            builder.topK(topK);
        }
        if (limit > 0) {
            builder.limit(limit);
        }
        if(offset > 0){
            builder.offset(offset);
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionNames(partitionNames);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            builder.outputFields(outputFields);
        } else {
            Collection<String> values = conversionCache.getPropertyCache().functionToPropertyMap.values();
            builder.outputFields(new ArrayList<>(values));
        }
        if (!searchParams.isEmpty()) {
            builder.searchParams(searchParams);
        }
        if (roundDecimal != -1) {
            builder.roundDecimal(roundDecimal);
        }
        // Set other parameters as needed
        return builder.build();
    }

    private QueryReq buildQuery() {
        QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName);
        String filterStr = buildFilters();
        if (StringUtils.isNotBlank(filterStr)) {
            builder.filter(filterStr);
        }
        if (topK > 0) {
            builder.limit(topK);
        }
        if (limit > 0L) {
            builder.limit(limit);
        }
        if(offset > 0){
            builder.offset(offset);
        }
        if(consistencyLevel!=null){
            builder.consistencyLevel(consistencyLevel);
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionNames(partitionNames);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            builder.outputFields(outputFields);
        } else {
            Collection<String> values = conversionCache.getPropertyCache().functionToPropertyMap.values();
            builder.outputFields(new ArrayList<>(values));
        }
        return builder.build();
    }
    private HybridSearchReq buildHybrid(){
        //混合查询
        List<AnnSearchReq> searchRequests = hybridWrapper.stream().filter(v -> StringUtils.isNotEmpty(v.getAnnsField()) && !v.getVectors().isEmpty()).map(
                v -> {
                    AnnSearchReq.AnnSearchReqBuilder<?, ?> annBuilder = AnnSearchReq.builder()
                            .vectorFieldName(v.getAnnsField())
                            .vectors(v.getVectors());
                    if (v.getTopK() > 0) {
                        annBuilder.topK(v.getTopK());
                    }
                    String expr = v.buildFilters();
                    if (StringUtils.isNotEmpty(expr)) {
                        annBuilder.expr(expr);
                    }
                    Map<String, Object> params = v.searchParams;
                    if (!params.isEmpty()) {
                        annBuilder.params(GsonUtil.toJson(params));
                    }
                    return annBuilder.build();
                }
        ).collect(Collectors.toList());
        HybridSearchReq.HybridSearchReqBuilder<?, ?> reqBuilder = HybridSearchReq.builder()
                .collectionName(collectionName)
                .searchRequests(searchRequests);
        if(ranker!=null){
            reqBuilder.ranker(ranker);
        }
        if(topK>0){
            reqBuilder.topK(topK);
        }
        if(consistencyLevel!=null){
            reqBuilder.consistencyLevel(consistencyLevel);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            reqBuilder.outFields(outputFields);
        } else {
            Collection<String> values = conversionCache.getPropertyCache().functionToPropertyMap.values();
            reqBuilder.outFields(new ArrayList<>(values));
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            reqBuilder.partitionNames(partitionNames);
        }
        if (roundDecimal != -1) {
            reqBuilder.roundDecimal(roundDecimal);
        }
        HybridSearchReq hybridSearchReq= reqBuilder.build();
        return hybridSearchReq;
    }

    /**
     * 执行搜索
     *
     * @return 搜索响应对象
     */
    public MilvusResp<List<MilvusResult<T>>> query() throws MilvusException{
        return executeWithRetry(
                () -> {
                    if(hybridWrapper.size()>0){
                        HybridSearchReq hybridSearchReq = buildHybrid();
                        log.info("Build HybridSearch Param--> {}", GsonUtil.toJson(hybridSearchReq));
                        SearchResp searchResp = client.hybridSearch(hybridSearchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    }
                    if (!vectors.isEmpty()) {
                        SearchReq searchReq = buildSearch();
                        log.info("Build Search Param--> {}", GsonUtil.toJson(searchReq));
                        SearchResp searchResp = client.search(searchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    } else {
                        QueryReq queryReq = buildQuery();
                        log.info("Build Query param--> {}", GsonUtil.toJson(queryReq));
                        QueryResp queryResp = client.query(queryReq);
                        return SearchRespConverter.convertGetRespToMilvusResp(queryResp, entityType);
                    }
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }


    public MilvusResp<List<MilvusResult<T>>> query(FieldFunction<T, ?>... outputFields) throws MilvusException {
        List<String> otf = new ArrayList<>();
        for (FieldFunction<T, ?> outputField : outputFields) {
            otf.add(outputField.getFieldName(outputField));
        }
        this.outputFields = otf;
        return query();
    }

    public MilvusResp<Long> count() throws MilvusException {
        this.outputFields = new ArrayList<>();
        this.outputFields.add("count(*)");
        return executeWithRetry(
                () -> {
                        QueryReq queryReq = buildQuery();
                        log.info("Build Query param--> {}", GsonUtil.toJson(queryReq));
                        QueryResp queryResp = client.query(queryReq);
                        return SearchRespConverter.convertGetRespToCount(queryResp);
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );

    }

    public MilvusResp<List<MilvusResult<T>>> query(String... outputFields) throws MilvusException {
        this.outputFields = Arrays.stream(outputFields).collect(Collectors.toList());
        return query();
    }

    public MilvusResp<List<MilvusResult<T>>> getById(Serializable... ids) {
        GetReq.GetReqBuilder<?, ?> builder = GetReq.builder()
                .collectionName(collectionName)
                .ids(Arrays.asList(ids));
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionName(partitionNames.get(0));
        }
        GetReq getReq = builder.build();
        GetResp getResp = client.get(getReq);

        return SearchRespConverter.convertGetRespToMilvusResp(getResp, entityType);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        setClient(client);
        setCollectionName(collectionName);
        setEntityType(entityType);
        setConversionCache(conversionCache);
    }

    @Override
    public LambdaQueryWrapper<T> wrapper() {
        return this;
    }

}