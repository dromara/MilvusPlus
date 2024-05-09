package io.github.javpower.milvus.plus.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author xgc
 **/
@Configuration
public class MilvusConfig {
    @Autowired
    private MilvusProperties properties;
    @Bean
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(properties.getUri())
                .token(properties.getToken())
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
