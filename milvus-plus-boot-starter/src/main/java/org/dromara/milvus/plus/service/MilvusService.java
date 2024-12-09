package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.converter.SearchRespConverter;
import org.dromara.milvus.plus.core.conditions.LambdaDeleteWrapper;
import org.dromara.milvus.plus.core.conditions.LambdaInsertWrapper;
import org.dromara.milvus.plus.core.conditions.LambdaQueryWrapper;
import org.dromara.milvus.plus.core.conditions.LambdaUpdateWrapper;
import org.dromara.milvus.plus.core.mapper.BaseMilvusMapper;
import org.dromara.milvus.plus.model.MilvusEntity;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.util.MilvusSpringUtils;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class MilvusService implements IAMService,ICMService,IVecMService{

    @Override
    public MilvusClientV2 getClient() {
        return MilvusSpringUtils.getBean(MilvusClientV2.class);
    }

    public <T> MilvusResp<List<MilvusResult<T>>> getById(Class<T> entityClass,Serializable... ids) {
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityClass.getName());
        MilvusEntity milvusEntity = conversionCache.getMilvusEntity();
        GetResp getResp = getByIds(milvusEntity.getCollectionName(),Arrays.asList(ids));
        MilvusResp<List<MilvusResult<T>>> milvusResp = SearchRespConverter.convertGetRespToMilvusResp(getResp, entityClass);
        return milvusResp;
    }

    public <T>MilvusResp<DeleteResp> removeById(Class<T> entityClass,Serializable ... ids){
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityClass.getName());
        DeleteResp deleteResp = deleteByIds(conversionCache.getCollectionName(), Arrays.asList(ids));
        MilvusResp<DeleteResp> resp = new MilvusResp<>();
        resp.setData(deleteResp);
        resp.setSuccess(true);
        return resp;
    }
    public <T>MilvusResp<InsertResp> insert(T ... entities){
        Class<T> entityClass = getEntityClass(entities[0]);
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaInsertWrapper<T> lambda = mapper.lambda(entityClass, new LambdaInsertWrapper<>());
        return lambda.insert(entities);
    }
    public <T>MilvusResp<InsertResp> insert(Collection<T> entities){
        T entity = entities.iterator().next();
        Class<T> entityClass = getEntityClass(entity);
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaInsertWrapper<T> lambda = mapper.lambda(entityClass, new LambdaInsertWrapper<>());
        return lambda.insert(entities.iterator());
    }
    public <T>MilvusResp<UpsertResp> updateById(T... entities) {
        Class<T> entityClass = getEntityClass(entities[0]);
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaUpdateWrapper<T> lambda = mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
        return lambda.updateById(entities);
    }
    public <T>MilvusResp<UpsertResp> updateById(Collection<T> entities) {
        T entity = entities.iterator().next();
        Class<T> entityClass = getEntityClass(entity);
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaUpdateWrapper<T> lambda = mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
        return lambda.updateById(entities.iterator());
    }
    public <T>LambdaUpdateWrapper ofUpdate(Class<T> entityClass){
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaUpdateWrapper<T> lambda = mapper.lambda(entityClass, new LambdaUpdateWrapper<>());
        return lambda;
    }
    public <T>LambdaInsertWrapper ofInsert(Class<T> entityClass){
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaInsertWrapper<T> lambda = mapper.lambda(entityClass, new LambdaInsertWrapper<>());
        return lambda;
    }
    public <T>LambdaDeleteWrapper ofDelete(Class<T> entityClass){
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaDeleteWrapper<T> lambda = mapper.lambda(entityClass, new LambdaDeleteWrapper<>());
        return lambda;
    }
    public <T> LambdaQueryWrapper ofQuery(Class<T> entityClass){
        BaseMilvusMapper<T> mapper=getBaseMilvusMapper();
        LambdaQueryWrapper<T> lambda = mapper.lambda(entityClass, new LambdaQueryWrapper<>());
        return lambda;
    }

    private <T> BaseMilvusMapper<T> getBaseMilvusMapper() {
        MilvusClientV2 client = getClient();
        return new BaseMilvusMapper<T>() {
            @Override
            public MilvusClientV2 getClient() {
                return client;
            }
        };
    }
    private <T> Class<T> getEntityClass(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        return (Class<T>) entity.getClass();
    }

}
