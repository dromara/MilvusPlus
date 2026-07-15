package org.dromara.milvus.plus.core.conditions;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
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
import org.dromara.milvus.plus.model.vo.PageResult;
import org.dromara.milvus.plus.util.LogSanitizeUtil;
import org.dromara.milvus.plus.exception.MilvusPlusException;
import org.dromara.milvus.plus.util.GsonUtil;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
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
    private Boolean ignoreGrowing;
    private MilvusClientV2 client;
    private Map<String, Object> searchParams = new HashMap<>(16);

    private List<LambdaQueryWrapper<T>> hybridWrapper=new ArrayList<>();

    /**
     * Hybrid search ranker. SDK 2.6+/3.x uses CreateCollectionReq.Function subclasses
     * (e.g. RRFRanker, WeightedRanker) instead of the removed BaseRanker type.
     */
    private CreateCollectionReq.Function ranker;

    private IndexParam.MetricType metricType;

    private long gracefulTime;
    private String groupByFieldName;

    private Integer groupSize;
    private Boolean strictGroupSize;

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
        if (searchParams != null) {
            this.searchParams.putAll(searchParams);
            // metric_type 应走 SearchReq.metricType，而不是塞进 searchParams
            Object mt = this.searchParams.remove("metric_type");
            if (mt != null && this.metricType == null) {
                this.metricType = parseMetricType(mt);
            }
        }
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
        this.metricType = parseMetricType(metric_type);
        if (this.metricType == null && metric_type != null) {
            // 无法解析时保留旧行为，兼容自定义字符串参数
            this.searchParams.put("metric_type", metric_type);
        } else {
            this.searchParams.remove("metric_type");
        }
        return this;
    }

    public LambdaQueryWrapper<T> metricType(IndexParam.MetricType metricType) {
        this.metricType = metricType;
        this.searchParams.remove("metric_type");
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

    /**
     * Nested AND: (current) && (nested). Preferred style.
     */
    public LambdaQueryWrapper<T> and(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>();
        if (consumer != null) {
            consumer.accept(nested);
        }
        return and(nested);
    }

    public LambdaQueryWrapper<T> or(ConditionBuilder<T> other) {
        super.or(other);
        return this;
    }

    /**
     * Nested OR: (current group) || (nested group). Nested multi-conditions default to AND.
     * Example: .eq(a,1).or(w -> w.eq(b,2).eq(c,3)) => (a == 1) || (b == 2 && c == 3)
     */
    public LambdaQueryWrapper<T> or(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>();
        if (consumer != null) {
            consumer.accept(nested);
        }
        return or(nested);
    }

    public LambdaQueryWrapper<T> not() {
        super.not();
        return this;
    }

    public LambdaQueryWrapper<T> not(ConditionBuilder<T> other) {
        super.not(other);
        return this;
    }

    /**
     * Nested NOT: append not (nested).
     */
    public LambdaQueryWrapper<T> not(Consumer<LambdaQueryWrapper<T>> consumer) {
        LambdaQueryWrapper<T> nested = new LambdaQueryWrapper<>();
        if (consumer != null) {
            consumer.accept(nested);
        }
        return not(nested);
    }

    /**
     * 原生 Milvus filter 表达式。
     */
    public LambdaQueryWrapper<T> filter(String milvusExpr) {
        super.filter(milvusExpr);
        return this;
    }

    /**
     * 同 filter。
     */
    public LambdaQueryWrapper<T> where(String milvusExpr) {
        super.where(milvusExpr);
        return this;
    }

    /**
     * 类 SQL WHERE 子集（实验）。例：status = 1 AND name LIKE '%x%'
     */
    public LambdaQueryWrapper<T> sqlWhere(String sqlWhere) {
        super.sqlWhere(sqlWhere);
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
    public LambdaQueryWrapper<T> ranker(CreateCollectionReq.Function ranker){
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
        if (limit != null) {
            this.setLimit(limit);
            // SDK 已弃用 topK，limit 与 topK 语义对齐：同时设置便于旧调用方兼容
            if (limit > 0 && limit <= Integer.MAX_VALUE) {
                this.setTopK(limit.intValue());
            }
        }
        return this;
    }
    public LambdaQueryWrapper<T> offset(Long offset) {
        this.setOffset(offset);
        return this;
    }

    public LambdaQueryWrapper<T> topK(Integer topK) {
        if (topK != null) {
            this.setTopK(topK);
            if (topK > 0) {
                this.setLimit(topK.longValue());
            }
        }
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
     * @param fieldName 按指定字段对搜索结果进行分组
     * @return
     */
    public LambdaQueryWrapper<T> groupByFieldName(String fieldName) {
        this.groupByFieldName=fieldName;
        return this;
    }
    public LambdaQueryWrapper<T> groupByFieldName(FieldFunction<T,?> fieldName) {
        this.groupByFieldName=fieldName.getFieldName(fieldName);
        return this;
    }

    // 设置保证时间戳
    public LambdaQueryWrapper<T> guaranteeTimestamp(long guaranteeTimestamp) {
        this.guaranteeTimestamp = guaranteeTimestamp;
        return this;
    }

    // 设置优雅的时间（毫秒）
    public LambdaQueryWrapper<T> gracefulTime(long gracefulTime) {
        this.gracefulTime=gracefulTime;
        return this;
    }

    // 设置是否忽略增长的段
    public LambdaQueryWrapper<T> ignoreGrowing(boolean ignoreGrowing) {
        this.ignoreGrowing = ignoreGrowing;
        return this;
    }

    // 设置分组搜索中每组内返回的实体目标数量
    public LambdaQueryWrapper<T> groupSize(Integer groupSize) {
        this.groupSize=groupSize;
        return this;
    }

    // 设置是否严格执行groupSize
    public LambdaQueryWrapper<T> strictGroupSize(Boolean strictGroupSize) {
        this.strictGroupSize=strictGroupSize;
        return this;
    }


    /**
     * 场景化向量检索：指定向量字段与向量值。
     */
    public LambdaQueryWrapper<T> vectorSearch(FieldFunction<T, ?> annsField, List<Float> vector) {
        return vector(annsField, vector);
    }

    public LambdaQueryWrapper<T> vectorSearch(String annsField, List<Float> vector) {
        return vector(annsField, vector);
    }

    public LambdaQueryWrapper<T> vectorSearch(FieldFunction<T, ?> annsField, BaseVector vector) {
        return vector(annsField, vector);
    }

    /**
     * 场景化文本检索（BM25/稀疏向量）：隐藏 _sparse 实现细节。
     */
    public LambdaQueryWrapper<T> textSearch(FieldFunction<T, ?> textField, String text) {
        return textVector(textField, text);
    }

    public LambdaQueryWrapper<T> textSearch(String textField, String text) {
        return textVector(textField, text);
    }

    /**
     * 范围检索：radius + rangeFilter。
     */
    public LambdaQueryWrapper<T> range(Object rangeFilter, Object radius) {
        rangeFilter(rangeFilter);
        radius(radius);
        return this;
    }

    /**
     * 返回条数语义方法（等价 limit/topK）。
     */
    public LambdaQueryWrapper<T> top(int n) {
        return topK(n);
    }

    /**
     * 专家参数收口。
     */
    public LambdaQueryWrapper<T> options(SearchOptions options) {
        if (options == null) {
            return this;
        }
        if (options.getConsistencyLevel() != null) {
            consistencyLevel(options.getConsistencyLevel());
        }
        if (options.getMetricType() != null) {
            metricType(options.getMetricType());
        }
        if (options.getIgnoreGrowing() != null) {
            ignoreGrowing(options.getIgnoreGrowing());
        }
        if (options.getRoundDecimal() != null) {
            roundDecimal(options.getRoundDecimal());
        }
        if (options.getGroupByFieldName() != null) {
            groupByFieldName(options.getGroupByFieldName());
        }
        if (options.getGroupSize() != null) {
            groupSize(options.getGroupSize());
        }
        if (options.getStrictGroupSize() != null) {
            strictGroupSize(options.getStrictGroupSize());
        }
        if (options.getGuaranteeTimestamp() != null) {
            guaranteeTimestamp(options.getGuaranteeTimestamp());
        }
        if (options.getGracefulTime() != null) {
            gracefulTime(options.getGracefulTime());
        }
        if (options.getSearchParams() != null && !options.getSearchParams().isEmpty()) {
            searchParams(options.getSearchParams());
        }
        return this;
    }

    public LambdaQueryWrapper<T> options(java.util.function.Consumer<SearchOptions> customizer) {
        return options(SearchOptions.of(customizer));
    }


    /**
     * 标量分页。注意：向量 ANN 检索的 offset 分页与数据库分页语义不同。
     *
     * @param pageNum  从 1 开始
     * @param pageSize 每页大小
     */
    public MilvusResp<PageResult<T>> page(long pageNum, long pageSize) {
        if (pageNum < 1) {
            throw MilvusPlusException.of("INVALID_PAGE", "pageNum must be >= 1");
        }
        if (pageSize < 1) {
            throw MilvusPlusException.of("INVALID_PAGE", "pageSize must be >= 1");
        }
        if (!vectors.isEmpty() || (hybridWrapper != null && !hybridWrapper.isEmpty())) {
            throw MilvusPlusException.of("PAGE_NOT_FOR_VECTOR_SEARCH",
                    "page() is for scalar query only. For vector search use topK/limit + offset carefully.");
        }
        // total
        List<String> originalOutput = this.outputFields;
        long offsetVal = (pageNum - 1) * pageSize;
        this.offset = 0;
        this.limit = 0;
        this.topK = 0;
        MilvusResp<Long> countResp = count();
        long total = countResp.getData() == null ? 0L : countResp.getData();

        // page data
        this.outputFields = originalOutput;
        this.offset = offsetVal;
        this.limit = pageSize;
        this.topK = 0;
        MilvusResp<List<MilvusResult<T>>> dataResp = query();
        PageResult<T> page = PageResult.of(pageNum, pageSize, total, dataResp.getData());
        MilvusResp<PageResult<T>> resp = new MilvusResp<>();
        resp.setSuccess(true);
        resp.setData(page);
        return resp;
    }

    /**
     * 构建完整的搜索请求
     * @return 搜索请求对象
     */
    private SearchReq buildSearch() {
        SearchReq.SearchReqBuilder builder = SearchReq.builder()
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
        // 优先使用 limit；topK 在 SDK 3.x 中已弃用但仍兼容
        long effectiveLimit = resolveLimit();
        if (effectiveLimit > 0) {
            builder.limit(effectiveLimit);
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
        if (metricType != null) {
            builder.metricType(metricType);
        }
        if (!searchParams.isEmpty()) {
            builder.searchParams(new HashMap<>(searchParams));
        }
        if (roundDecimal != -1) {
            builder.roundDecimal(roundDecimal);
        }
        if(guaranteeTimestamp>0L){
            builder.guaranteeTimestamp(guaranteeTimestamp);
        }
        if(gracefulTime>0L){
            builder.gracefulTime(gracefulTime);
        }
        if(ignoreGrowing!=null){
            builder.ignoreGrowing(ignoreGrowing);
        }
        if(groupByFieldName!=null&&!groupByFieldName.isEmpty()){
            builder.groupByFieldName(groupByFieldName);
        }
        if(groupSize!=null&&groupSize>0){
            builder.groupSize(groupSize);
        }
        if(strictGroupSize!=null){
            builder.strictGroupSize(strictGroupSize);
        }
        if (ranker != null) {
            builder.ranker(ranker);
        }
        return builder.build();
    }

    private long resolveLimit() {
        if (limit > 0) {
            return limit;
        }
        if (topK > 0) {
            return topK;
        }
        return 0;
    }

    private IndexParam.MetricType parseMetricType(Object metricTypeValue) {
        if (metricTypeValue == null) {
            return null;
        }
        if (metricTypeValue instanceof IndexParam.MetricType) {
            return (IndexParam.MetricType) metricTypeValue;
        }
        try {
            return IndexParam.MetricType.valueOf(String.valueOf(metricTypeValue).trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            log.warn("Unknown metric type: {}", metricTypeValue);
            return null;
        }
    }

    private QueryReq buildQuery() {
        QueryReq.QueryReqBuilder builder = QueryReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName);
        String filterStr = buildFilters();
        if (StringUtils.isNotBlank(filterStr)) {
            builder.filter(filterStr);
        }
        long effectiveLimit = resolveLimit();
        // count(*) 不能带 pagination（limit/offset），否则服务端报错
        boolean countOnly = outputFields != null && outputFields.size() == 1 && "count(*)".equals(outputFields.get(0));
        if (countOnly) {
            // 不设置 limit/offset
        } else {
            // Milvus：无 filter 时必须带 limit
            if (effectiveLimit <= 0L && StringUtils.isBlank(filterStr)) {
                effectiveLimit = 16384L;
            }
            if (effectiveLimit > 0L) {
                builder.limit(effectiveLimit);
            }
            if (offset > 0) {
                builder.offset(offset);
            }
        }
        if(consistencyLevel!=null){
            builder.consistencyLevel(consistencyLevel);
        }
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionNames(partitionNames);
        }
        if (outputFields != null && !outputFields.isEmpty()) {
            builder.outputFields(outputFields);
        } else if (conversionCache != null && conversionCache.getPropertyCache() != null) {
            Collection<String> values = conversionCache.getPropertyCache().functionToPropertyMap.values();
            builder.outputFields(new ArrayList<>(values));
        }
        return builder.build();
    }
    private HybridSearchReq buildHybrid(){
        //混合查询
        List<AnnSearchReq> searchRequests = hybridWrapper.stream().filter(v -> StringUtils.isNotEmpty(v.getAnnsField()) && !v.getVectors().isEmpty()).map(
                v -> {
                    AnnSearchReq.AnnSearchReqBuilder annBuilder = AnnSearchReq.builder()
                            .vectorFieldName(v.getAnnsField())
                            .vectors(v.getVectors());
                    long subLimit = v.resolveLimit();
                    if (subLimit > 0) {
                        annBuilder.limit(subLimit);
                    }
                    String filter = v.buildFilters();
                    if (StringUtils.isNotEmpty(filter)) {
                        // expr 已弃用，优先使用 filter
                        annBuilder.filter(filter);
                    }
                    if (v.metricType != null) {
                        annBuilder.metricType(v.metricType);
                    }
                    Map<String, Object> params = v.searchParams;
                    if (params != null && !params.isEmpty()) {
                        annBuilder.params(GsonUtil.toJson(params));
                    }
                    return annBuilder.build();
                }
        ).collect(Collectors.toList());
        HybridSearchReq.HybridSearchReqBuilder reqBuilder = HybridSearchReq.builder()
                .collectionName(collectionName)
                .searchRequests(searchRequests);
        if(ranker!=null){
            reqBuilder.ranker(ranker);
        }
        long effectiveLimit = resolveLimit();
        if (effectiveLimit > 0) {
            reqBuilder.limit(effectiveLimit);
        }
        if (offset > 0) {
            reqBuilder.offset(offset);
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
        return reqBuilder.build();
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
                        log.info("Build HybridSearch Param--> {}", LogSanitizeUtil.truncate(hybridSearchReq));
                        SearchResp searchResp = client.hybridSearch(hybridSearchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    }
                    if (!vectors.isEmpty()) {
                        SearchReq searchReq = buildSearch();
                        log.info("Build Search Param--> {}", LogSanitizeUtil.truncate(searchReq));
                        SearchResp searchResp = client.search(searchReq);
                        return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
                    } else {
                        QueryReq queryReq = buildQuery();
                        log.info("Build Query param--> {}", LogSanitizeUtil.truncate(queryReq));
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
                        log.info("Build Query param--> {}", LogSanitizeUtil.truncate(queryReq));
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
        if (ids == null || ids.length == 0) {
            throw MilvusPlusException.of("IDS_EMPTY", "ids must not be empty");
        }
        GetReq.GetReqBuilder builder = GetReq.builder()
                .collectionName(StringUtils.isNotBlank(collectionAlias) ? collectionAlias : collectionName)
                .ids(Arrays.asList(ids));
        if (!CollectionUtils.isEmpty(partitionNames)) {
            builder.partitionName(partitionNames.get(0));
        }
        GetReq getReq = builder.build();
        GetResp getResp = client.get(getReq);

        return SearchRespConverter.convertGetRespToMilvusResp(getResp, entityType);
    }

    /**
     * 按主键批量获取（语义别名）。
     */
    public MilvusResp<List<MilvusResult<T>>> byIds(Serializable... ids) {
        return getById(ids);
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