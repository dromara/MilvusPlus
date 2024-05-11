package io.github.javpower.milvus.plus.core.mapper;

import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.core.conditions.*;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.util.SpringUtils;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author xgc
 **/
@Slf4j
public class MilvusMapper<T> {

    /**
     * 创建搜索构建器实例
     * @return 返回搜索构建器
     */
    public LambdaQueryWrapper<T> queryWrapper() {
        return lambda(new LambdaQueryWrapper<>());
    }

    /**
     * 创建删除构建器实例
     * @return 返回删除构建器
     */
    public LambdaDeleteWrapper<T> deleteWrapper() {
        return lambda(new LambdaDeleteWrapper<>());
    }

    /**
     * 创建更新构建器实例
     * @return 返回删除构建器
     */
    public LambdaUpdateWrapper<T> updateWrapper() {
        return lambda(new LambdaUpdateWrapper<>());
    }

    /**
     * 创建新增构建器实例
     * @return 返回删除构建器
     */
    public LambdaInsertWrapper<T> insertWrapper() {
        return lambda(new LambdaInsertWrapper<>());
    }


    public MilvusResp<List<T>> getById(Serializable ... ids) {
        LambdaQueryWrapper<T> lambda = queryWrapper();
        return lambda.getById(ids);
    }
    public MilvusResp<DeleteResp> removeById(Serializable ... ids){
        LambdaDeleteWrapper<T> lambda = deleteWrapper();
        return lambda.removeById(ids);
    }
    public MilvusResp<UpsertResp> updateById(T ... entity){
        LambdaUpdateWrapper<T> lambda = updateWrapper();
        return lambda.updateById(entity);
    }
    public MilvusResp<InsertResp> insert(T ... entity){
        LambdaInsertWrapper<T> lambda = insertWrapper();
        return lambda.insert(entity);
    }


    /**
     * 创建通用构建器实例
     * @param wrapper 构建器实例
     * @return 返回构建器实例
     */
    public  <W> W lambda(Wrapper<W, T> wrapper) {
        // 获取实例化的类的类型参数T
        Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class<T> entityType = (Class<T>) type;
        // 从实体类上获取@MilvusCollection注解
        MilvusCollection collectionAnnotation = entityType.getAnnotation(MilvusCollection.class);
        if (collectionAnnotation == null) {
            throw new IllegalStateException("Entity type " + entityType.getName() + " is not annotated with @MilvusCollection.");
        }
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType);
        String collectionName = conversionCache == null ? null : conversionCache.getCollectionName();
        // 使用SpringUtil获取MilvusClient实例
        MilvusClientV2 client = SpringUtils.getBean(MilvusClientV2.class);
        // 初始化构建器实例
        wrapper.init(collectionName, client, conversionCache, entityType);
        return wrapper.wrapper();
    }



}