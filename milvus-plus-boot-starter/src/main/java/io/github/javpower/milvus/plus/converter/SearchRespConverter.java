package io.github.javpower.milvus.plus.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.javpower.milvus.plus.cache.ConversionCache;
import io.github.javpower.milvus.plus.cache.MilvusCache;
import io.github.javpower.milvus.plus.cache.PropertyCache;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResult;
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

    /**
     * 将SearchResp对象转换为自定义的MilvusResp对象，其中SearchResp是Milvus搜索响应的内部结构，
     * 而MilvusResp是对外提供的统一响应格式。该方法主要涉及将搜索结果中的每个实体从Map形式转换为指定的Java实体类T。
     *
     * @param searchResp Milvus搜索操作的原始响应对象，包含搜索结果的详细信息。
     * @param entityType 指定的Java实体类类型，用于将搜索结果的每个实体转换为该类型。
     * @return 转换后的MilvusResp对象，其中包含了列表形式的搜索结果以及操作是否成功的标志。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertSearchRespToMilvusResp(SearchResp searchResp, Class<T> entityType) {
        // 从缓存中获取对应实体类型的转换缓存和属性缓存
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType);
        PropertyCache propertyCache = conversionCache.getPropertyCache();

        // 获取原始搜索结果列表，并将其转换为MilvusResult<T>类型的列表
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        List<List<MilvusResult<T>>> milvusResults = searchResults.stream()
                .map(innerList -> innerList.stream()
                        .map(searchResult -> {
                            try {
                                // 根据属性缓存，将实体Map中的键转换为对应的Java实体类字段名
                                Map<String, Object> entityMap = searchResult.getEntity();
                                Map<String, Object> entityMap2=new HashMap<>();
                                for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
                                    String key = propertyCache.findKeyByValue(entry.getKey());
                                    entityMap2.put(key,entry.getValue());
                                }
                                // 将转换后的Map转换为Java实体类T
                                T entity = objectMapper.convertValue(entityMap2, entityType);
                                // 创建MilvusResult对象并设置相关字段
                                MilvusResult<T> tMilvusResult = new MilvusResult<>();
                                tMilvusResult.setId(searchResult.getId());
                                tMilvusResult.setDistance(searchResult.getDistance());
                                tMilvusResult.setEntity(entity);
                                return tMilvusResult;
                            } catch (IllegalArgumentException e) {
                                // 抛出转换过程中的异常
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        // 将嵌套的列表展平为单个列表
        List<MilvusResult<T>> results = milvusResults
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 创建并填充MilvusResp对象
        MilvusResp<List<MilvusResult<T>>> milvusResp = new MilvusResp<>();
        milvusResp.setData(results);
        milvusResp.setSuccess(true);

        return milvusResp;
    }

    /**
     * 将Get响应转换为Milvus响应的通用方法。
     * @param getResp Get操作的响应对象，可以是QueryResp或GetResp类型。
     * @param entityType 实体类型，用于泛型结果的类型转换。
     * @return 返回一个包含Milvus结果列表的MilvusResp对象。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertGetRespToMilvusResp(QueryResp getResp, Class<T> entityType) {
        // 从QueryResp中提取查询结果
        List<QueryResp.QueryResult> queryResults = getResp.getQueryResults();
        return convertQuery(queryResults, entityType);
    }

    /**
     * 将Get响应转换为Milvus响应的通用方法。
     * @param getResp Get操作的响应对象，可以是QueryResp或GetResp类型。
     * @param entityType 实体类型，用于泛型结果的类型转换。
     * @return 返回一个包含Milvus结果列表的MilvusResp对象。
     */
    public static <T> MilvusResp<List<MilvusResult<T>>> convertGetRespToMilvusResp(GetResp getResp, Class<T> entityType) {
        // 从GetResp中提取结果
        List<QueryResp.QueryResult> getResults = getResp.getResults;
        return convertQuery(getResults, entityType);
    }


    /**
     * 将查询结果转换为指定类型的实体列表。
     *
     * @param getResults 查询结果列表，来自Milvus数据库的查询响应。
     * @param entityType 需要转换成的实体类型，指定了转换的目标。
     * @return MilvusResp对象，包含转换后的实体列表。每个实体都包装在一个MilvusResult对象中，同时设置成功状态为true。
     */
    private static <T> MilvusResp<List<MilvusResult<T>>> convertQuery(List<QueryResp.QueryResult> getResults, Class<T> entityType){
        // 初始化转换缓存和属性缓存，用于帮助将查询结果映射到Java实体
        ConversionCache conversionCache = MilvusCache.milvusCache.get(entityType);
        PropertyCache propertyCache = conversionCache.getPropertyCache();
        List<T> entities = new ArrayList<>();

        // 遍历每个查询结果，映射到对应的Java实体
        for (QueryResp.QueryResult queryResult : getResults) {
            Map<String, Object> entityMap = queryResult.getEntity();
            Map<String, Object> entityMap2=new HashMap<>();
            // 通过属性缓存转换键名，以适应Java实体的字段命名
            for (Map.Entry<String, Object> entry : entityMap.entrySet()) {
                String key = propertyCache.findKeyByValue(entry.getKey());
                entityMap2.put(key,entry.getValue());
            }
            // 使用转换工具将映射后的Map转换为指定类型的实体
            T entity =  objectMapper.convertValue(entityMap2, entityType);
            entities.add(entity);
        }

        // 将转换后的实体列表包装成MilvusResult对象，并收集到一个新的列表中
        List<MilvusResult<T>> results = entities.stream().map(v -> {
            MilvusResult<T> vo = new MilvusResult<>();
            vo.setEntity(v);
            vo.setDistance(0.0f); // 设置距离为0.0f，因为当前上下文未提供实际距离信息
            return vo;
        }).collect(Collectors.toList());

        // 构建并返回一个包含转换结果的MilvusResp对象，标记操作成功
        MilvusResp<List<MilvusResult<T>>> milvusResp = new MilvusResp<>();
        milvusResp.setData(results);
        milvusResp.setSuccess(true);
        return milvusResp;
    }
}
