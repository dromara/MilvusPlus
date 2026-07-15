package org.dromara.milvus.plus.model.vo;

import lombok.Data;
/**
 * @author xgc
 **/
@Data
public class MilvusResult<T> {
    private T entity;
    /**
     * 兼容历史字段名。SDK 3.x 语义为 score（相似度/距离得分）。
     */
    private Float distance;
    private Object id;
    private Long total;

    /**
     * 语义别名：推荐新代码使用 score。
     */
    public Float getScore() {
        return distance;
    }

    public void setScore(Float score) {
        this.distance = score;
    }
}
