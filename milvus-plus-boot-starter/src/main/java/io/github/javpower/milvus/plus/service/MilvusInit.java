package io.github.javpower.milvus.plus.service;

import io.github.javpower.milvus.plus.entity.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class MilvusInit extends AbstractMilvusClientBuilder {

    @Autowired
    private MilvusProperties milvusProperties;
    private MilvusClientV2 client;

    // Spring会调用这个方法来初始化client
    @PostConstruct
    public void initialize() {
        io.github.javpower.milvus.plus.model.MilvusProperties milvusProperties1=new io.github.javpower.milvus.plus.model.MilvusProperties();
        BeanUtils.copyProperties(milvusProperties,milvusProperties1);
        super.setProperties(milvusProperties1);
        super.initialize();
        client = getClient();
    }

    // Spring会调用这个方法来关闭client
    @PreDestroy
    public void close() throws InterruptedException {
        super.close();
    }

    @Bean
    public MilvusClientV2 milvusClientV2() {
        return client;
    }

}