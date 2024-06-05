package org.dromara.milvus.plus.service;

import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.collection.response.GetCollectionStatsResp;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.index.request.DropIndexReq;
import io.milvus.v2.service.index.response.DescribeIndexResp;
import io.milvus.v2.service.partition.request.*;
import io.milvus.v2.service.utility.request.AlterAliasReq;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import io.milvus.v2.service.utility.request.DropAliasReq;
import io.milvus.v2.service.utility.request.ListAliasesReq;
import io.milvus.v2.service.utility.response.ListAliasResp;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.builder.CollectionSchemaBuilder;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.core.FieldFunction;
import org.dromara.milvus.plus.model.MilvusEntity;

import java.util.Collections;
import java.util.List;

public interface ICMService {

    MilvusClientV2 getClient();

    default void createCollection(Class<?> milvusClass){
        MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
        createCollection(milvusEntity);
    }

    /**
     * 创建集合
     * @param milvusEntity
     */
    default void createCollection(MilvusEntity milvusEntity){
        MilvusClientV2 client = getClient();
        try {
            String collectionName = milvusEntity.getCollectionName();
            // 检查集合是否存在
            boolean collectionExists = client.hasCollection(
                    HasCollectionReq.builder().collectionName(collectionName).build()
            );
            if (!collectionExists) {
                // 创建新集合
                MilvusConverter.create(milvusEntity,client);
            }
            //加载集合
            MilvusConverter.loadStatus(milvusEntity,client);
        } catch (MilvusException e) {
            throw new RuntimeException("Error handling Milvus collection", e);
        }
    }
    /**
     * 添加字段到集合
     * @param collectionName 集合名称
     * @param addFieldReq 字段请求参数
     */
    default void addField(String collectionName,AddFieldReq ... addFieldReq){
        MilvusClientV2 client = getClient();
        // 创建新集合
        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                collectionName, client
        );
        for (AddFieldReq fieldReq : addFieldReq) {
            schemaBuilder.addField(fieldReq);
        }
    }
    /**
     * 获取集合中的特定字段
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @return 字段架构信息
     */
    default CreateCollectionReq.FieldSchema getField(String collectionName,String fieldName){
        MilvusClientV2 client = getClient();
        // 创建新集合
        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                collectionName, client
        );
        return schemaBuilder.getField(fieldName);
    }

    /**
     * 根据字段功能对象获取集合中的特定字段
     * @param collectionName 字段功能对象，代表集合名称
     * @param fieldName 字段功能对象，代表字段名称
     * @return 字段架构信息
     */
    default CreateCollectionReq.FieldSchema getField(FieldFunction collectionName,FieldFunction fieldName){
        String fieldName1 = fieldName.getFieldName(fieldName);
        String collectionName1 = collectionName.getFieldName(collectionName);
        return getField(collectionName1,fieldName1);
    }

    /**
     * 获取集合的详细信息
     * @param collectionName 集合名称
     * @return 集合详细信息响应对象
     * @throws MilvusException 如果操作过程中发生错误
     */
    default DescribeCollectionResp describeCollection(String collectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        DescribeCollectionReq describeCollectionReq = DescribeCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        return client.describeCollection(describeCollectionReq);
    }
    /**
     * 删除集合
     * @param collectionName 要删除的集合名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void dropCollection(String collectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
                .collectionName(collectionName)
                .build();
        client.dropCollection(dropCollectionReq);

        // 检查集合是否已被删除
        boolean isDropped = hasCollection(collectionName);
        if (!isDropped) {
            throw new RuntimeException("Failed to drop collection: " + collectionName);
        }
    }

    /**
     * 检查集合是否存在
     * @param collectionName 集合名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default boolean hasCollection(String collectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        // 检查集合是否已被删除
        boolean hasCollection = client.hasCollection(
                HasCollectionReq.builder()
                        .collectionName(collectionName)
                        .build()
        );
        return hasCollection;
    }
    /**
     * 获取集合的统计信息
     * @param collectionName 集合名称
     * @return 集合统计信息响应对象
     * @throws MilvusException 如果操作过程中发生错误
     */
    default GetCollectionStatsResp getCollectionStats(String collectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        GetCollectionStatsReq getCollectionStatsReq = GetCollectionStatsReq.builder()
                .collectionName(collectionName)
                .build();
        return client.getCollectionStats(getCollectionStatsReq);
    }
    /**
     * 重命名集合
     * @param oldCollectionName 原始集合名称
     * @param newCollectionName 新集合名称
     * @throws MilvusException 如果操作过程中发生错误，例如集合不存在或新名称与旧名称相同
     */
    default void renameCollection(String oldCollectionName, String newCollectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        RenameCollectionReq renameCollectionReq = RenameCollectionReq.builder()
                .collectionName(oldCollectionName)
                .newCollectionName(newCollectionName)
                .build();
        client.renameCollection(renameCollectionReq);
    }
    /**
     * 为集合创建索引
     * @param collectionName 集合名称
     * @param indexParams 索引参数列表，包含要创建的索引的详细信息
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void createIndex(String collectionName, List<IndexParam> indexParams) throws MilvusException {
        MilvusClientV2 client = getClient();
        CreateIndexReq createIndexReq = CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(indexParams)
                .build();
        client.createIndex(createIndexReq);
    }

    /**
     * 为集合中的特定字段创建索引
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @param metricType 度量类型，例如 L2 或 IP
     * @param indexType 索引类型，例如 AUTOINDEX
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void createIndex(String collectionName, String fieldName, IndexParam.MetricType metricType, IndexParam.IndexType indexType) throws MilvusException {
        IndexParam indexParam = IndexParam.builder()
                .metricType(metricType)
                .indexType(indexType)
                .fieldName(fieldName)
                .build();
        createIndex(collectionName, Collections.singletonList(indexParam));
    }
    /**
     * 获取指定集合中指定字段的索引信息
     * @param collectionName 集合名称
     * @param fieldName 字段名称
     * @return 索引信息响应对象，包含索引的详细信息
     * @throws MilvusException 如果操作过程中发生错误，例如集合或字段不存在
     */
    default DescribeIndexResp describeIndex(String collectionName, String fieldName) throws MilvusException {
        MilvusClientV2 client = getClient();
        DescribeIndexReq describeIndexReq = DescribeIndexReq.builder()
                .collectionName(collectionName)
                .fieldName(fieldName)
                .build();
        return client.describeIndex(describeIndexReq);
    }
    default DescribeIndexResp describeIndex(String collectionName, FieldFunction fieldName) throws MilvusException {
        return describeIndex(collectionName,fieldName.getFieldName(fieldName));
    }
    /**
     * 从指定集合中的指定字段删除索引
     * @param collectionName 集合名称
     * @param fieldName 字段名称，索引将从该字段删除
     * @throws MilvusException 如果操作过程中发生错误，例如集合或字段不存在
     */
    default void dropIndex(String collectionName, String fieldName) throws MilvusException {
        MilvusClientV2 client = getClient();
        DropIndexReq dropIndexReq = DropIndexReq.builder()
                .collectionName(collectionName)
                .fieldName(fieldName)
                .build();
        client.dropIndex(dropIndexReq);
    }
    /**
     * 获取指定集合或分区的加载状态
     * @param collectionName 集合名称
     * @param partitionName 分区名称，如果为空，则检查整个集合的加载状态
     * @return 布尔值，指示指定集合或分区是否已加载
     * @throws MilvusException 如果操作过程中发生错误
     */
    default Boolean getLoadState(String collectionName, String partitionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        GetLoadStateReq.GetLoadStateReqBuilder<?, ?> builder = GetLoadStateReq.builder()
                .collectionName(collectionName);
        if(StringUtils.isNotEmpty(partitionName)){
            builder.partitionName(partitionName);
        }
        GetLoadStateReq getLoadStateReq = builder.build();
        return client.getLoadState(getLoadStateReq);
    }

    /**
     * 获取指定集合的加载状态，忽略分区
     * @param collectionName 集合名称
     * @return 布尔值，指示指定集合是否已加载
     * @throws MilvusException 如果操作过程中发生错误
     */
    default Boolean isCollectionLoaded(String collectionName) throws MilvusException {
        return getLoadState(collectionName, null);
    }
    /**
     * 加载指定集合的数据到内存中
     * @param collectionName 要加载的集合名称
     * @param numReplicas 创建的副本数量，默认为1
     * @param async 是否异步执行，默认为true，表示操作可能在后台运行
     * @param timeout 操作的超时时间，默认为60000毫秒（1分钟）
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void loadCollection(
            String collectionName,
            Integer numReplicas,
            Boolean async,
            Long timeout
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                .collectionName(collectionName)
                .numReplicas(numReplicas)
                .async(async)
                .timeout(timeout)
                .build();
        client.loadCollection(loadCollectionReq);
    }

    /**
     * 加载指定集合的数据到内存中，使用默认参数
     * @param collectionName 要加载的集合名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void loadCollection(String collectionName) throws MilvusException {
        loadCollection(collectionName, 1, true, 60000L);
    }
    /**
     * 从内存中释放指定集合的数据
     * @param collectionName 要释放的集合名称
     * @param async 是否异步执行，默认为true，表示操作可能在后台运行
     * @param timeout 操作的超时时间，默认为60000毫秒（1分钟）
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void releaseCollection(
            String collectionName,
            Boolean async,
            Long timeout
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
                .collectionName(collectionName)
                .async(async)
                .timeout(timeout)
                .build();
        client.releaseCollection(releaseCollectionReq);
    }

    /**
     * 从内存中释放指定集合的数据，使用默认参数
     * @param collectionName 要释放的集合名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void releaseCollection(String collectionName) throws MilvusException {
        releaseCollection(collectionName, true, 60000L);
    }
    /**
     * 在指定集合中创建分区
     * @param collectionName 集合名称
     * @param partitionName 要创建的分区名称
     * @throws MilvusException 如果操作过程中发生错误，例如集合不存在或分区名称已存在
     */
    default void createPartition(String collectionName, String partitionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.createPartition(createPartitionReq);
    }
    /**
     * 从当前集合中删除指定的分区
     * @param collectionName 集合名称
     * @param partitionName 要删除的分区名称
     * @throws MilvusException 如果操作过程中发生错误，例如集合或分区不存在
     */
    default void dropPartition(String collectionName, String partitionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        DropPartitionReq dropPartitionReq = DropPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        client.dropPartition(dropPartitionReq);
    }
    /**
     * 检查指定的分区是否存在于指定的集合中
     * @param collectionName 集合名称
     * @param partitionName 分区名称
     * @return 布尔值，如果分区存在返回true，否则返回false
     * @throws MilvusException 如果操作过程中发生错误
     */
    default Boolean hasPartition(String collectionName, String partitionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        HasPartitionReq hasPartitionReq = HasPartitionReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .build();
        return client.hasPartition(hasPartitionReq);
    }
    /**
     * 列出指定集合中的所有分区
     * @param collectionName 集合名称
     * @return 分区名称的列表
     * @throws MilvusException 如果操作过程中发生错误
     */
    default List<String> listPartitions(String collectionName) throws MilvusException {
        MilvusClientV2 client = getClient();
        ListPartitionsReq listPartitionsReq = ListPartitionsReq.builder()
                .collectionName(collectionName)
                .build();
        return client.listPartitions(listPartitionsReq);
    }
    /**
     * 加载指定集合中的指定分区到内存
     * @param collectionName 集合名称
     * @param partitionNames 要加载的分区名称列表
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void loadPartitions(String collectionName, List<String> partitionNames) throws MilvusException {
        MilvusClientV2 client = getClient();
        LoadPartitionsReq loadPartitionsReq = LoadPartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNames)
                .build();
        client.loadPartitions(loadPartitionsReq);
    }

    /**
     * 便利方法，加载指定集合中的单个分区到内存
     * @param collectionName 集合名称
     * @param partitionName 要加载的单个分区名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void loadPartition(String collectionName, String partitionName) throws MilvusException {
        loadPartitions(collectionName, Collections.singletonList(partitionName));
    }
    /**
     * 从内存中释放指定集合中的指定分区
     * @param collectionName 集合名称
     * @param partitionNames 要释放的分区名称列表
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void releasePartitions(String collectionName, List<String> partitionNames) throws MilvusException {
        MilvusClientV2 client = getClient();
        ReleasePartitionsReq releasePartitionsReq = ReleasePartitionsReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNames)
                .build();
        client.releasePartitions(releasePartitionsReq);
    }

    /**
     * 便利方法，从内存中释放指定集合中的单个分区
     * @param collectionName 集合名称
     * @param partitionName 要释放的单个分区名称
     * @throws MilvusException 如果操作过程中发生错误
     */
    default void releasePartition(String collectionName, String partitionName) throws MilvusException {
        releasePartitions(collectionName, Collections.singletonList(partitionName));
    }

    /**
     * create aliases
     */
    default void createAlias(MilvusEntity milvusEntity) {
        MilvusClientV2 client = getClient();
        for (String alias : milvusEntity.getAlias()) {
            CreateAliasReq createAliasReq = CreateAliasReq.builder().alias(alias)
                    .collectionName(milvusEntity.getCollectionName()).build();
            client.createAlias(createAliasReq);
        }
    }

    default void createAlias(Class<?> milvusClass) {
        MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
        createAlias(milvusEntity);
    }

    /**
     * drop aliases
     */
    default void dropAlias(MilvusEntity milvusEntity) {
        MilvusClientV2 client = getClient();
        for (String alias : milvusEntity.getAlias()) {
            client.dropAlias(DropAliasReq.builder().alias(alias).build());
        }
    }

    /**
     * alter aliases
     */
    default void alterAlias(MilvusEntity milvusEntity) {
        MilvusClientV2 client = getClient();
        for (String alias : milvusEntity.getAlias()) {
            client.alterAlias(AlterAliasReq.builder()
                    .collectionName(milvusEntity.getCollectionName())
                    .alias(alias).build());
        }
    }

    /**
     * list aliases
     *
     * @return List<String> alias names
     */
    default ListAliasResp listAliases(MilvusEntity milvusEntity) {
        MilvusClientV2 client = getClient();
        return client.listAliases(ListAliasesReq.builder().collectionName(milvusEntity.getCollectionName()).build());
    }
}
