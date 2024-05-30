package org.dromara.milvus.plus.service.impl;

import org.dromara.milvus.plus.model.MilvusProperties;
import org.dromara.milvus.plus.service.AbstractMilvusClientBuilder;

public class MilvusClientBuild extends AbstractMilvusClientBuilder {

    public MilvusClientBuild(MilvusProperties properties) {
        super.setProperties(properties);
    }

}