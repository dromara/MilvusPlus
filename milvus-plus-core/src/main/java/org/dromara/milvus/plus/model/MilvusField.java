package org.dromara.milvus.plus.model;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.AddFieldReq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
/**
 * @author xgc
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MilvusField {
    private String fieldName;
    private DataType dataType;
    private Boolean isPrimaryKey;
    private Boolean autoId;
    private String description;
    private Integer dimension;
    private Integer maxLength;
    private Boolean isPartitionKey;
    private DataType elementType;
    private Integer maxCapacity;

    public AddFieldReq to() {
        return AddFieldReq.builder()
                .fieldName(fieldName)
                .dataType(dataType)
                .isPrimaryKey(isPrimaryKey)
                .autoID(autoId)
                .description(StringUtils.isNotEmpty(description) ? description : null)
                .dimension(dimension)
                .maxLength(maxLength != null && maxLength > 0 ? maxLength : null)
                .isPartitionKey(isPartitionKey)
                .elementType(elementType)
                .maxCapacity(maxCapacity != null && maxCapacity > 0 ? maxCapacity : null)
                .build();
    }
}