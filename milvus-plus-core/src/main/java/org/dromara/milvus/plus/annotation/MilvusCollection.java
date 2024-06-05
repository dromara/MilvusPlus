package org.dromara.milvus.plus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xgc
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MilvusCollection {
    /**
     * 集合的名称
     */
    String name();

    /**
     *
     */
    String[] alias() default {};
}