package io.github.javpower.milvus.plus.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResult;
import io.github.javpower.milvus.plus.model.vo.MilvusResultVo;
import io.milvus.v2.service.vector.response.GetResp;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xgc
 **/
public class SearchRespConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> MilvusResp<MilvusResultVo<T>> convertSearchRespToMilvusResp(SearchResp searchResp, Class<T> entityType) {
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType);
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        // 将searchResults中的每个SearchResult转换为MilvusResult<T>
        List<List<MilvusResult<T>>> milvusResults = searchResults.stream()
                .map(innerList -> innerList.stream()
                        .map(searchResult -> {
                            try {
                                // 使用ObjectMapper将Map<String, Object>转换为Java实体类T
                                Map<String, Object> entityMap = searchResult.getEntity();
                                Map<String, Object> entityMap2=new HashMap<>();
                                for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
                                    String key = propertyCache.findKeyByValue(entry.getKey());
                                    entityMap2.put(key,entry.getValue());
                                }
                                T entity = objectMapper.convertValue(entityMap2, entityType);
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

    public static <T> MilvusResp<List<T>> convertGetRespToMilvusResp(QueryResp getResp, Class<T> entityType) {
        List<QueryResp.QueryResult> queryResults = getResp.getQueryResults();
        return convertQuery(queryResults,entityType);
    }
    public static <T> MilvusResp<List<T>> convertGetRespToMilvusResp(GetResp getResp, Class<T> entityType) {
        List<QueryResp.QueryResult> getResults = getResp.getResults;
        return convertQuery(getResults,entityType);
    }
    private static <T> MilvusResp<List<T>>  convertQuery(List<QueryResp.QueryResult> getResults, Class<T> entityType){
        // 解析GetResp中的查询结果
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType);
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        List<T> entities = new ArrayList<>();
        // 遍历每个查询结果，并将它们映射到Java实体类T的实例
        for (QueryResp.QueryResult queryResult : getResults) {
            Map<String, Object> entityMap = queryResult.getEntity();
            Map<String, Object> entityMap2=new HashMap<>();
            for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
                String key = propertyCache.findKeyByValue(entry.getKey());
                entityMap2.put(key,entry.getValue());
            }
            // 假设有一个方法可以从Map映射到实体类T，这个方法需要自定义实现
            T entity =  objectMapper.convertValue(entityMap2, entityType);
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