package org.dromara.milvus.plus.cache;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author xgc
 **/
@Data
public class MilvusCache {
    public static final Map<String,ConversionCache> milvusCache=new ConcurrentHashMap<>(); //类名-->缓存

    /**
     * 清理指定实体缓存（热更新实体注解后可调用，随后会懒加载重建）。
     */
    public static void evict(Class<?> entityClass) {
        if (entityClass != null) {
            milvusCache.remove(entityClass.getName());
        }
    }

    /**
     * 清空全部实体缓存。
     */
    public static void clear() {
        milvusCache.clear();
    }
}
