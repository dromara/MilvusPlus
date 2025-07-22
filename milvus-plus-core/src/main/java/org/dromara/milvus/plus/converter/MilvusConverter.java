package org.dromara.milvus.plus.converter;


import com.google.common.collect.Lists;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.partition.request.HasPartitionReq;
import io.milvus.v2.service.partition.request.LoadPartitionsReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.annotation.*;
import org.dromara.milvus.plus.builder.CollectionSchemaBuilder;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.cache.ConversionCache;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.cache.PropertyCache;
import org.dromara.milvus.plus.model.MilvusEntity;
import org.dromara.milvus.plus.util.AnalyzerParamsUtils;
import org.dromara.milvus.plus.util.GsonUtil;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author xgc
 **/
@Slf4j
public class MilvusConverter {

    /**
     * 将Java实体类转换为MilvusEntity对象。
     * 该过程涉及读取实体类上的@MilvusCollection和@MilvusField注解，根据注解信息构建MilvusEntity对象，
     * 同时缓存有关实体类与字段的映射信息和索引参数，以便后续使用。
     *
     * @param entityClass 需要转换的Java实体类的Class对象。
     * @return 转换后的MilvusEntity对象，包含了Milvus表的结构信息和索引参数。
     *
     * @throws IllegalArgumentException 如果实体类没有@MilvusCollection注解，则抛出异常。
     */
    public static MilvusEntity convert(Class<?> entityClass) {
        ConversionCache cache = MilvusCache.milvusCache.get(entityClass.getName());
        if (Objects.nonNull(cache)) {
            return cache.getMilvusEntity();
        }
        MilvusEntity milvus = new MilvusEntity();
        boolean autoID=false;
        // 集合名称
        MilvusCollection collectionAnnotation = entityClass.getAnnotation(MilvusCollection.class);
        if (Objects.isNull(collectionAnnotation)) {
            throw new IllegalArgumentException("Entity must be annotated with @MilvusCollection");
        }
        // 分区信息
        MilvusPartition milvusPartition = entityClass.getAnnotation(MilvusPartition.class);
        if (Objects.nonNull(milvusPartition)) {
            String[] name = milvusPartition.name();
            milvus.setPartitionName(Lists.newArrayList(name));
        } else {
            milvus.setPartitionName(Lists.newArrayList());
        }

        // 集合名称
        String collectionName = collectionAnnotation.name();
        milvus.setCollectionName(collectionName);
        boolean enableDynamicField = collectionAnnotation.enableDynamicField();
        milvus.setEnableDynamicField(enableDynamicField);
        //一致性级别
        ConsistencyLevel level = collectionAnnotation.level();
        milvus.setConsistencyLevel(level);
        // 集合别名
        if (collectionAnnotation.alias().length > 0) {
            milvus.setAlias(Arrays.asList(collectionAnnotation.alias()));
        }
        // 初始化字段列表和索引参数列表
        List<AddFieldReq> milvusFields = new ArrayList<>();
        List<IndexParam> indexParams = new ArrayList<>();
        // 用于存储属性与函数映射的缓存
        PropertyCache propertyCache = new PropertyCache();
        List<Field> fields = getAllFieldsFromClass(entityClass);
        List<CreateCollectionReq.Function> functions=new ArrayList<>();
        // 遍历实体类的所有字段，读取@MilvusField注解信息
        for (Field field : fields) {
            MilvusField fieldAnnotation = field.getAnnotation(MilvusField.class);
            if (Objects.isNull(fieldAnnotation)) {
                continue;
            }
            // 处理字段名，优先使用注解中的字段名，若无则用反射获取的字段名
            String fieldName = fieldAnnotation.name().isEmpty() ? field.getName() : fieldAnnotation.name();
            // 缓存属性名与函数名的映射
            propertyCache.functionToPropertyMap.put(field.getName(), fieldName);
            propertyCache.nullableToPropertyMap.put(field.getName(),fieldAnnotation.nullable());
            propertyCache.methodToPropertyMap.put(getGetMethodName(field), fieldName);
            // 处理主键字段
            if (fieldAnnotation.isPrimaryKey()) {
                CollectionToPrimaryCache.collectionToPrimary.put(collectionName, fieldName);
            }
            // 构建Milvus字段描述
            AddFieldReq.AddFieldReqBuilder<?, ?> builder = AddFieldReq.builder()
                    .fieldName(fieldName)
                    .dataType(fieldAnnotation.dataType())
                    .isPrimaryKey(fieldAnnotation.isPrimaryKey())
                    .isPartitionKey(fieldAnnotation.isPartitionKey())
                    .elementType(fieldAnnotation.elementType())
                    .enableAnalyzer(fieldAnnotation.enableAnalyzer())
                    .enableMatch(fieldAnnotation.enableMatch())
                    .isNullable(fieldAnnotation.nullable())
                    .autoID(false);
            autoID=autoID?autoID:fieldAnnotation.autoID();

            if(fieldAnnotation.enableAnalyzer()&&fieldAnnotation.dataType()==DataType.VarChar){
                Map<String, Object> analyzerParams = AnalyzerParamsUtils.convertToMap(fieldAnnotation.analyzerParams());
                log.info("-----------analyzerParams--------- \n"+ GsonUtil.toJson(analyzerParams));
                builder.analyzerParams(analyzerParams);
                //构建该文本对应的SPARSE_FLOAT_VECTOR向量字段
                AddFieldReq sparse = AddFieldReq.builder().fieldName(fieldName + "_sparse").dataType(DataType.SparseFloatVector).build();
                milvusFields.add(sparse);
                //构建索引
                IndexParam sparseIndex = IndexParam.builder()
                        .indexName(fieldName + "_sparse_index")
                        .fieldName(fieldName + "_sparse")
                        .indexType(IndexParam.IndexType.AUTOINDEX)
                        .metricType(IndexParam.MetricType.BM25)
                        .build();
                indexParams.add(sparseIndex);
                //定义一个函数，将文本转换为稀疏向量
                String funName = fieldName+"_bm25_emb";
                CreateCollectionReq.Function fun= CreateCollectionReq.Function.builder().
                        name(funName).
                        functionType(FunctionType.BM25).
                        inputFieldNames(Lists.newArrayList(fieldName)).
                        outputFieldNames(Lists.newArrayList(fieldName + "_sparse")).build();
                functions.add(fun);
            }
            // 描述
            Optional.of(fieldAnnotation.description())
                    .filter(StringUtils::isNotEmpty).ifPresent(builder::description);
            // 处理向量字段的维度
            Optional.of(fieldAnnotation.dimension())
                    .filter(dimension -> dimension > 0).ifPresent(
                            dimension -> {
                                builder.dimension(dimension);
//                               if (!isListFloat(field)) {
//                                   throw new IllegalArgumentException("Vector field type mismatch");
//                                }
                            } );
            // 数组字段的最大长度
            Optional.of(fieldAnnotation.maxLength())
                    .filter(maxLength -> maxLength > 0).ifPresent(builder::maxLength);
            // hash表的最大容量
            Optional.of(fieldAnnotation.maxCapacity())
                    .filter(maxCapacity -> maxCapacity > 0).ifPresent(builder::maxCapacity);
            // 构建字段对象并添加到列表
            milvusFields.add(builder.build());
            // 根据字段信息构建索引参数对象
            createIndexParam(field, fieldName).ifPresent(indexParams::add);
        }
        // 设置Milvus字段和索引参数
        milvus.setMilvusFields(milvusFields);
        milvus.setIndexParams(indexParams);
        milvus.setFunctions(functions);
        // 缓存转换结果和集合信息
        ConversionCache conversionCache = new ConversionCache();
        conversionCache.setMilvusEntity(milvus);
        conversionCache.setCollectionName(collectionName);
        conversionCache.setPropertyCache(propertyCache);
        conversionCache.setAutoID(autoID);
        MilvusCache.milvusCache.put(entityClass.getName(), conversionCache);

        return milvus;
    }
    /**
     * 递归获取类及其所有父类的所有字段。
     *
     * @param clazz 要检查的类。
     * @return 包含所有字段的列表。
     */
    public static List<Field> getAllFieldsFromClass(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        // 递归地获取字段直到Object类
        while (clazz != null && clazz != Object.class) {
            // 获取当前类的所有字段并添加到列表中
            fields.addAll(Stream.of(clazz.getDeclaredFields())
                    .peek(field -> field.setAccessible(true)) // 确保可以访问私有字段
                    .collect(Collectors.toList()));
            clazz = clazz.getSuperclass(); // 移动到父类
        }
        return fields;
    }

