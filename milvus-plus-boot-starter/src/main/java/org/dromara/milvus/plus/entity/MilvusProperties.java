package org.dromara.milvus.plus.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author xgc
 **/
@Data
@ConfigurationProperties(prefix = "milvus")
@Component
public class MilvusProperties {
    private boolean enable;
    private String uri;
    private String dbName;
    private String username;
    private String password;
    private String token;
    private List<String> packages;
}