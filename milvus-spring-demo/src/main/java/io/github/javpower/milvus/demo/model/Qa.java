package io.github.javpower.milvus.demo.model;

import io.github.javpower.milvus.plus.annotation.ExtraParam;
import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.annotation.MilvusField;
import io.github.javpower.milvus.plus.annotation.MilvusIndex;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import lombok.Data;

import java.util.List;

@Data
@MilvusCollection(name = "qa") // 指定Milvus集合的名称
public class Qa {
    @MilvusField(
            name = "qa_id", // 字段名称
            dataType = DataType.Int64, // 数据类型为64位整数
            isPrimaryKey = true, // 标记为主键
            autoID = true // 假设这个ID是自动生成的
    )
    private Long qa;

    @MilvusField(
            name = "question", // 字段名称
            dataType = DataType.FloatVector, // 数据类型为浮点型向量
            dimension = 128 // 向量维度，假设人脸特征向量的维度是128
    )
    @MilvusIndex(
            indexType = IndexParam.IndexType.IVF_FLAT, // 使用IVF_FLAT索引类型
            metricType = IndexParam.MetricType.L2, // 使用L2距离度量类型
            indexName = "q_index", // 索引名称
            extraParams = { // 指定额外的索引参数
                    @ExtraParam(key = "nprobe", value = "10") // 例如，IVF的nlist参数
            }
    )
    private List<Double> qVector;

    @MilvusField(
            name = "answer", // 字段名称
            dataType = DataType.VarChar // 数据类型为64位整数
    )
    private String answer;
}