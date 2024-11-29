package org.dromara.milvus.plus.annotation;

import org.dromara.milvus.plus.model.TokenizerType;

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

    TokenizerType tokenizer() default TokenizerType.standard; // 分词器配置
    BuiltInFilter[] builtInFilters() default {}; //内置过滤器
    CustomFilter[] customFilters() default {}; //自定义过滤器

}