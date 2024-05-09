package io.github.javpower.milvus.plus.service;


import io.milvus.v2.client.MilvusClientV2;
import org.springframework.stereotype.Service;
/**
 * @author xgc
 **/
@Service
public class MilvusClient implements AutoCloseable {

    public final MilvusClientV2 client;

    public MilvusClient(MilvusClientV2 client) {
        this.client = client;
    }

    @Override
    public void close() throws InterruptedException {
        client.close(10);
    }
}