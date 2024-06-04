package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.config.MilvusPropertiesConfiguration;
import org.dromara.milvus.plus.model.MilvusProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class MilvusInit extends AbstractMilvusClientBuilder {

    @Autowired
    private MilvusPropertiesConfiguration milvusPropertiesConfiguration;

    private MilvusClientV2 client;

    // Spring会调用这个方法来初始化client
    @PostConstruct
    public void initialize() {
        MilvusProperties milvusProperties = new MilvusProperties();
        BeanUtils.copyProperties(milvusPropertiesConfiguration, milvusProperties);
        super.setProperties(milvusProperties);
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
        printBanner();
        return client;
    }

    public void printBanner() {
        String banner =
                "  __  __ _ _                    ____  _           \n" +
                        " |  \\/  (_) |_   ___   _ ___   |  _ \\| |_   _ ___ \n" +
                        " | |\\/| | | \\ \\ / / | | / __|  | |_) | | | | / __|\n" +
                        " | |  | | | |\\ V /| |_| \\__ \\  |  __/| | |_| \\__ \\\n" +
                        " |_|  |_|_|_| \\_/  \\__,_|___/  |_|   |_|\\__,_|___/\n\n";
        System.out.println(banner);
    }
}