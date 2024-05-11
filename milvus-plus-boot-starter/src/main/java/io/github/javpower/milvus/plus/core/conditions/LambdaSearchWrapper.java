package io.github.javpower.milvus.plus.core.conditions;

import com.alibaba.fastjson.JSON;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.converter.SearchRespConverter;
import io.github.javpower.milvus.plus.core.FieldFunction;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResultVo;
import io.github.javpower.milvus.plus.service.MilvusClient;
import io.milvus.exception.MilvusException;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
     * 搜索构建器内部类，用于构建搜索请求
     */
@Data
@Slf4j
public  class LambdaSearchWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaSearchWrapper<T>,T>{
    private ConversionCache<?, ?> conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String annsField;
    private int topK;
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
    /**
     * 添加等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> eq(String fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }

    /**
     * 添加不等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> ne(String fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }

    /**
     * 添加大于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> gt(String fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }

    /**
     * 添加大于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> ge(String fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }

    /**
     * 添加小于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> lt(String fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }

    /**
     * 添加小于等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> le(String fieldName, Object value) {
        super.le(fieldName,value);
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
    protected LambdaSearchWrapper<T> between(String fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    /**
     * 添加空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> isNull(String fieldName) {
        super.isNull(fieldName);
        return this;
    }

    /**
     * 添加非空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> isNotNull(String fieldName) {
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
    protected LambdaSearchWrapper<T> in(String fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }

    /**
     * 添加LIKE条件。
     *
     * @param fieldName 字段名
     * @param value     要匹配的模式
     * @return 当前条件构建器对象
     */
    protected LambdaSearchWrapper<T> like(String fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> jsonContains(String fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaSearchWrapper<T> arrayContains(String fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaSearchWrapper<T> arrayLength(String fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    public LambdaSearchWrapper<T> eq(FieldFunction<T,?> fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> ne(FieldFunction<T,?> fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> gt(FieldFunction<T,?> fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> ge(FieldFunction<T,?> fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> lt(FieldFunction<T,?> fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> le(FieldFunction<T,?> fieldName, Object value) {
        super.le(fieldName,value);
        return this;
    }

    // Range operation
    public LambdaSearchWrapper<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    // Null check
    public LambdaSearchWrapper<T> isNull(FieldFunction<T,?> fieldName) {
        super.isNull(fieldName);
        return this;
    }

    public LambdaSearchWrapper<T> isNotNull(FieldFunction<T,?> fieldName) {
        super.isNotNull(fieldName);
        return this;
    }

    // In operator
    public LambdaSearchWrapper<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }

    // Like operator
    public LambdaSearchWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    // JSON array operations
    public LambdaSearchWrapper<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaSearchWrapper<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaSearchWrapper<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaSearchWrapper<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }
    public LambdaSearchWrapper<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaSearchWrapper<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    // Logic operations
    public LambdaSearchWrapper<T> and(ConditionBuilder<T> other) {
        super.and(other);
        return this;
    }

    public LambdaSearchWrapper<T> or(ConditionBuilder<T> other) {
        super.or(other);
        return this;
    }

    public LambdaSearchWrapper<T> not() {
        super.not();
        return this;
    }

    public LambdaSearchWrapper<T> vector(List<Float> vector) {
        vectors.add(vector);
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
    /**
     * 构建完整的搜索请求
     * @return 搜索请求对象
     */
    private SearchReq build() {
        SearchReq.SearchReqBuilder<?, ?> builder = SearchReq.builder()
                .collectionName(collectionName)
                .annsField(annsField);

        if (!vectors.isEmpty()) {
            builder.data(vectors);
        }
        String filterStr = buildFilters();
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        if(topK>0){
            builder.topK(topK);
        }
        if(limit>0){
            builder.limit(limit);
        }
        // Set other parameters as needed
        return builder.build();
    }

    /**
     * 执行搜索
     * @return 搜索响应对象
     */
    public MilvusResp<MilvusResultVo<T>> query() throws MilvusException {
        SearchReq searchReq = build();
        log.info("build query param-->{}", JSON.toJSONString(searchReq));
        SearchResp search = client.client.search(searchReq);
        MilvusResp<MilvusResultVo<T>> tMilvusResp = SearchRespConverter.convertSearchRespToMilvusResp(search, entityType);
        return tMilvusResp;
    }
    public MilvusResp<List<T>> getById(Serializable ... ids){
        GetReq getReq = GetReq.builder()
                .collectionName(collectionName)
                .ids(Arrays.asList(ids))
                .build();
        GetResp getResp = client.client.get(getReq);
        MilvusResp<List<T>> tMilvusResp = SearchRespConverter.convertGetRespToMilvusResp(getResp, entityType);
        return tMilvusResp;

    }

    @Override
    public void init(String collectionName, MilvusClient client, ConversionCache conversionCache, Class entityType) {
        setClient(client);
        setCollectionName(collectionName);
        setEntityType(entityType);
        setConversionCache(conversionCache);
    }

    @Override
    public LambdaSearchWrapper<T> wrapper() {
        return this;
    }

}