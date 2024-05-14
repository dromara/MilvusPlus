package io.github.javpower.milvus.plus.mapper;

import io.github.javpower.milvus.plus.core.mapper.BaseMilvusMapper;
import io.github.javpower.milvus.plus.util.SpringUtils;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.stereotype.Component;

@Component
public class MilvusMapper<T> extends BaseMilvusMapper<T> {

    @Override
    public MilvusClientV2 getClient() {
        return SpringUtils.getBean(MilvusClientV2.class);
    }
}
