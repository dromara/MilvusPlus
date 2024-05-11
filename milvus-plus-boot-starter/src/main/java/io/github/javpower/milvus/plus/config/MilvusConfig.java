package io.github.javpower.milvus.plus.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xgc
 **/
@Configuration
public class MilvusConfig {

    private final MilvusProperties properties;

    public MilvusConfig(MilvusProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MilvusClientV2 milvusClientV2() {
        if(!properties.isEnable()){
            return null;
        }
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(properties.getUri())
               // .token(properties.getToken())
                .build();
        return new MilvusClientV2(connectConfig);
    }

}
