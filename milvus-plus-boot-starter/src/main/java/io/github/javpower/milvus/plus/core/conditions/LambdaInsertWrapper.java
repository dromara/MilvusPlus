package io.github.javpower.milvus.plus.core.conditions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.core.FieldFunction;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.service.MilvusClient;
import io.milvus.exception.MilvusException;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
* 构建器内部类，用于构建insert请求
*/
@Data
@Slf4j
public  class LambdaInsertWrapper<T> extends AbstractChainWrapper<T> implements Wrapper<LambdaInsertWrapper<T>,T>{
    private ConversionCache<?, ?> conversionCache;
    private Class<T> entityType;
    private String collectionName;
    private MilvusClient client;
    private JSONObject entity=new JSONObject();
    public LambdaInsertWrapper(String collectionName, MilvusClient client, ConversionCache<?, ?> conversionCache, Class<T> entityType) {
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

    /**
     * 构建完整的insert请求
     * @return 搜索请求对象
     */
    public MilvusResp<InsertResp> insert() {
        if(entity.size()>0){
            return insert(Collections.singletonList(entity));
        }
        throw new MilvusException("not insert data",400);
    }


    private MilvusResp<InsertResp> insert(List<JSONObject> jsonObjects){
        log.info("insert data--->{}", JSON.toJSONString(jsonObjects));
        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(jsonObjects)
                .build();
        InsertResp insert = client.client.insert(insertReq);
        MilvusResp<InsertResp> resp=new MilvusResp();
        resp.setData(insert);
        resp.setSuccess(true);
        return resp;
    }

    public MilvusResp<InsertResp> insert(T ...t) throws MilvusException {
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        List<JSONObject> jsonObjects=new ArrayList<>();
        for (T t1 : t) {
            Map<String, Object> propertiesMap = getPropertiesMap(t1);
            JSONObject jsonObject=new JSONObject();
            for (Map.Entry<String, Object> entry : propertiesMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String tk = propertyCache.functionToPropertyMap.get(key);
                jsonObject.put(tk,value);
            }
            jsonObjects.add(jsonObject);

        }
       return insert(jsonObjects);
    }

    @Override
    public void init(String collectionName, MilvusClient client, ConversionCache conversionCache, Class entityType) {
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