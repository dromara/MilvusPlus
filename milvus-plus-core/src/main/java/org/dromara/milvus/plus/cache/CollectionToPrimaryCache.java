package org.dromara.milvus.plus.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xgc
 **/
public class CollectionToPrimaryCache {

    public static Map<String, String> collectionToPrimary = new ConcurrentHashMap<>();//集合名称->主键名称

}
