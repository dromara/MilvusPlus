package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.CreateAliasReq;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.model.MilvusEntity;

/**
 * 集合别名
 *
 * @author code2tan
 */
public interface ICAliasService {
    MilvusClientV2 getClient();


    default void createAlias(MilvusEntity milvusEntity) {
        MilvusClientV2 client = getClient();
        CreateAliasReq createAliasReq = CreateAliasReq.builder().alias(milvusEntity.getAlias())
                .collectionName(milvusEntity.getCollectionName()).build();
        client.createAlias(createAliasReq);
    }

    default void createAlias(Class<?> milvusClass) {
        MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
        createAlias(milvusEntity);
    }
}
