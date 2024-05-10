package io.github.javpower.milvus.plus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
/**
 * @author xgc
 **/
@Data
@ConfigurationProperties(prefix = "milvus")
@Component
public class MilvusProperties {

    private Boolean enable;
    private String uri;
    private String token;
}