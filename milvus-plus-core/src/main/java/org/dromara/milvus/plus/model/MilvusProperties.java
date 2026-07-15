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
    private boolean openLog;
    private String logLevel;
    private boolean banner = true;

    /**
     * 集合结构同步策略，默认 IGNORE（兼容历史行为）。
     * 推荐新项目使用 AUTO_ADD。
     */
    private SchemaMode schemaMode = SchemaMode.IGNORE;

    /**
     * 是否允许 schema-mode=RECREATE 时删表重建。生产务必 false。
     */
    private boolean enableRecreate = false;
}
