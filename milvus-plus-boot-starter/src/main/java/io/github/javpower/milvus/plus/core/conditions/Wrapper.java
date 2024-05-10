package io.github.javpower.milvus.plus.core.conditions;

import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.service.MilvusClient;

/**
     * 通用构建器接口
     * @param <W> 构建器类型
     * @param <T> 实体类型
     */
public interface Wrapper<W, T> {
    void init(String collectionName, MilvusClient client, ConversionCache<?, ?> conversionCache, Class<T> entityType);
    W wrapper();
}