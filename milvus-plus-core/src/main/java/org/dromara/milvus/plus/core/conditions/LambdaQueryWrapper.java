package org.dromara.milvus.plus.core.conditions;

import com.alibaba.fastjson2.JSON;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.GetReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.converter.SearchRespConverter;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
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

    public LambdaQueryWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache = conversionCache;
        this.entityType = entityType;
    }

    public LambdaQueryWrapper() {

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

    public LambdaQueryWrapper<T> jsonContains(String fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaQueryWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaQueryWrapper<T> arrayContains(String fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }

    public LambdaQueryWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaQueryWrapper<T> arrayLength(String fieldName, int length) {
        super.arrayLength(fieldName,length);
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

    // Like operator
    public LambdaQueryWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        super.like(fieldName,value);
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
    public LambdaQueryWrapper<T> vector(List<? extends Float> vector) {
        BaseVector baseVector = new FloatVec((List<Float>) vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(String annsField, List<? extends Float> vector) {
        this.annsField=annsField;
        BaseVector baseVector = new FloatVec((List<Float>) vector);
        vectors.add(baseVector);
        return this;
    }
    public LambdaQueryWrapper<T> vector(FieldFunction<T,?> annsField, List<? extends Float> vector) {
        this.annsField=annsField.getFieldName(annsField);
        BaseVector baseVector = new FloatVec((List<Float>) vector);
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
    public LambdaQueryWrapper<T> topK(Integer topK) {
        this.setTopK(topK);
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

    public QueryReq buildQuery() {
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

    /**
     * 执行搜索
     *
     * @return 搜索响应对象
     */
//    public MilvusResp<List<MilvusResult<T>>> query() throws MilvusException {
//        if (!vectors.isEmpty()) {
//            SearchReq searchReq = buildSearch();
//            log.info("build search param-->{}", JSON.toJSONString(searchReq));
//            SearchResp search = client.search(searchReq);
//            return SearchRespConverter.convertSearchRespToMilvusResp(search, entityType);
//        } else {
//            QueryReq queryReq = buildQuery();
//            log.info("build query param-->{}", JSON.toJSONString(queryReq));
//            QueryResp query = client.query(queryReq);
//            return SearchRespConverter.convertGetRespToMilvusResp(query, entityType);
//        }
//    }
    public MilvusResp<List<MilvusResult<T>>> query() throws MilvusException{
        return query(1);
    }
    private MilvusResp<List<MilvusResult<T>>> query(int attempt) throws MilvusException{
        try {
            return executeQuery(attempt);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("collection not loaded") && attempt < maxRetries) {
                log.warn("Attempt {}: Collection not loaded, attempting to reload and retry.", attempt);
                handleCollectionNotLoaded();
                return query(attempt + 1);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
    private MilvusResp<List<MilvusResult<T>>> executeQuery(int attempt) throws Exception {
        if (!vectors.isEmpty()) {
            SearchReq searchReq = buildSearch();
            log.info("Attempt {}: Build search param--> {}", attempt, JSON.toJSONString(searchReq));
            SearchResp searchResp = client.search(searchReq);
            return SearchRespConverter.convertSearchRespToMilvusResp(searchResp, entityType);
        } else {
            QueryReq queryReq = buildQuery();
            log.info("Attempt {}: Build query param--> {}", attempt, JSON.toJSONString(queryReq));
            QueryResp queryResp = client.query(queryReq);
            return SearchRespConverter.convertGetRespToMilvusResp(queryResp, entityType);
        }
    }

    private void handleCollectionNotLoaded() {
        ConversionCache cache = MilvusCache.milvusCache.get(entityType.getName());
        MilvusConverter.loadStatus(cache.getMilvusEntity(), client);
    }

    // 定义最大重试次数的常量
    private static final int maxRetries = 2;

    public MilvusResp<List<MilvusResult<T>>> query(FieldFunction<T, ?>... outputFields) throws MilvusException {
        List<String> otf = new ArrayList<>();
        for (FieldFunction<T, ?> outputField : outputFields) {
            otf.add(outputField.getFieldName(outputField));
        }
        this.outputFields = otf;
        return query();
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