package io.github.javpower.milvus.plus.core;
/**
 * @author xgc
 **/
@FunctionalInterface
public interface FieldFunction<T, R> {
    R apply(T entity);
}