package io.github.javpower.milvus.plus.service.impl;

import io.github.javpower.milvus.plus.model.MilvusProperties;
import io.github.javpower.milvus.plus.service.AbstractMilvusClientBuilder;

public class MilvusClientBuild extends AbstractMilvusClientBuilder {

    public MilvusClientBuild(MilvusProperties properties) {
        super.setProperties(properties);
    }

}