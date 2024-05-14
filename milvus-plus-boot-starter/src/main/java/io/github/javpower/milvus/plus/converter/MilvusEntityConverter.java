package io.github.javpower.milvus.plus.converter;


import io.github.javpower.milvus.plus.annotation.ExtraParam;
import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.annotation.MilvusField;
import io.github.javpower.milvus.plus.annotation.MilvusIndex;
import io.github.javpower.milvus.plus.cache.CollectionToPrimaryCache;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.model.MilvusEntity;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xgc
 **/
public class MilvusEntityConverter {

    /**
     * 将Java实体类转换为MilvusEntity对象。
     * 该过程涉及读取实体类上的@MilvusCollection和@MilvusField注解，根据注解信息构建MilvusEntity对象，
     * 同时缓存有关实体类与字段的映射信息和索引参数，以便后续使用。
     *
     * @param entityClass 需要转换的Java实体类的Class对象。
     * @return 转换后的MilvusEntity对象，包含了Milvus表的结构信息和索引参数。
     * @throws IllegalArgumentException 如果实体类没有@MilvusCollection注解，则抛出异常。
     */
    public static MilvusEntity convert(Class<?> entityClass) {
        MilvusEntity milvus=new MilvusEntity();
        // 获取实体类上的@MilvusCollection注解，校验其存在性
        MilvusCollection collectionAnnotation = entityClass.getAnnotation(MilvusCollection.class);
        if (collectionAnnotation == null) {
            throw new IllegalArgumentException("Entity must be annotated with @MilvusCollection");
        }
        // 从注解中读取集合（表）名称
        String collectionName = collectionAnnotation.name();
        milvus.setCollectionName(collectionName);

        // 初始化字段列表和索引参数列表
        List<AddFieldReq> milvusFields = new ArrayList<>();
        List<IndexParam> indexParams=new ArrayList<>();
        // 用于存储属性与函数映射的缓存
        PropertyCache propertyCache=new PropertyCache();

        // 遍历实体类的所有字段，读取@MilvusField注解信息
        for (Field field : entityClass.getDeclaredFields()) {
            MilvusField fieldAnnotation = field.getAnnotation(MilvusField.class);
            if (fieldAnnotation != null) {
                // 处理字段名，优先使用注解中的字段名，若无则用反射获取的字段名
                String fieldName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
                // 缓存属性名与函数名的映射
                propertyCache.functionToPropertyMap.put(field.getName(),fieldName);
                propertyCache.methodToPropertyMap.put(getGetMethodName(field),fieldName);
                // 处理主键字段
                if (fieldAnnotation.isPrimaryKey()) {
                    CollectionToPrimaryCache.collectionToPrimary.put(collectionName,fieldName);
                }
                // 构建Milvus字段描述
                AddFieldReq.AddFieldReqBuilder<?, ?> builder = AddFieldReq.builder()
                        .fieldName(fieldName)
                        .dataType(fieldAnnotation.dataType())
                        .isPrimaryKey(fieldAnnotation.isPrimaryKey())
                        .isPartitionKey(fieldAnnotation.isPartitionKey())
                        .elementType(fieldAnnotation.elementType())
                        .autoID(fieldAnnotation.autoID());
                // 如果存在描述，则添加
                if(StringUtils.isNotEmpty(fieldAnnotation.description())){
                    builder.description(fieldAnnotation.description());
                }
                // 处理向量字段的维度、数组字段的最大长度、hash表的最大容量
                if(fieldAnnotation.dimension()>0){
                    builder.dimension(fieldAnnotation.dimension());
                }
                if(fieldAnnotation.maxLength() > 0){
                    builder.maxLength(fieldAnnotation.maxLength());
                }
                if(fieldAnnotation.maxCapacity() > 0){
                    builder.maxCapacity(fieldAnnotation.maxCapacity());
                }
                // 构建字段对象并添加到列表
                AddFieldReq milvusField = builder.build();
                milvusFields.add(milvusField);
                // 根据字段信息构建索引参数对象
                IndexParam indexParam = createIndexParam(field,fieldName);
                if(indexParam!=null){
                    indexParams.add(indexParam);
                }
            }
        }
        // 设置Milvus字段和索引参数
        milvus.setMilvusFields(milvusFields);
        milvus.setIndexParams(indexParams);

        // 缓存转换结果和集合信息
        ConversionCache conversionCache=new ConversionCache();
        conversionCache.setMilvusEntity(milvus);
        conversionCache.setCollectionName(collectionName);
        conversionCache.setPropertyCache(propertyCache);
        MilvusCache.milvusCache.put(entityClass.getName(),conversionCache);

        return milvus;
    }


    /**
     * 根据字段信息和字段名称创建索引参数对象。
     *
     * @param field 字段对象，需要包含MilvusIndex注解以用于索引配置。
     * @param fieldName 字段名称，用于构建索引参数时作为名称备用。
     * @return IndexParam 索引参数对象，如果字段没有MilvusIndex注解，则返回null。
     */
    private static IndexParam createIndexParam(Field field,String fieldName) {
        // 尝试获取字段上的MilvusIndex注解，用于后续索引参数的构建
        MilvusIndex fieldAnnotation = field.getAnnotation(MilvusIndex.class);
        if (fieldAnnotation == null) {
            return null;
        }

        // 初始化额外参数映射，用于存储由extraParams注解提供的额外参数
        Map<String,Object> map=new HashMap<>();
        ExtraParam[] extraParams = fieldAnnotation.extraParams();
        for (ExtraParam extraParam : extraParams) {
            map.put(extraParam.key(),extraParam.value());
        }

        // 使用收集到的参数构建索引参数对象
        return IndexParam.builder()
                .indexName(fieldAnnotation.indexName().isEmpty() ? fieldName : fieldAnnotation.indexName())
                .fieldName(fieldName)
                .indexType(fieldAnnotation.indexType())
                .metricType(fieldAnnotation.metricType()) // 默认使用L2距离，根据需要调整
                .extraParams(map)
                .build();
    }
    public static String getGetMethodName(Field field) {
        // 检查字段是否为布尔类型
        String prefix = field.getType() == boolean.class || field.getType() == Boolean.class ? "is" : "get";

        // 确保字段名不为空或null
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null.");
        }
        // 获取字段名的首字母大写形式
        String fieldName = capitalizeFirstLetter(field.getName());
        // 构建并返回getter方法名
        return prefix + fieldName;
    }
    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }


}
