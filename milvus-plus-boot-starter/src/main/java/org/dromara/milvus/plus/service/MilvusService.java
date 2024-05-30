package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.util.SpringUtils;
import org.springframework.stereotype.Service;

@Service
public class MilvusService implements IAMService,ICMService,IVecMService{
    @Override
    public MilvusClientV2 getClient() {
        return SpringUtils.getBean(MilvusClientV2.class);
    }
}
