package org.dromara.milvus.plus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xgc
 **/
@Data
@Component
@ConfigurationProperties(prefix = "milvus")
public class MilvusPropertiesConfiguration {
    private boolean enable;
    private String uri;
    private String dbName;
    private String username;
    private String password;
    private String token;
    private List<String> packages;
}