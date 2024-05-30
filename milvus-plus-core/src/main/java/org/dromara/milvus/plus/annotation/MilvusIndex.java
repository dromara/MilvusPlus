package org.dromara.milvus.plus.annotation;


import io.milvus.v2.common.IndexParam;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @author xgc
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MilvusIndex {
    IndexParam.IndexType indexType() default IndexParam.IndexType.FLAT; // 索引类型
    IndexParam.MetricType metricType() default IndexParam.MetricType.L2; // 度量类型
    String indexName() default ""; // 索引名称
    ExtraParam[] extraParams() default {}; // 指定额外的参数
}