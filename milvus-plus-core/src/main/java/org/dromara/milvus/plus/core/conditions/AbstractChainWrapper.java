package org.dromara.milvus.plus.core.conditions;

import io.milvus.v2.client.MilvusClientV2;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.model.vo.MilvusResp;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class AbstractChainWrapper<T> extends ConditionBuilder<T>{
    protected static class ArrayIterator<T> implements Iterator<T> {
        private final T[] array;
        private int index = 0;

        public ArrayIterator(T[] array) {
            this.array = array;
        }
        @Override
        public boolean hasNext() {
            return index < array.length;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            return array[index++];
        }
    }
    // 定义最大重试次数的常量
    public static final int maxRetries = 2;
    protected <R> MilvusResp<R> executeWithRetry(Supplier<MilvusResp<R>> action, String errorMessage, int maxRetries,Class entityType, MilvusClientV2 client) {
        int attempt = 1;
        while (true) {
            try {
                return action.get(); // 尝试执行操作
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains(errorMessage) && attempt < maxRetries) {
                    log.warn("Attempt {}: {} - attempting to retry.", attempt, errorMessage);
                    handleCollectionNotLoaded(entityType,client);
                    attempt++;
                } else {
                    throw new RuntimeException(e); // 如果不是预期的错误或者重试次数达到上限，则抛出异常
                }
            }
        }
    }
    protected void handleCollectionNotLoaded(Class entityType, MilvusClientV2 client) {
        ConversionCache cache = MilvusCache.milvusCache.get(entityType.getName());
        MilvusConverter.loadStatus(cache.getMilvusEntity(), client);
    }

}
