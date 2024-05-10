package io.github.javpower.milvus.plus.service;


import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * @author xgc
 **/
@Service
public class MilvusClient implements AutoCloseable {
    @Autowired(required = false)
    public MilvusClientV2 client;

    @Override
    public void close() throws InterruptedException {
        client.close(10);
    }
}