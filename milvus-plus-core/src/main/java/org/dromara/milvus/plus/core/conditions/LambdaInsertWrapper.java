package org.dromara.milvus.plus.core.conditions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.cache.PropertyCache;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.util.IdWorkerUtils;

import java.util.*;

/**
* 构建器内部类，用于构建insert请求
*/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public  class LambdaInsertWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaInsertWrapper<T>,T>{
    private ConversionCache conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private String partitionName;
    private MilvusClientV2 client;
    private JSONObject entity=new JSONObject();
    public LambdaInsertWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        this.collectionName = collectionName;
        this.client = client;
        this.conversionCache=conversionCache;
        this.entityType=entityType;
    }

    public LambdaInsertWrapper() {

    }
    public LambdaInsertWrapper<T> put(FieldFunction<T,?> fieldName, Object value){
        this.entity.put(fieldName.getFieldName(fieldName),value);
        return this;
    }
    public LambdaInsertWrapper<T> put(String fieldName, Object value){
        this.entity.put(fieldName,value);
        return this;
    }
    public LambdaInsertWrapper<T> partition(String partitionName){
        this.partitionName=partitionName;
       return this;
    }
    public LambdaInsertWrapper<T> partition(FieldFunction<T,?> partitionName){
        this.partitionName=partitionName.getFieldName(partitionName);
        return this;
    }

    /**
     * 构建完整的insert请求
     * @return 搜索请求对象
     */
    public MilvusResp<InsertResp> insert() {
        if (!entity.isEmpty()) {
            return insert(Collections.singletonList(entity));
        }
        throw new MilvusException("not insert data",400);
    }


    private MilvusResp<InsertResp> insert(List<JSONObject> jsonObjects){
        log.info("insert data--->{}", JSON.toJSONString(jsonObjects));
        InsertReq.InsertReqBuilder<?, ?> builder = InsertReq.builder()
                .collectionName(collectionName)
                .data(jsonObjects);
        if(StringUtils.isNotEmpty(partitionName)){
            builder.partitionName(partitionName);
        }
        InsertReq insertReq = builder
                .build();
        InsertResp insert = client.insert(insertReq);
        MilvusResp<InsertResp> resp = new MilvusResp<>();
        resp.setData(insert);
        resp.setSuccess(true);
        return resp;
    }
    public MilvusResp<InsertResp> insert(T ... entity) throws MilvusException {
        Iterator<T> iterator = new ArrayIterator<>(entity);
        return insert(iterator);
    }
    public MilvusResp<InsertResp> insert(Iterator<T> iterator) throws MilvusException {
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType.getName());
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        String pk = CollectionToPrimaryCache.collectionToPrimary.get(collectionName);
        List<JSONObject> jsonObjects=new ArrayList<>();
        while (iterator.hasNext()) {
            T t1 = iterator.next();
            Map<String, Object> propertiesMap = getPropertiesMap(t1);
            JSONObject jsonObject=new JSONObject();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String tk = propertyCache.functionToPropertyMap.get(key);
                jsonObject.put(tk,value);
            }
            if(conversionCache.isAutoID()){
                jsonObject.put(pk, IdWorkerUtils.nextId());
            }
            jsonObjects.add(jsonObject);
        }
       return insert(jsonObjects);
    }

    @Override
    public void init(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType) {
        setClient(client);
        setCollectionName(collectionName);
        setEntityType(entityType);
        setConversionCache(conversionCache);
    }

    @Override
    public LambdaInsertWrapper<T> wrapper() {
        return this;
    }
}