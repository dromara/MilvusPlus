package io.github.javpower.milvus.plus.cache;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author xgc
 **/
@Data
public class MilvusCache {
    public static final Map<Class<?>,ConversionCache> milvusCache=new ConcurrentHashMap<>();

}
