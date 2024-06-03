package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;

public interface MilvusClientBuilder {
    /**
     * 初始化
     */
    void initialize();

    /**
     * 关闭客户端
     */
    void close() throws InterruptedException;

    /**
     * 获取milvus客户端
     *
     * @return MilvusClientV2
     */
    MilvusClientV2 getClient();
}