package io.github.javpower.milvus.plus.converter;


import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.annotation.MilvusField;
import io.github.javpower.milvus.plus.annotation.MilvusIndex;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.CollectionToPrimaryCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.model.MilvusEntity;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
/**
 * @author xgc
 **/
public class MilvusEntityConverter {

    public static MilvusEntity convert(Class<?> entityClass) {
        MilvusEntity milvus=new MilvusEntity();
        MilvusCollection collectionAnnotation = entityClass.getAnnotation(MilvusCollection.class);
        if (collectionAnnotation == null) {
            throw new IllegalArgumentException("Entity must be annotated with @MilvusCollection");
        }
        String collectionName = collectionAnnotation.name();
        milvus.setCollectionName(collectionName);
        List<AddFieldReq> milvusFields = new ArrayList<>();
        List<IndexParam> indexParams=new ArrayList<>();
        PropertyCache propertyCache=new PropertyCache();
        CollectionToPrimaryCache collectionToPrimaryCache =new CollectionToPrimaryCache();
        for (Field field : entityClass.getDeclaredFields()) {
            MilvusField fieldAnnotation = field.getAnnotation(MilvusField.class);
            if (fieldAnnotation != null) {
                String fieldName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
                propertyCache.functionToPropertyMap.put(field.getName(),fieldName);
                if (fieldAnnotation.isPrimaryKey()) {
                    collectionToPrimaryCache.collectionToPrimary.put(collectionName,fieldName);
                }
                AddFieldReq milvusField = AddFieldReq.builder()
                        .fieldName(fieldName)
                        .dataType(fieldAnnotation.dataType())
                        .isPrimaryKey(fieldAnnotation.isPrimaryKey())
                        .autoID(fieldAnnotation.autoID())
                        .description(StringUtils.isNotEmpty(fieldAnnotation.description()) ? fieldAnnotation.description() : null)
                        .dimension(fieldAnnotation.dimension())
                        .maxLength(fieldAnnotation.maxLength() > 0 ? fieldAnnotation.maxLength() : null)
                        .isPartitionKey(fieldAnnotation.isPartitionKey())
                        .elementType(fieldAnnotation.elementType())
                        .maxCapacity(fieldAnnotation.maxCapacity() > 0 ? fieldAnnotation.maxCapacity() : null)
                        .build();
                milvusFields.add(milvusField);
                // 构建IndexParam对象
                IndexParam indexParam = createIndexParam(field,fieldName);
                if(indexParam!=null){
                    indexParams.add(indexParam);
                }
            }
        }
        milvus.setMilvusFields(milvusFields);
        milvus.setIndexParams(indexParams);
        //缓存
        ConversionCache conversionCache=new ConversionCache<>();
        conversionCache.setMilvusEntity(milvus);
        conversionCache.setCollectionName(collectionName);
        conversionCache.setPropertyCache(propertyCache);
        conversionCache.setCollectionToPrimaryCache(collectionToPrimaryCache);
        MilvusCache.milvusCache.put(entityClass,conversionCache);
        return milvus;
    }


    private static IndexParam createIndexParam(Field field,String fieldName) {
        MilvusIndex fieldAnnotation = field.getAnnotation(MilvusIndex.class);
        if (fieldAnnotation == null) {
            return null;
        }
        return IndexParam.builder()
                .fieldName(fieldAnnotation.indexName().isEmpty() ? fieldName : fieldAnnotation.indexName())
                .indexType(fieldAnnotation.indexType())
                .metricType(fieldAnnotation.metricType()) // 默认使用L2距离，根据需要调整
                .build();
    }


}