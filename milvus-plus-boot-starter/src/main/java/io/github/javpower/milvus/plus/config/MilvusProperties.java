package io.github.javpower.milvus.plus.config;

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
    private String token;
    private List<String> packages;
}