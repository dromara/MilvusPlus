package org.dromara.solon.service;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.service.IAMService;
import org.dromara.milvus.plus.service.ICMService;
import org.dromara.milvus.plus.service.IVecMService;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;

@Component
public class MilvusService implements IAMService, ICMService, IVecMService {
    @Override
    public MilvusClientV2 getClient() {
        return Solon.context().getBean(MilvusClientV2.class);
    }
}
