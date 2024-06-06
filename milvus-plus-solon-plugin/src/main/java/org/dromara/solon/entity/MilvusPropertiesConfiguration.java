package org.dromara.solon.entity;

import lombok.Data;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * @author xgc
 **/
@Data
@Inject("${milvus}") //see https://solon.noear.org/article/326
@Configuration
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
}