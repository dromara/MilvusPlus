package io.github.javpower.milvus.plus.service;


import io.github.javpower.milvus.plus.builder.CollectionSchemaBuilder;
import io.github.javpower.milvus.plus.converter.MilvusEntityConverter;
import io.github.javpower.milvus.plus.model.MilvusEntity;
import io.milvus.exception.MilvusException;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author xgc
 **/
@Service
public class MilvusCollectionService {
    private final MilvusClient milvusClient;

    public MilvusCollectionService(MilvusClient milvusClient) {
        this.milvusClient = milvusClient;
    }

    public void performBusinessLogic(List<Class<?>> annotatedClasses) {
        for (Class<?> milvusClass : annotatedClasses) {
            MilvusEntity milvusEntity = MilvusEntityConverter.convert(milvusClass);
            try {
                String collectionName = milvusEntity.getCollectionName();
                // 检查集合是否存在
                boolean collectionExists = milvusClient.client.hasCollection(
                        HasCollectionReq.builder().collectionName(collectionName).build()
                );
                if (collectionExists) {
                    // 获取集合的详细信息
                    DescribeCollectionResp collectionInfo = milvusClient.client.describeCollection(
                            DescribeCollectionReq.builder().collectionName(collectionName).build()
                    );
                    // 检查字段是否一致，这里需要实现字段比较逻辑
                    List<String> existingFieldNames = collectionInfo.getFieldNames();
                    List<AddFieldReq> requiredFields = milvusEntity.getMilvusFields();
                    List<String> requiredFieldNames = requiredFields.stream().map(AddFieldReq::getFieldName).collect(Collectors.toList());
                    if (!new HashSet<>(existingFieldNames).containsAll(requiredFieldNames) || !new HashSet<>(requiredFieldNames).containsAll(existingFieldNames)) {
                        // 字段不一致，删除并重新创建集合
                        milvusClient.client.dropCollection(
                                DropCollectionReq.builder().collectionName(collectionName).build()
                        );
                        // 创建新集合
                        create(milvusEntity);
                    }
                } else {
                    // 创建新集合
                    create(milvusEntity);
                }
            } catch (MilvusException e) {
                throw new RuntimeException("Error handling Milvus collection", e);
            }
        }
    }
    private void create(MilvusEntity milvusEntity){
        // 创建新集合
        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                milvusEntity.getCollectionName(), milvusClient
        );
        schemaBuilder.addField(milvusEntity.getMilvusFields().toArray(new AddFieldReq[0]));
        List<IndexParam> indexParams = milvusEntity.getIndexParams();
        schemaBuilder.createSchema();
        if (indexParams != null && !indexParams.isEmpty()) {
            schemaBuilder.createIndex(indexParams);
        }
    }
}