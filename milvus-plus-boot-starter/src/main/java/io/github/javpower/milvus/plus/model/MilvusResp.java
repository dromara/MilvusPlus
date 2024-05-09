package io.github.javpower.milvus.plus.model;

import lombok.Data;

import java.util.List;
/**
 * @author xgc
 **/
@Data
public class MilvusResp<T> {
    private List<List<MilvusResult<T>>> res ;

}
