package org.dromara.milvus.plus.service;

import com.google.gson.JsonObject;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.service.vector.request.*;
import io.milvus.v2.service.vector.request.data.BaseVector;
import io.milvus.v2.service.vector.response.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IVecMService {

    MilvusClientV2 getClient();

    /**
     * 根据ID或布尔表达式删除指定集合中的实体
     * @param collectionName 集合名称
     * @param partitionName 分区名称，如果为空，则在指定集合的所有分区中查找实体
     * @param filter 用于筛选匹配实体的标量过滤条件，默认为空字符串，表示不应用此条件
     * @param ids 要删除的实体ID列表，如果为空，则根据filter条件删除
     * @return DeleteResp对象，包含被删除实体的数量
     * @throws MilvusException 如果操作过程中发生错误
     */
    default DeleteResp delete(
            String collectionName,
            String partitionName,
            String filter,
            List<Object> ids
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        DeleteReq deleteReq = DeleteReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .filter(filter)
                .ids(ids)
                .build();
        return client.delete(deleteReq);
    }

    /**
     * 根据过滤条件删除指定集合中的实体
     * @param collectionName 集合名称
     * @param filter 用于筛选匹配实体的布尔表达式
     * @return DeleteResp对象，包含被删除实体的数量
     * @throws MilvusException 如果操作过程中发生错误
     */
    default DeleteResp deleteByFilter(String collectionName, String filter) throws MilvusException {
        return delete(collectionName, null, filter, null);
    }

    /**
     * 根据ID列表删除指定集合中的实体
     * @param collectionName 集合名称
     * @param ids 要删除的实体ID列表
     * @return DeleteResp对象，包含被删除实体的数量
     * @throws MilvusException 如果操作过程中发生错误
     */
    default DeleteResp deleteByIds(String collectionName, List<Object> ids) throws MilvusException {
        return delete(collectionName, null, "", ids);
    }

    /**
     * 根据ID获取指定集合中的特定实体
     * @param collectionName 集合名称
     * @param partitionName 分区名称，如果为空，则在指定集合的所有分区中查找实体
     * @param ids 要查询的实体ID列表
     * @param outputFields 查询结果中需要包含的字段名称列表
     * @return GetResp对象，表示一个或多个被查询的实体
     * @throws MilvusException 如果操作过程中发生错误
     */
    default GetResp get(
            String collectionName,
            String partitionName,
            List<Object> ids,
            List<String> outputFields
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        GetReq getReq = GetReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .ids(ids)
                .outputFields(outputFields)
                .build();
        return client.get(getReq);
    }

    /**
     * 根据ID列表获取指定集合中的实体，不指定分区，返回所有字段
     * @param collectionName 集合名称
     * @param ids 要查询的实体ID列表
     * @return GetResp对象，表示一个或多个被查询的实体
     * @throws MilvusException 如果操作过程中发生错误
     */
    default GetResp getByIds(String collectionName, List<Object> ids) throws MilvusException {
        return get(collectionName, null, ids, Collections.emptyList());
    }
    default GetResp getById(String collectionName, Object id) throws MilvusException {
        return get(collectionName, null, Collections.singletonList(id), Collections.emptyList());
    }
    /**
     * 向指定集合插入数据
     * @param collectionName 集合名称
     * @param data 要插入的数据列表，数据结构应与集合的schema匹配
     * @param partitionName 分区名称，如果为空，则数据将插入到默认分区
     * @return InsertResp对象，包含插入实体的数量信息
     * @throws MilvusException 如果操作过程中发生错误
     */
    default InsertResp insert(
            String collectionName,
            List<JsonObject> data,
            String partitionName
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        InsertReq insertReq = InsertReq.builder()
                .collectionName(collectionName)
                .data(data)
                .partitionName(partitionName)
                .build();
        return client.insert(insertReq);
    }

    /**
     * 向指定集合和分区插入单条数据
     * @param collectionName 集合名称
     * @param data 要插入的数据，数据结构应与集合的schema匹配
     * @return InsertResp对象，包含插入实体的数量信息
     * @throws MilvusException 如果操作过程中发生错误
     */
    default InsertResp insert(String collectionName, JsonObject data) throws MilvusException {
        return insert(collectionName, Collections.singletonList(data), null);
    }

    /**
     * 根据标量过滤条件进行查询
     * @param collectionName 集合名称
     * @param partitionNames 分区名称列表，如果为空，则查询整个集合
     * @param outputFields 查询结果中需要包含的字段名称列表
     * @param ids 要查询的实体ID列表，如果为空，则根据filter条件查询
     * @param filter 标量过滤条件，用于筛选匹配的实体
     * @param consistencyLevel 一致性级别，默认为集合创建时指定的级别
     * @param offset 查询结果中要跳过的记录数，用于分页
     * @param limit 查询结果返回的记录数，用于分页
     * @return 查询结果对象列表，包含指定输出字段的特定查询结果
     * @throws MilvusException 如果操作过程中发生错误
     */
    default QueryResp query(
            String collectionName,
            List<String> partitionNames,
            List<String> outputFields,
            List<Object> ids,
            String filter,
            ConsistencyLevel consistencyLevel,
            long offset,
            long limit
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        QueryReq queryReq = QueryReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNames)
                .outputFields(outputFields)
                .ids(ids)
                .filter(filter)
                .consistencyLevel(consistencyLevel)
                .offset(offset)
                .limit(limit)
                .build();
        return client.query(queryReq);
    }

    /**
     * 根据过滤条件查询指定集合中的实体，不指定分区和ID
     * @param collectionName 集合名称
     * @param outputFields 查询结果中需要包含的字段名称列表
     * @param filter 标量过滤条件
     * @param consistencyLevel 一致性级别
     * @param offset 跳过的记录数
     * @param limit 返回的记录数
     * @return 查询结果对象列表
     * @throws MilvusException 如果操作过程中发生错误
     */
    default QueryResp queryByFilter(
            String collectionName,
            List<String> outputFields,
            String filter,
            ConsistencyLevel consistencyLevel,
            long offset,
            long limit
    ) throws MilvusException {
        return query(collectionName, Collections.emptyList(), outputFields, Collections.emptyList(), filter, consistencyLevel, offset, limit);
    }

    /**
     * 执行向量相似性搜索，可选择性地包含标量过滤表达式
     * @param collectionName 集合名称
     * @param partitionNames 分区名称列表，如果为空，则搜索整个集合
     * @param annsField 向量字段名称，当存在多个向量字段时使用
     * @param topK 返回搜索结果记录数
     * @param filter 标量过滤条件，用于筛选匹配的实体
     * @param outputFields 查询结果中需要包含的字段名称列表
     * @param data 查询向量嵌入列表
     * @param offset 查询结果中要跳过的记录数，用于分页
     * @param limit 返回的记录数，用于分页
     * @param roundDecimal 计算距离结果保留的小数位数
     * @param searchParams 特定于此操作的参数设置
     * @param guaranteeTimestamp 有效的时间戳
     * @param gracefulTime 一个时间段（毫秒）
     * @param consistencyLevel 一致性级别
     * @param ignoreGrowing 是否在相似性搜索期间忽略增长段
     * @return 搜索结果对象列表，包含指定输出字段和相关性分数的特定搜索结果
     * @throws MilvusException 如果操作过程中发生错误
     */
    default SearchResp search(
            String collectionName,
            List<String> partitionNames,
            String annsField,
            int topK,
            String filter,
            List<String> outputFields,
            List<BaseVector> data,
            long offset,
            long limit,
            int roundDecimal,
            Map<String, Object> searchParams,
            long guaranteeTimestamp,
            long gracefulTime,
            ConsistencyLevel consistencyLevel,
            boolean ignoreGrowing
    ) throws MilvusException {
        MilvusClientV2 client = getClient();
        SearchReq searchReq = SearchReq.builder()
                .collectionName(collectionName)
                .partitionNames(partitionNames)
                .annsField(annsField)
                .topK(topK)
                .filter(filter)
                .outputFields(outputFields)
                .data(data)
                .offset(offset)
                .limit(limit)
                .roundDecimal(roundDecimal)
                .searchParams(searchParams)
                .guaranteeTimestamp(guaranteeTimestamp)
                .gracefulTime(gracefulTime)
                .consistencyLevel(consistencyLevel)
                .ignoreGrowing(ignoreGrowing)
                .build();
        return client.search(searchReq);
    }

    /**
     * 便利方法：执行向量相似性搜索，不包含标量过滤表达式
     * @param collectionName 集合名称
     * @param data 查询向量嵌入列表
     * @param topK 返回搜索结果记录数
     * @return 搜索结果对象列表
     * @throws MilvusException 如果操作过程中发生错误
     */
    default SearchResp search(String collectionName, List<BaseVector> data, int topK) throws MilvusException {
        return search(collectionName, Collections.emptyList(), null, topK, "", Collections.emptyList(), data, 0, 0, -1, Collections.emptyMap(), 0, 0, ConsistencyLevel.BOUNDED, false);
    }

    /**
     * 在指定集合中插入或更新数据
     * @param collectionName 集合名称
     * @param partitionName 分区名称，如果为空，则数据将插入或更新到默认分区
     * @param data 要插入或更新的数据列表，数据结构应与集合的schema匹配
     * @return UpsertResp对象，包含插入或更新实体的数量信息
     * @throws MilvusException 如果操作过程中发生错误
     */
    default UpsertResp upsert(
            String collectionName,
            String partitionName,
            List<JsonObject> data) throws MilvusException {
        MilvusClientV2 client = getClient();
        UpsertReq upsertReq = UpsertReq.builder()
                .collectionName(collectionName)
                .partitionName(partitionName)
                .data(data)
                .build();
        return client.upsert(upsertReq);
    }

    /**
     * 便利方法：在指定集合中插入或更新单条数据
     * @param collectionName 集合名称
     * @param data 要插入或更新的单个数据，数据结构应与集合的schema匹配
     * @return UpsertResp对象，包含插入或更新实体的数量信息
     * @throws MilvusException 如果操作过程中发生错误
     */
    default UpsertResp upsert(String collectionName, JsonObject data) throws MilvusException {
        return upsert(collectionName, null, Collections.singletonList(data));
    }

}
