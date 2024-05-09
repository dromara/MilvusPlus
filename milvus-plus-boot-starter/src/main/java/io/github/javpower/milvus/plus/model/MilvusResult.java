package io.github.javpower.milvus.plus.model;

import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class MilvusResult<T> {
    private T entity;
    private Float distance;
    private Object id;
}
