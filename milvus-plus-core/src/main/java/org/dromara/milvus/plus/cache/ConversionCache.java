package org.dromara.milvus.plus.cache;

import lombok.Data;
import org.dromara.milvus.plus.model.MilvusEntity;
/**
 * @author xgc
 **/
@Data
public class ConversionCache {
    private String collectionName;
    private PropertyCache propertyCache;
    private MilvusEntity milvusEntity;
    private boolean autoID;

}
