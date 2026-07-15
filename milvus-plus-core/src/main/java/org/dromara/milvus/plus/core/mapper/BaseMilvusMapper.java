package org.dromara.milvus.plus.core.mapper;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.annotation.MilvusCollection;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.core.conditions.*;
import org.dromara.milvus.plus.exception.MilvusPlusException;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

/**
 * @author xgc
 **/
@Slf4j
public abstract class BaseMilvusMapper<T> {


    public abstract MilvusClientV2 getClient();


    /**
     * 创建搜索构建器实例
     * @return 返回搜索构建器
     */
    public LambdaQueryWrapper<T> queryWrapper() {
        return lambda(new LambdaQueryWrapper<>());
    }

    /**
     * 针对动态集合名创建查询构建器（分表/多租户场景）。
     */
    public LambdaQueryWrapper<T> queryWrapper(String collectionName) {
        return lambda(collectionName, new LambdaQueryWrapper<>());
    }

    /**
     * 创建删除构建器实例
     * @return 返回删除构建器
     */
    public LambdaDeleteWrapper<T> deleteWrapper() {
        return lambda(new LambdaDeleteWrapper<>());
    }

    public LambdaDeleteWrapper<T> deleteWrapper(String collectionName) {
        return lambda(collectionName, new LambdaDeleteWrapper<>());
    }

    /**
     * 创建更新构建器实例
     * @return 返回删除构建器
     */
    public LambdaUpdateWrapper<T> updateWrapper() {
        return lambda(new LambdaUpdateWrapper<>());
    }

    public LambdaUpdateWrapper<T> updateWrapper(String collectionName) {
        return lambda(collectionName, new LambdaUpdateWrapper<>());
    }

    /**
     * 创建新增构建器实例
     * @return 返回删除构建器
     */
    public LambdaInsertWrapper<T> insertWrapper() {
        return lambda(new LambdaInsertWrapper<>());
    }

    public LambdaInsertWrapper<T> insertWrapper(String collectionName) {
        return lambda(collectionName, new LambdaInsertWrapper<>());
    }

    /**
     * 绑定物理集合名，后续 CRUD 都落到该集合（线程内短生命周期使用，勿做单例共享可变状态）。
     */
    public CollectionBoundMapper<T> forCollection(String collectionName) {
        if (StringUtils.isBlank(collectionName)) {
            throw MilvusPlusException.of("COLLECTION_NAME_EMPTY", "collectionName must not be blank");
        }
        Class<T> entityType = resolveEntityTypeQuietly();
        return new CollectionBoundMapper<>(this, collectionName, entityType);
    }

    /**
     * 显式指定实体类型 + 物理集合名（适合 MilvusService 匿名 Mapper 场景）。
     */
    public CollectionBoundMapper<T> forCollection(Class<T> entityType, String collectionName) {
        if (entityType == null) {
            throw MilvusPlusException.of("ENTITY_TYPE_NULL", "entityType must not be null");
        }
        if (StringUtils.isBlank(collectionName)) {
            throw MilvusPlusException.of("COLLECTION_NAME_EMPTY", "collectionName must not be blank");
        }
        return new CollectionBoundMapper<>(this, collectionName, entityType);
    }

    @SuppressWarnings("unchecked")
    private Class<T> resolveEntityTypeQuietly() {
        try {
            Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if (type instanceof Class) {
                return (Class<T>) type;
            }
        } catch (Exception ignored) {
            // anonymous mapper without concrete type parameter
        }
        return null;
    }


    public MilvusResp<List<MilvusResult<T>>> getById(Serializable ... ids) {
        LambdaQueryWrapper<T> lambda = queryWrapper();
        return lambda.getById(ids);
    }
    public MilvusResp<DeleteResp> removeById(Serializable ... ids){
        LambdaDeleteWrapper<T> lambda = deleteWrapper();
        return lambda.removeById(ids);
    }
    public MilvusResp<InsertResp> insert(T ... entity){
        LambdaInsertWrapper<T> lambda = insertWrapper();
        return lambda.insert(entity);
    }
    public MilvusResp<InsertResp> insert(Collection<T> entity){
        LambdaInsertWrapper<T> lambda = insertWrapper();
        return lambda.insert(entity.iterator());
    }
    public MilvusResp<UpsertResp> updateById(T... entity) {
        LambdaUpdateWrapper<T> lambda = updateWrapper();
        return lambda.updateById(entity);
    }
    public MilvusResp<UpsertResp> updateById(Collection<T> entity) {
        LambdaUpdateWrapper<T> lambda = updateWrapper();
        return lambda.updateById(entity.iterator());
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
        return lambda(entityType, wrapper);
    }

