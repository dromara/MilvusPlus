package org.dromara.milvus.plus.model.vo;

import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class MilvusResult<T> {
    private T entity;
    private Float distance;
    private Object id;
    private Long total;
}
