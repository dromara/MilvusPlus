package org.dromara.milvus.plus.service;

import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.model.MilvusEntity;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.HasCollectionReq;

public interface ICMService {

    MilvusClientV2 getClient();

    /**
     * 创建集合
     * @param milvusClass 集合实体类CLASS
     */
    default void createCollection(Class<?> milvusClass){
        MilvusClientV2 client = getClient();
        MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
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

}
