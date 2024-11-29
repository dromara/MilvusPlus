package org.dromara.milvus.plus.annotation;

import org.dromara.milvus.plus.model.BuiltInFilterType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义内置过滤器的注解。
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BuiltInFilter {
    BuiltInFilterType name() default BuiltInFilterType.lowercase;
}