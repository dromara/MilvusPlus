package org.dromara.milvus.plus.core.conditions;

import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.IndexParam;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 检索专家参数收口，避免 LambdaQueryWrapper 主链无限膨胀。
 */
@Data
public class SearchOptions {

    private ConsistencyLevel consistencyLevel;
    private IndexParam.MetricType metricType;
    private Boolean ignoreGrowing;
    private Integer roundDecimal;
    private String groupByFieldName;
    private Integer groupSize;
    private Boolean strictGroupSize;
    private Long guaranteeTimestamp;
    private Long gracefulTime;
    private final Map<String, Object> searchParams = new HashMap<>(8);

    public static SearchOptions create() {
        return new SearchOptions();
    }

    public static SearchOptions of(Consumer<SearchOptions> customizer) {
        SearchOptions options = new SearchOptions();
        if (customizer != null) {
            customizer.accept(options);
        }
        return options;
    }

    public SearchOptions radius(Object radius) {
        searchParams.put("radius", radius);
        return this;
    }

    public SearchOptions rangeFilter(Object rangeFilter) {
        searchParams.put("range_filter", rangeFilter);
        return this;
    }

    public SearchOptions param(String key, Object value) {
        if (key != null) {
            searchParams.put(key, value);
        }
        return this;
    }

    public SearchOptions params(Map<String, Object> params) {
        if (params != null) {
            searchParams.putAll(params);
        }
        return this;
    }
}
