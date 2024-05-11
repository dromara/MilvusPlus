package io.github.javpower.milvus.plus.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResult;
import io.github.javpower.milvus.plus.model.vo.MilvusResultVo;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xgc
 **/
public class SearchRespConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> MilvusResp<MilvusResultVo<T>> convertSearchRespToMilvusResp(SearchResp searchResp, Class<T> entityType) {
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        // 将searchResults中的每个SearchResult转换为MilvusResult<T>
        List<List<MilvusResult<T>>> milvusResults = searchResults.stream()
                .map(innerList -> innerList.stream()
                        .map(searchResult -> {
                            try {
                                // 使用ObjectMapper将Map<String, Object>转换为Java实体类T
                                T entity = objectMapper.convertValue(searchResult.getEntity(), entityType);
                                MilvusResult<T> tMilvusResult = new MilvusResult<>();
                                tMilvusResult.setId(searchResult.getId());
                                tMilvusResult.setDistance(searchResult.getDistance());
                                tMilvusResult.setEntity(entity);
                                return tMilvusResult;
                            } catch (IllegalArgumentException e) {
                                // 处理转换错误
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // 创建MilvusResultVo对象并设置结果
        MilvusResultVo<T> vo = new MilvusResultVo<>();
        vo.setVo(milvusResults);
        // 创建MilvusResp对象并设置结果和成功标志
        MilvusResp<MilvusResultVo<T>> milvusResp = new MilvusResp<>();
        milvusResp.setData(vo);
        milvusResp.setSuccess(true);

        return milvusResp;
    }

    public static <T> MilvusResp<List<T>> convertGetRespToMilvusResp(GetResp getResp, Class<T> entityType) {
        // 解析GetResp中的查询结果
        List<QueryResp.QueryResult> getResults = getResp.getResults;
        List<T> entities = new ArrayList<>();
        // 遍历每个查询结果，并将它们映射到Java实体类T的实例
        for (QueryResp.QueryResult queryResult : getResults) {
            Map<String, Object> entityMap = queryResult.getEntity();
            // 假设有一个方法可以从Map映射到实体类T，这个方法需要自定义实现
            T entity =  objectMapper.convertValue(entityMap, entityType);
            entities.add(entity);
        }
        // 创建MilvusResp对象，并将实体列表作为其数据部分
        MilvusResp<List<T>> milvusResp = new MilvusResp<>();
        milvusResp.setData(entities);
        milvusResp.setSuccess(true);
        // 返回MilvusResp对象
        return milvusResp;
    }
}