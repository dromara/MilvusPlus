package io.github.javpower.milvus.plus.cache;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author xgc
 **/
@Data
public class MilvusCache {
    public static final Map<String,ConversionCache> milvusCache=new ConcurrentHashMap<>(); //类名-->缓存

}
