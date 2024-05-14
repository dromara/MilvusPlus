package io.github.javpower.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;

public interface MilvusClientBuilder {
    void initialize();
    void close() throws InterruptedException;
    MilvusClientV2 getClient();
}