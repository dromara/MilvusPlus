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

    String name() default ""; // 字段名称，默认使用 Java 字段名
    DataType dataType() default DataType.FloatVector; // 数据类型，默认为 FLOAT_VECTOR
    int dimension() default -1; // 向量维度，仅对向量类型有效
    boolean isPrimaryKey() default false; // 是否为主键
    boolean autoID() default false; // 是否自动生成
    String description() default ""; // 字段描述
    DataType elementType() default DataType.None; // 数组或集合中元素的类型，默认为 INVALID
    int maxLength() default -1; // 数组或字符串类型的最大长度，默认为 -1（不指定）
    int maxCapacity() default -1; // 集合类型的最大容量，默认为 -1（不指定）
    boolean isPartitionKey() default false; // 是否为分区键
}