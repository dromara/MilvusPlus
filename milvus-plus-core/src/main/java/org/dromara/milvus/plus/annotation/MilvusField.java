package org.dromara.milvus.plus.annotation;

import io.milvus.v2.common.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xgc
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MilvusField {

    /**
     * 字段名称，默认使用 Java 字段名
     */
    String name() default "";

    /**
     * 数据类型，默认为 FLOAT_VECTOR
     *
     * @see DataType
     */
    DataType dataType() default DataType.FloatVector;

    /**
     * 向量维度，仅对向量类型有效
     */
    int dimension() default -1;

    /**
     * 是否为主键
     */
    boolean isPrimaryKey() default false;

    /**
     * 是否自动生成
     */
    boolean autoID() default false;

    /**
     * 字段描述
     */
    String description() default "";

    /**
     * 数组或集合中元素的类型，默认为 INVALID
     *
     * @see DataType
     */
    DataType elementType() default DataType.None;

    /**
     * 数组或字符串类型的最大长度
     */
    int maxLength() default -1;

    /**
     * 集合类型的最大容量
     */
    int maxCapacity() default -1;

    /**
     * 是否为分区键
     */
    boolean isPartitionKey() default false;

    /**
     * 启动分析器
     */
    boolean enableAnalyzer() default false;

    /**
     *
     * 启用文本匹配
     */
    boolean enableMatch() default false;

    /**
     * 分析器参数。
     */
    AnalyzerParams analyzerParams() default @AnalyzerParams;

}