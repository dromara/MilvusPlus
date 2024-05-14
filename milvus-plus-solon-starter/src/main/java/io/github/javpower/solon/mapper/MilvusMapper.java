package io.github.javpower.solon.mapper;

import io.github.javpower.milvus.plus.core.mapper.BaseMilvusMapper;
import io.github.javpower.solon.service.MilvusInit;
import io.milvus.v2.client.MilvusClientV2;


public class MilvusMapper<T> extends BaseMilvusMapper<T> {

    @Override
    public MilvusClientV2 getClient() {
        return MilvusInit.client;
    }
}
