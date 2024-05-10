package io.github.javpower.milvus.plus.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResult;
import io.github.javpower.milvus.plus.model.vo.MilvusResultVo;
import io.milvus.v2.service.vector.response.SearchResp;

import java.util.List;
/**
 * @author xgc
 **/
public class SearchRespConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> MilvusResp<MilvusResultVo<T>> convertSearchRespToMilvusResp(SearchResp searchResp, Class<T> entityType){
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();

        // 反序列化JSON字符串到具体的MilvusResult对象列表
        TypeReference<List<List<MilvusResult<T>>>> typeRef = new TypeReference<List<List<MilvusResult<T>>>>() {};
        List<List<MilvusResult<T>>> milvusResults = null;
        try {
            // 将searchResults转换为JSON字符串
            String jsonResults = objectMapper.writeValueAsString(searchResults);
            milvusResults = objectMapper.readValue(jsonResults, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        // 创建MilvusResp对象并设置结果
        MilvusResp<MilvusResultVo<T>> milvusResp = new MilvusResp<>();
        MilvusResultVo<T> vo=new MilvusResultVo<>();
        vo.setVo(milvusResults);
        milvusResp.setData(vo);
        milvusResp.setSuccess(true);
        return milvusResp;
    }

}