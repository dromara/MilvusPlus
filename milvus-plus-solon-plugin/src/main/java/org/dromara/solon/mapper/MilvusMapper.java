package org.dromara.solon.mapper;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.core.mapper.BaseMilvusMapper;
import org.noear.solon.Solon;

public class MilvusMapper<T> extends BaseMilvusMapper<T> {
    @Override
    public MilvusClientV2 getClient() {
        return Solon.context().getBean(MilvusClientV2.class);
    }
}
