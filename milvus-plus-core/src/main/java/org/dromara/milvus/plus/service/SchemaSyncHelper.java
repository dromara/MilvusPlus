package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.exception.MilvusPlusException;
import org.dromara.milvus.plus.model.MilvusEntity;
import org.dromara.milvus.plus.model.SchemaMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 集合结构同步：把“实体注解”与“服务端 collection”对齐。
 */
@Slf4j
public final class SchemaSyncHelper {

    private SchemaSyncHelper() {
    }

    public static void sync(MilvusEntity entity, MilvusClientV2 client, SchemaMode mode, boolean enableRecreate) {
        if (entity == null || client == null) {
            return;
        }
        SchemaMode effective = mode == null ? SchemaMode.IGNORE : mode;
        String collectionName = entity.getCollectionName();
        boolean exists = client.hasCollection(HasCollectionReq.builder().collectionName(collectionName).build());

        if (!exists) {
            log.info("Collection [{}] not exists, create by entity schema", collectionName);
            MilvusConverter.create(entity, client);
            MilvusConverter.loadStatus(entity, client);
            return;
        }

        switch (effective) {
            case IGNORE:
                MilvusConverter.loadStatus(entity, client);
                return;
            case VALIDATE:
                validate(entity, client);
                MilvusConverter.loadStatus(entity, client);
                return;
            case AUTO_ADD:
                autoAdd(entity, client);
                MilvusConverter.loadStatus(entity, client);
                return;
            case RECREATE:
                if (!enableRecreate) {
                    throw MilvusPlusException.of("SCHEMA_RECREATE_DISABLED",
                            "schema-mode=RECREATE but milvus.enable-recreate is false. " +
                                    "Set milvus.enable-recreate=true only in non-production environments.");
                }
                log.warn("Dropping and recreating collection [{}] because schema-mode=RECREATE", collectionName);
                client.dropCollection(DropCollectionReq.builder().collectionName(collectionName).build());
                MilvusConverter.create(entity, client);
                MilvusConverter.loadStatus(entity, client);
                return;
            default:
                MilvusConverter.loadStatus(entity, client);
        }
    }

    public static List<String> missingFieldNames(MilvusEntity entity, DescribeCollectionResp describe) {
        Set<String> serverFields = new HashSet<>();
        if (describe != null && describe.getFieldNames() != null) {
            serverFields.addAll(describe.getFieldNames());
        }
        // BM25 自动生成的 sparse 字段也算在实体侧
        List<String> missing = new ArrayList<>();
        if (entity.getMilvusFields() == null) {
            return missing;
        }
        for (AddFieldReq field : entity.getMilvusFields()) {
            if (field == null || field.getFieldName() == null) {
                continue;
            }
            if (!serverFields.contains(field.getFieldName())) {
                missing.add(field.getFieldName());
            }
        }
        return missing;
    }

    private static void validate(MilvusEntity entity, MilvusClientV2 client) {
        DescribeCollectionResp describe = describe(entity.getCollectionName(), client);
        List<String> missing = missingFieldNames(entity, describe);
        if (!missing.isEmpty()) {
            throw MilvusPlusException.of("SCHEMA_MISMATCH",
                    "Collection [" + entity.getCollectionName() + "] missing fields vs entity: " + missing +
                            ". Use schema-mode=AUTO_ADD to auto add, or migrate manually.");
        }
        log.info("Schema validate ok for collection [{}]", entity.getCollectionName());
    }

    private static void autoAdd(MilvusEntity entity, MilvusClientV2 client) {
        DescribeCollectionResp describe = describe(entity.getCollectionName(), client);
        Set<String> serverFields = describe.getFieldNames() == null
                ? new HashSet<>()
                : new HashSet<>(describe.getFieldNames());

        List<AddFieldReq> toAdd = entity.getMilvusFields().stream()
                .filter(f -> f != null && f.getFieldName() != null && !serverFields.contains(f.getFieldName()))
                .collect(Collectors.toList());

        if (toAdd.isEmpty()) {
            log.info("Schema auto-add: collection [{}] already up-to-date", entity.getCollectionName());
            return;
        }

        for (AddFieldReq field : toAdd) {
            // 已有集合新增字段通常要求 nullable 或 default
            if (!Boolean.TRUE.equals(field.getIsNullable()) && field.getDefaultValue() == null
                    && !Boolean.TRUE.equals(field.getIsPrimaryKey())) {
                log.warn("Auto-add field [{}] forces nullable=true for existing collection [{}]",
                        field.getFieldName(), entity.getCollectionName());
                field.setIsNullable(true);
            }
            log.info("Auto-add field [{}] to collection [{}]", field.getFieldName(), entity.getCollectionName());
            // 复用 ICMService 的转换逻辑：通过匿名实现
            addFieldInternal(client, entity.getCollectionName(), field);
        }
    }

    private static void addFieldInternal(MilvusClientV2 client, String collectionName, AddFieldReq fieldReq) {
        // 避免循环依赖：直接构建 AddCollectionFieldReq
        io.milvus.v2.service.collection.request.AddCollectionFieldReq.AddCollectionFieldReqBuilder builder =
                io.milvus.v2.service.collection.request.AddCollectionFieldReq.builder()
                        .collectionName(collectionName)
                        .fieldName(fieldReq.getFieldName())
                        .dataType(fieldReq.getDataType())
                        .description(fieldReq.getDescription())
                        .maxLength(fieldReq.getMaxLength())
                        .isPrimaryKey(Boolean.TRUE.equals(fieldReq.getIsPrimaryKey()))
                        .isPartitionKey(Boolean.TRUE.equals(fieldReq.getIsPartitionKey()))
                        .isClusteringKey(Boolean.TRUE.equals(fieldReq.getIsClusteringKey()))
                        .autoID(Boolean.TRUE.equals(fieldReq.getAutoID()))
                        .dimension(fieldReq.getDimension())
                        .elementType(fieldReq.getElementType())
                        .maxCapacity(fieldReq.getMaxCapacity())
                        .isNullable(fieldReq.getIsNullable() == null || Boolean.TRUE.equals(fieldReq.getIsNullable()))
                        .enableAnalyzer(fieldReq.getEnableAnalyzer())
                        .analyzerParams(fieldReq.getAnalyzerParams())
                        .enableMatch(fieldReq.getEnableMatch())
                        .typeParams(fieldReq.getTypeParams())
                        .multiAnalyzerParams(fieldReq.getMultiAnalyzerParams());
        if (fieldReq.isEnableDefaultValue() || fieldReq.getDefaultValue() != null) {
            builder.defaultValue(fieldReq.getDefaultValue());
        }
        client.addCollectionField(builder.build());
    }

    private static DescribeCollectionResp describe(String collectionName, MilvusClientV2 client) {
        return client.describeCollection(
                io.milvus.v2.service.collection.request.DescribeCollectionReq.builder()
                        .collectionName(collectionName)
                        .build()
        );
    }
}
