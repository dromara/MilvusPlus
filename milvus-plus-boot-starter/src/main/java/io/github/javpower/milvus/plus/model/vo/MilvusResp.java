package io.github.javpower.milvus.plus.model.vo;

import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class MilvusResp<T> {

    private boolean success;
    private T data;

}
