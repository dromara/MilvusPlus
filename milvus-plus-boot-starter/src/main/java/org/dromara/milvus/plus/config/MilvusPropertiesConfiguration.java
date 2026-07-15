package org.dromara.milvus.plus.config;

import lombok.Data;
import org.dromara.milvus.plus.model.SchemaMode;
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
    private boolean openLog;
    private String logLevel;
    private boolean banner = true;
    /**
     * 集合结构同步：IGNORE / VALIDATE / AUTO_ADD / RECREATE
     */
    private SchemaMode schemaMode = SchemaMode.IGNORE;
    /**
     * 是否允许 RECREATE 删表重建
     */
    private boolean enableRecreate = false;
}