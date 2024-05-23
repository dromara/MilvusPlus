package org.dromara.milvus.plus.cache;

import org.dromara.milvus.plus.model.MilvusEntity;
import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class ConversionCache {
    private String collectionName;
    private PropertyCache propertyCache;
    private MilvusEntity milvusEntity;

}
