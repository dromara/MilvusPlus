package org.dromara.milvus.plus.model.vo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * 标量分页结果。向量检索请使用 topK/limit，语义不同于数据库 offset 分页。
 */
@Data
public class PageResult<T> {

    private long pageNum;
    private long pageSize;
    private long total;
    private List<MilvusResult<T>> records = Collections.emptyList();

    public long getPages() {
        if (pageSize <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<MilvusResult<T>> records) {
        PageResult<T> page = new PageResult<>();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setTotal(total);
        page.setRecords(records == null ? Collections.emptyList() : records);
        return page;
    }
}
