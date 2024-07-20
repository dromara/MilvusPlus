package org.dromara.milvus.plus.core.conditions;

import com.alibaba.fastjson.JSON;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.vo.MilvusResp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
     * 构建器内部类，用于构建remove请求
     */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public  class LambdaDeleteWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaDeleteWrapper<T>,T>{
    private ConversionCache conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;
    private List<Object> ids=new ArrayList<>();

    public LambdaDeleteWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache=conversionCache;
        this.entityType=entityType;
    }

    public LambdaDeleteWrapper() {

    }
    public LambdaDeleteWrapper<T> partition(String partitionName){
        this.partitionName=partitionName;
        return this;
    }
    public LambdaDeleteWrapper<T> partition(FieldFunction<T,?> partitionName){
        this.partitionName=partitionName.getFieldName(partitionName);
        return this;
    }
    /**
     * 添加等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaDeleteWrapper<T> eq(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> ne(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> gt(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> ge(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> lt(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> le(String fieldName, Object value) {
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
    public LambdaDeleteWrapper<T> between(String fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    /**
     * 添加空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaDeleteWrapper<T> isNull(String fieldName) {
        super.isNull(fieldName);
        return this;
    }

    /**
     * 添加非空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaDeleteWrapper<T> isNotNull(String fieldName) {
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
    public LambdaDeleteWrapper<T> in(String fieldName, List<?> values) {
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
    public LambdaDeleteWrapper<T> like(String fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> jsonContains(String fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaDeleteWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaDeleteWrapper<T> arrayContains(String fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }

    public LambdaDeleteWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaDeleteWrapper<T> arrayLength(String fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    public LambdaDeleteWrapper<T> eq(FieldFunction<T,?> fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> ne(FieldFunction<T,?> fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> gt(FieldFunction<T,?> fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> ge(FieldFunction<T,?> fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> lt(FieldFunction<T,?> fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> le(FieldFunction<T,?> fieldName, Object value) {
        super.le(fieldName,value);
        return this;
    }

    // Range operation
    public LambdaDeleteWrapper<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    // Null check
    public LambdaDeleteWrapper<T> isNull(FieldFunction<T,?> fieldName) {
        super.isNull(fieldName);
        return this;
    }

    public LambdaDeleteWrapper<T> isNotNull(FieldFunction<T,?> fieldName) {
        super.isNotNull(fieldName);
        return this;
    }

    // In operator
    public LambdaDeleteWrapper<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }

    // Like operator
    public LambdaDeleteWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    // JSON array operations
    public LambdaDeleteWrapper<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaDeleteWrapper<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaDeleteWrapper<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaDeleteWrapper<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }
    public LambdaDeleteWrapper<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaDeleteWrapper<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    // Logic operations
    public LambdaDeleteWrapper<T> and(ConditionBuilder<T> other) {
        super.and(other);
        return this;
    }

    public LambdaDeleteWrapper<T> or(ConditionBuilder<T> other) {
        super.or(other);
        return this;
    }

    public LambdaDeleteWrapper<T> not() {
        super.not();
        return this;
    }

    public LambdaDeleteWrapper<T> id(Object id) {
        this.ids.add(id);
        return this;
    }
    public LambdaDeleteWrapper<T> id(Object... id) {
        this.ids.addAll(Arrays.asList(id));
        return this;
    }
    public LambdaDeleteWrapper<T> id(List<Object> ids) {
        this.ids.addAll(ids);
        return this;
    }
    /**
     * 构建完整的删除请求
     * @return 搜索请求对象
     */
    private DeleteReq build() {
        DeleteReq.DeleteReqBuilder<?, ?> builder = DeleteReq.builder()
                .collectionName(this.collectionName);
        String filterStr = buildFilters();
        if (filterStr != null && !filterStr.isEmpty()) {
            builder.filter(filterStr);
        }
        if(StringUtils.isNotEmpty(partitionName)){
            builder.partitionName(partitionName);
        }
        if (!ids.isEmpty()) {
            builder.ids(this.ids);
        }
        // Set other parameters as needed
        return builder.build();
    }

    /**
     * 执行搜索
     * @return 搜索响应对象
     */
    public MilvusResp<DeleteResp> remove() throws MilvusException {
        return executeWithRetry(
                () -> {
                    DeleteReq deleteReq = build();
                    log.info("build remove param-->{}", JSON.toJSONString(deleteReq));
                    DeleteResp delete = client.delete(deleteReq);
                    MilvusResp<DeleteResp> resp = new MilvusResp<>();
                    resp.setData(delete);
                    resp.setSuccess(true);
                    return resp;
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }

    public MilvusResp<DeleteResp> removeById(Serializable... ids) throws MilvusException {
        this.id(ids);
        return remove();
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        setClient(client);
        setCollectionName(collectionName);
        setEntityType(entityType);
        setConversionCache(conversionCache);
    }

    @Override
    public LambdaDeleteWrapper<T> wrapper() {
        return this;
    }
}