package io.github.javpower.milvus.plus.cache;

import io.github.javpower.milvus.plus.model.MilvusEntity;
import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class ConversionCache<T,R> {
    private String collectionName;
    private FieldFunctionCache<T,R> fieldFunctionCache;
    private PropertyCache propertyCache;
    private MilvusEntity milvusEntity;

}
