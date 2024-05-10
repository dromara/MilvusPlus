package io.github.javpower.milvus.plus.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author xgc
 **/
@Data
public class MilvusResultVo<T> {
    private List<List<MilvusResult<T>>> vo;

}
