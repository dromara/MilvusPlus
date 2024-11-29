package org.dromara.milvus.plus.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.annotation.AnalyzerParams;
import org.dromara.milvus.plus.annotation.CustomFilter;
import org.dromara.milvus.plus.annotation.Filter;
import org.dromara.milvus.plus.model.BuiltInFilterType;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzerParamsUtils {

    public static Map<String, Object> convertToMap(AnalyzerParams analyzerParams) {
        Map<String, Object> paramsMap = new HashMap<>();
        if (analyzerParams != null) {
            String type = analyzerParams.type();
            if(StringUtils.isNotEmpty(type)){
                //使用默认分析器
                paramsMap.put("type", type);
            }
            String tokenizer = analyzerParams.tokenizer();
            if(StringUtils.isNotEmpty(tokenizer)){
                // 设置分词器
                paramsMap.put("tokenizer",tokenizer);
            }
            Filter filter = analyzerParams.filter();
            List<String> builtInFiltersList = new ArrayList<>();
            List<Map<String, Object>> customFiltersList = new ArrayList<>();
            if(filter!=null){
                CustomFilter[] customFilters = filter.customFilters();
                BuiltInFilterType[] builtInFilterTypes = filter.builtInFilters();
                // 处理内置过滤器
                for (BuiltInFilterType builtInFilterType : builtInFilterTypes) {
                    builtInFiltersList.add(builtInFilterType.name());
                }
                //处理自定义过滤器
                for (CustomFilter customFilter : customFilters) {
                    Map<String, Object> filterMap = new HashMap<>();
                    filterMap.put("type", customFilter.type());
                    if (customFilter.max() > 0) {
                        filterMap.put("max", customFilter.max());
                    }
                    if (customFilter.stopWords().length > 0) {
                        filterMap.put("stop_words", new ArrayList<>(Lists.newArrayList(customFilter.stopWords())));
                    }
                    customFiltersList.add(filterMap);
                }
            }
            // 合并过滤器列表
            List<Object> filters = new ArrayList<>();
            filters.addAll(builtInFiltersList);
            filters.addAll(customFiltersList);
            if(!CollectionUtils.isEmpty(filters)){
                paramsMap.put("filter", filters);
            }
        }
        return paramsMap;
    }

}