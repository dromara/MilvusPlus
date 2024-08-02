package org.dromara.milvus.plus.core.conditions;

import com.google.gson.JsonObject;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.PropertyCache;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.util.GsonUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 构建器内部类，用于构建update请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class LambdaUpdateWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaUpdateWrapper<T>, T> {
    private ConversionCache conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;

    public LambdaUpdateWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache = conversionCache;
        this.entityType = entityType;
    }

    public LambdaUpdateWrapper() {

    }

    public LambdaUpdateWrapper<T> partition(String partitionName) {
        this.partitionName = partitionName;
        return this;
    }

    public LambdaUpdateWrapper<T> partition(FieldFunction<T, ?> partitionName) {
        this.partitionName = partitionName.getFieldName(partitionName);
        return this;
    }

    /**
     * 添加等于条件。
     *
     * @param fieldName 字段名
     * @param value     要比较的值
     * @return 当前条件构建器对象
     */
    public LambdaUpdateWrapper<T> eq(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> ne(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> gt(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> ge(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> lt(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> le(String fieldName, Object value) {
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
    public LambdaUpdateWrapper<T> between(String fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    /**
     * 添加空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaUpdateWrapper<T> isNull(String fieldName) {
        super.isNull(fieldName);
        return this;
    }

    /**
     * 添加非空值检查条件。
     *
     * @param fieldName 字段名
     * @return 当前条件构建器对象
     */
    public LambdaUpdateWrapper<T> isNotNull(String fieldName) {
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
    public LambdaUpdateWrapper<T> in(String fieldName, List<?> values) {
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
    public LambdaUpdateWrapper<T> like(String fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> jsonContains(String fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> jsonContainsAll(String fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaUpdateWrapper<T> jsonContainsAny(String fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaUpdateWrapper<T> arrayContains(String fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> arrayContainsAll(String fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }

    public LambdaUpdateWrapper<T> arrayContainsAny(String fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaUpdateWrapper<T> arrayLength(String fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    public LambdaUpdateWrapper<T> eq(FieldFunction<T,?> fieldName, Object value) {
        super.eq(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> ne(FieldFunction<T,?> fieldName, Object value) {
        super.ne(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> gt(FieldFunction<T,?> fieldName, Object value) {
        super.gt(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> ge(FieldFunction<T,?> fieldName, Object value) {
        super.ge(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> lt(FieldFunction<T,?> fieldName, Object value) {
        super.lt(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> le(FieldFunction<T,?> fieldName, Object value) {
        super.le(fieldName,value);
        return this;
    }

    // Range operation
    public LambdaUpdateWrapper<T> between(FieldFunction<T,?> fieldName, Object start, Object end) {
        super.between(fieldName,start,end);
        return this;
    }

    // Null check
    public LambdaUpdateWrapper<T> isNull(FieldFunction<T,?> fieldName) {
        super.isNull(fieldName);
        return this;
    }

    public LambdaUpdateWrapper<T> isNotNull(FieldFunction<T,?> fieldName) {
        super.isNotNull(fieldName);
        return this;
    }

    // In operator
    public LambdaUpdateWrapper<T> in(FieldFunction<T,?> fieldName, List<?> values) {
        super.in(fieldName,values);
        return this;
    }

    // Like operator
    public LambdaUpdateWrapper<T> like(FieldFunction<T,?> fieldName, String value) {
        super.like(fieldName,value);
        return this;
    }

    // JSON array operations
    public LambdaUpdateWrapper<T> jsonContains(FieldFunction<T,?> fieldName, Object value) {
        super.jsonContains(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> jsonContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAll(fieldName,values);
        return this;
    }

    public LambdaUpdateWrapper<T> jsonContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.jsonContainsAny(fieldName,values);
        return this;
    }

    // Array operations
    public LambdaUpdateWrapper<T> arrayContains(FieldFunction<T,?> fieldName, Object value) {
        super.arrayContains(fieldName,value);
        return this;
    }

    public LambdaUpdateWrapper<T> arrayContainsAll(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAll(fieldName,values);
        return this;
    }
    public LambdaUpdateWrapper<T> arrayContainsAny(FieldFunction<T,?> fieldName, List<?> values) {
        super.arrayContainsAny(fieldName,values);
        return this;
    }

    public LambdaUpdateWrapper<T> arrayLength(FieldFunction<T,?> fieldName, int length) {
        super.arrayLength(fieldName,length);
        return this;
    }

    // Logic operations
    public LambdaUpdateWrapper<T> and(ConditionBuilder<T> other) {
        super.and(other);
        return this;
    }

    public LambdaUpdateWrapper<T> or(ConditionBuilder<T> other) {
        super.or(other);
        return this;
    }

    public LambdaUpdateWrapper<T> not() {
        super.not();
        return this;
    }

    /**
     * 构建完整的删除请求
     *
     * @return 搜索请求对象
     */
    private QueryResp build() {
        String filterStr = buildFilters();
        if (filterStr != null && !filterStr.isEmpty()) {
            QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                    .collectionName(collectionName).filter(filterStr);
            return client.query(builder.build());
        } else {
            return null;
        }
    }

    /**
     * 执行更新
     *
     * @return 更新响应对象
     */
    public MilvusResp<UpsertResp> update(T entity) throws MilvusException {
        // 获取主键字段
        String primaryKeyField = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        // 将实体转换为属性映射
        Map<String, Object> propertiesMap = getPropertiesMap(entity);
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        // 初始化主键标识和主键值
        boolean hasPrimaryKey = false;
        Object  primaryKeyValue = null;
        // 准备更新的数据列表
        List<JsonObject> updateDataList = new ArrayList<>();
        // 构建单个更新对象
        JsonObject updateObject = new JsonObject();
        for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            String tableNameColumn = propertyCache.functionToPropertyMap.get(field);
            // 检查是否为主键字段
            if (primaryKeyField.equals(tableNameColumn)) {
                hasPrimaryKey = true;
                primaryKeyValue = value;
            }
            // 添加到更新对象
            GsonUtil.put(updateObject,tableNameColumn, value);
        }
        // 检查是否需要构建查询条件
        boolean needBuildQuery = !hasPrimaryKey;
        if (hasPrimaryKey) {
            for (Map.Entry<String, String> property : propertyCache.functionToPropertyMap.entrySet()) {
                if (updateObject.get(property.getValue()) == null) {
                    needBuildQuery = true;
                    eq(primaryKeyField,primaryKeyValue);
                    break;
                }
            }
        }

        // 如果需要构建查询条件，则执行查询并准备更新数据
        if (needBuildQuery) {
            QueryResp queryResp = build();
            if (queryResp != null) {
                for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                    Map<String, Object> existingEntity = result.getEntity();
                    JsonObject existingData = new JsonObject();
                    for (Map.Entry<String, Object> existingEntry : existingEntity.entrySet()) {
                        String existingField = existingEntry.getKey();
                        Object existingValue = existingEntry.getValue();
                        Object updateValue = updateObject.get(existingField);
                        GsonUtil.put(existingData,existingField, updateValue != null ? updateValue : existingValue);
                    }

                    updateDataList.add(existingData);
                }
            }
        } else {
            updateDataList.add(updateObject);
        }

        // 检查是否有数据需要更新
        if (updateDataList.isEmpty()) {
            return new MilvusResp<>();
        }
        // 执行更新操作
        return update(updateDataList);
    }

    private MilvusResp<UpsertResp> update(List<JsonObject> jsonObjects) {
        return executeWithRetry(
                () -> {
                    log.info("update data--->{}", GsonUtil.toJson(jsonObjects));
                    UpsertReq.UpsertReqBuilder<?, ?> builder = UpsertReq.builder()
                            .collectionName(collectionName)
                            .data(jsonObjects);
                    if (StringUtils.isNotEmpty(partitionName)) {
                        builder.partitionName(partitionName);
                    }
                    UpsertReq upsertReq = builder
                            .build();
                    UpsertResp upsert = client.upsert(upsertReq);
                    MilvusResp<UpsertResp> resp = new MilvusResp<>();
                    resp.setData(upsert);
                    resp.setSuccess(true);
                    return resp;
                },
                "collection not loaded",
                maxRetries,
                entityType,
                client
        );
    }

    public MilvusResp<UpsertResp> updateById(T... entity) throws MilvusException {
        Iterator<T> iterator = new ArrayIterator<>(entity);
        return updateById(iterator);
    }

    public MilvusResp<UpsertResp> updateById(Iterator<T> iterator) throws MilvusException {
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        String pk = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        List<JsonObject> jsonObjects = new ArrayList<>();
        // 使用迭代器遍历可变参数
        while (iterator.hasNext()) {
            T t1 = iterator.next();
            Map<String, Object> propertiesMap = getPropertiesMap(t1);
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                // 根据PropertyCache转换属性名
                String tk = propertyCache.functionToPropertyMap.get(key);
                GsonUtil.put(jsonObject,tk, value);
            }
            // 检查是否包含主键
            if (!jsonObject.has(pk)) {
                throw new MilvusException("not find primary key", 400);
            }
            jsonObjects.add(jsonObject);
        }
        // 准备更新的数据列表
        List<JsonObject> updateDataList = new ArrayList<>();
        for (JsonObject updateObject : jsonObjects) {
            boolean isBuild=false;
            for (Map.Entry<String, String> property : propertyCache.functionToPropertyMap.entrySet()) {
                if (updateObject.get(property.getValue()) == null) {
                    //缺少数据需要
                    isBuild=true;
                }
            }
            if(isBuild){
                QueryReq.QueryReqBuilder<?, ?> builder = QueryReq.builder()
                        .collectionName(collectionName).filter(pk+" == "+updateObject.get(pk));
                QueryResp queryResp= client.query(builder.build());
                if (queryResp != null) {
                    for (QueryResp.QueryResult result : queryResp.getQueryResults()) {
                        Map<String, Object> existingEntity = result.getEntity();
                        JsonObject existingData = new JsonObject();
                        for (Map.Entry<String, Object> existingEntry : existingEntity.entrySet()) {
                            String existingField = existingEntry.getKey();
                            Object existingValue = existingEntry.getValue();
                            Object updateValue = updateObject.get(existingField);
                            GsonUtil.put(existingData,existingField, updateValue != null ? updateValue : existingValue);
                        }
                        updateDataList.add(existingData);
                    }
                }
            }else {
                updateDataList.add(updateObject);
            }
        }
        return update(updateDataList);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        setClient(client);
        setCollectionName(collectionName);
        setEntityType(entityType);
        setConversionCache(conversionCache);
    }

    @Override
    public LambdaUpdateWrapper<T> wrapper() {
        return this;
    }
}