    /**
     * 根据字段信息和字段名称创建索引参数对象。
     *
     * @param field     字段对象，需要包含MilvusIndex注解以用于索引配置。
     * @param fieldName 字段名称，用于构建索引参数时作为名称备用。
     * @return IndexParam 索引参数对象，如果字段没有MilvusIndex注解，则返回null。
     */
    private static Optional<IndexParam> createIndexParam(Field field, String fieldName) {
        // 尝试获取字段上的MilvusIndex注解，用于后续索引参数的构建
        MilvusIndex fieldAnnotation = field.getAnnotation(MilvusIndex.class);
        if (fieldAnnotation == null) {
            return Optional.empty();
        }

        // 初始化额外参数映射，用于存储由extraParams注解提供的额外参数
        Map<String, Object> map = Optional.ofNullable(fieldAnnotation.extraParams())
                .map(Arrays::stream)
                .orElseGet(Stream::empty)
                .collect(Collectors.toMap(ExtraParam::key, ExtraParam::value, (old, current) -> current));

        // 构建索引参数对象
        IndexParam build = IndexParam.builder()
                .indexName(fieldAnnotation.indexName().isEmpty() ? fieldName : fieldAnnotation.indexName())
                .fieldName(fieldName)
                .indexType(fieldAnnotation.indexType())
                .metricType(fieldAnnotation.metricType())
                .extraParams(map)
                .build();
        return Optional.of(build);
    }

