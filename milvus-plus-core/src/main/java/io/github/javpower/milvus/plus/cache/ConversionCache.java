package io.github.javpower.milvus.plus.cache;

import io.github.javpower.milvus.plus.model.MilvusEntity;
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
