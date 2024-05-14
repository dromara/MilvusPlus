package io.github.javpower.solon.entity;



import lombok.Data;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * @author xgc
 **/
@Data
@Component
public class MilvusProperties {
    @Inject("${milvus.enable}")
    private boolean enable;
    @Inject("${milvus.uri}")
    private String uri;
    @Inject("${milvus.token}")
    private String token;
    @Inject("${milvus.packages}")
    private List<String> packages;
}