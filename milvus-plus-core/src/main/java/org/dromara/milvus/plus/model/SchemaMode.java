package org.dromara.milvus.plus.model;

/**
 * 启动/调用时集合结构同步策略。
 * <p>
 * Plus 的核心体验之一：实体注解即 schema，尽量减少人工维护集合结构。
 */
public enum SchemaMode {

    /**
     * 仅在集合不存在时创建；已存在则不做字段对比（历史默认行为）。
     */
    IGNORE,

    /**
     * 集合不存在则创建；已存在则对比字段，实体多出的字段导致启动/调用失败（fail-fast）。
     */
    VALIDATE,

    /**
     * 集合不存在则创建；已存在则自动 add 实体中缺失的字段（要求 nullable 或有默认值）。
     */
    AUTO_ADD,

    /**
     * 危险：删除后重建集合（仅建议开发环境）。需配置 enable-recreate=true 才会生效。
     */
    RECREATE
}
