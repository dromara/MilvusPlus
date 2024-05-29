package org.dromara.milvus.plus.model;

import lombok.Data;

import java.util.List;

/**
 * @author xgc
 **/
@Data
public class MilvusProperties {
    private boolean enable;
    private String uri;
    private String dbName;
    private String username;
    private String password;
    private String token;
    private List<String> packages;
}