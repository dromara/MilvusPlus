package org.dromara.milvus.plus.mapper;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.core.mapper.BaseMilvusMapper;
import org.dromara.milvus.plus.util.MilvusSpringUtils;


public class MilvusMapper<T> extends BaseMilvusMapper<T> {

    @Override
    public MilvusClientV2 getClient() {
        return MilvusSpringUtils.getBean(MilvusClientV2.class);
    }
}