    public static String getGetMethodName(Field field) {
        // 确保字段名不为空或null
        if (field == null) {
            throw new IllegalArgumentException("Field must not be null.");
        }
        // 检查字段是否为布尔类型
        String prefix = field.getType() == boolean.class || field.getType() == Boolean.class ? "is" : "get";

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

    public static void create(MilvusEntity milvusEntity, MilvusClientV2 client) {
        List<IndexParam> indexParams = milvusEntity.getIndexParams();
        if (indexParams == null || indexParams.isEmpty()) {
            throw new IllegalArgumentException("the index does not exist, please define the index");
        }
        // 创建新集合
        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                milvusEntity.getEnableDynamicField(),milvusEntity.getCollectionName(), client
        );
        schemaBuilder.addField(milvusEntity.getMilvusFields().toArray(new AddFieldReq[0]));
        schemaBuilder.addConsistencyLevel(milvusEntity.getConsistencyLevel());
        schemaBuilder.addFun(milvusEntity.getFunctions());
        log.info("-------create schema---------");
        schemaBuilder.createSchema();
        log.info("-------create schema fun---------");
        schemaBuilder.createIndex(indexParams);
        log.info("-------create index---------");
        // 创建分区
        List<String> partitionName = milvusEntity.getPartitionName();
        if (CollectionUtils.isEmpty(partitionName)) {
            return;
        }
        for (String pn : partitionName) {
            CreatePartitionReq req = CreatePartitionReq.builder()
                    .collectionName(milvusEntity.getCollectionName())
                    .partitionName(pn)
                    .build();
            client.createPartition(req);
        }
    }

    public static void loadStatus(MilvusEntity milvusEntity, MilvusClientV2 client) {
        // 集合加载状态+加载集合
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
                .collectionName(milvusEntity.getCollectionName())
                .build();
        Boolean resp = client.getLoadState(getLoadStateReq);
        log.info("load collection state-->{}", resp);
        if (!resp) {
            LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                    .collectionName(milvusEntity.getCollectionName())
                    .build();
            client.loadCollection(loadCollectionReq);
            log.info("load collection--{}", milvusEntity.getCollectionName());
        }
        // 加载分区
        List<String> partitionName = milvusEntity.getPartitionName();
        if (CollectionUtils.isEmpty(partitionName)) {
            return;
        }
        for (String pn : partitionName) {
            HasPartitionReq hasPartitionReq = HasPartitionReq.builder()
                    .collectionName(milvusEntity.getCollectionName())
                    .partitionName(pn)
                    .build();
            Boolean hasPartition = client.hasPartition(hasPartitionReq);
            log.info("has partition -->{}--{}", pn, hasPartition);
            if (!hasPartition) {
                // 创建分区
                CreatePartitionReq req = CreatePartitionReq.builder()
                        .collectionName(milvusEntity.getCollectionName())
                        .partitionName(pn)
                        .build();
                client.createPartition(req);
                log.info("create partition -->{}", pn);
            }
        }
        // 加载分区
        LoadPartitionsReq loadPartitionsReq = LoadPartitionsReq.builder()
                .collectionName(milvusEntity.getCollectionName())
                .partitionNames(partitionName)
                .build();
        client.loadPartitions(loadPartitionsReq);
        log.info("load partition--{}", milvusEntity.getPartitionName());
    }
    /**
     * 判断字段是否是 List<Float> 类型。
     *
     * @param field 要检查的字段
     * @return 如果字段是 List<Float> 类型返回 true，否则返回 false
     */
    public static boolean isListFloat(Field field) {
        // 确保字段不是 null
        if (field == null) {
            return false;
        }
        // 获取字段的泛型类型
        Type genericType = field.getGenericType();

        // 检查是否是参数化类型
        if (!(genericType instanceof ParameterizedType)) {
            return false;
        }
        // 类型转换
        ParameterizedType parameterizedType = (ParameterizedType) genericType;
        // 获取参数化类型的原始类型
        Type rawType = parameterizedType.getRawType();

        // 检查原始类型是否是 List.class
        if (!(rawType instanceof Class) || !List.class.isAssignableFrom((Class<?>) rawType)) {
            return false;
        }
        // 获取类型参数
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        // 检查类型参数是否正好有一个，并且是 Float.TYPE
        return typeArguments.length == 1 && typeArguments[0] == Float.class;
    }
}
