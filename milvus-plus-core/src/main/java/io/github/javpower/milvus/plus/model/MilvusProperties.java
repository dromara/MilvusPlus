package io.github.javpower.milvus.plus.model;

import lombok.Data;

import java.util.List;

/**
 * @author xgc
 **/
@Data
public class MilvusProperties {
    private boolean enable;
    private String uri;
    private String token;
    private List<String> packages;
}