    public <W> W lambda(String collectionName, Wrapper<W, T> wrapper) {
        Type type = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class<T> entityType = (Class<T>) type;
        return lambda(entityType, collectionName, wrapper);
    }

    public  <W> W lambda(Class<T> entityType,Wrapper<W, T> wrapper) {
        return lambda(entityType, null, wrapper);
    }

    public <W> W lambda(Class<T> entityType, String collectionNameOverride, Wrapper<W, T> wrapper) {
        // 从实体类上获取@MilvusCollection注解
        MilvusCollection collectionAnnotation = entityType.getAnnotation(MilvusCollection.class);
        if (collectionAnnotation == null) {
            throw MilvusPlusException.of("ENTITY_ANNOTATION_MISSING",
                    "Entity type " + entityType.getName() + " is not annotated with @MilvusCollection.");
        }
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType.getName());
        if (conversionCache == null) {
            // 未走启动扫描时也能懒加载实体缓存（修复 getById 空缓存 NPE）
            MilvusConverter.convert(entityType);
            conversionCache = MilvusCache.milvusCache.get(entityType.getName());
        }
        String logicalName = conversionCache == null ? collectionAnnotation.name() : conversionCache.getCollectionName();
        String collectionName = StringUtils.isNotBlank(collectionNameOverride)
                ? collectionNameOverride
                : logicalName;
        // 动态集合名复用逻辑实体的主键字段映射
        if (StringUtils.isNotBlank(collectionNameOverride)) {
            String pk = CollectionToPrimaryCache.collectionToPrimary.get(logicalName);
            if (pk != null) {
                CollectionToPrimaryCache.collectionToPrimary.put(collectionNameOverride, pk);
            }
        }
        // 初始化构建器实例
        MilvusClientV2 client = getClient();
        wrapper.init(collectionName, client, conversionCache, entityType);
        return wrapper.wrapper();
    }

    /**
     * 动态集合绑定视图：同一实体映射到不同物理 collection。
     */
    public static final class CollectionBoundMapper<T> {
        private final BaseMilvusMapper<T> delegate;
        private final String collectionName;
        private final Class<T> entityType;

        private CollectionBoundMapper(BaseMilvusMapper<T> delegate, String collectionName, Class<T> entityType) {
            this.delegate = delegate;
            this.collectionName = collectionName;
            this.entityType = entityType;
        }

        public String getCollectionName() {
            return collectionName;
        }

        public LambdaQueryWrapper<T> queryWrapper() {
            if (entityType != null) {
                return delegate.lambda(entityType, collectionName, new LambdaQueryWrapper<>());
            }
            return delegate.queryWrapper(collectionName);
        }

        public LambdaInsertWrapper<T> insertWrapper() {
            if (entityType != null) {
                return delegate.lambda(entityType, collectionName, new LambdaInsertWrapper<>());
            }
            return delegate.insertWrapper(collectionName);
        }

        public LambdaUpdateWrapper<T> updateWrapper() {
            if (entityType != null) {
                return delegate.lambda(entityType, collectionName, new LambdaUpdateWrapper<>());
            }
            return delegate.updateWrapper(collectionName);
        }

        public LambdaDeleteWrapper<T> deleteWrapper() {
            if (entityType != null) {
                return delegate.lambda(entityType, collectionName, new LambdaDeleteWrapper<>());
            }
            return delegate.deleteWrapper(collectionName);
        }

        public MilvusResp<InsertResp> insert(T... entity) {
            return insertWrapper().insert(entity);
        }

        public MilvusResp<InsertResp> insert(Collection<T> entity) {
            return insertWrapper().insert(entity.iterator());
        }

        public MilvusResp<UpsertResp> updateById(T... entity) {
            return updateWrapper().updateById(entity);
        }

        public MilvusResp<UpsertResp> updateByIdPartial(T... entity) {
            return updateWrapper().partial(true).updateById(entity);
        }

        public MilvusResp<DeleteResp> removeById(Serializable... ids) {
            return deleteWrapper().removeById(ids);
        }

        public MilvusResp<List<MilvusResult<T>>> getById(Serializable... ids) {
            return queryWrapper().getById(ids);
        }
    }
}
