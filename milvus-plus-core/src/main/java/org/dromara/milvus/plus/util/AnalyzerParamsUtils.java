package org.dromara.milvus.plus.util;

import com.google.common.collect.Lists;
import org.dromara.milvus.plus.annotation.AnalyzerParams;
import org.dromara.milvus.plus.annotation.BuiltInFilter;
import org.dromara.milvus.plus.annotation.CustomFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzerParamsUtils {

    public static Map<String, Object> convertToMap(AnalyzerParams analyzerParams) {
        Map<String, Object> paramsMap = new HashMap<>();
        if (analyzerParams != null) {
            // 设置分词器
            paramsMap.put("tokenizer", analyzerParams.tokenizer().name().toLowerCase());
            // 处理内置过滤器
            List<String> builtInFiltersList = new ArrayList<>();
            for (BuiltInFilter builtInFilter : analyzerParams.builtInFilters()) {
                builtInFiltersList.add(builtInFilter.name().name());
            }
            // 处理自定义过滤器
            List<Map<String, Object>> customFiltersList = new ArrayList<>();
            for (CustomFilter customFilter : analyzerParams.customFilters()) {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("type", customFilter.type());
                if (customFilter.max() > 0) {
                    filterMap.put("max", customFilter.max());
                }
                if (customFilter.stopWords().length > 0) {
                    filterMap.put("stopWords", new ArrayList<>(Lists.newArrayList(customFilter.stopWords())));
                }
                customFiltersList.add(filterMap);
            }
            // 合并过滤器列表
            List<Object> filters = new ArrayList<>();
            filters.addAll(builtInFiltersList);
            filters.addAll(customFiltersList);
            paramsMap.put("filter", filters);
        }
        return paramsMap;
    }

}