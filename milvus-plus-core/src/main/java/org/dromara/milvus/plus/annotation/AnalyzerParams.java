package org.dromara.milvus.plus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表示分析器参数的注解，包含分词器和过滤器列表。
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnalyzerParams {
    String type() default ""; // 分析器类型
    String tokenizer() default ""; // 自定义分词器配置
    Filter filter() default @Filter; //过滤器

}