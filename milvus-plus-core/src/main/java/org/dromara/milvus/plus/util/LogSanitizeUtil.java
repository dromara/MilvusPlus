package org.dromara.milvus.plus.util;

/**
 * 日志脱敏：向量数据很长，默认截断，避免刷屏与性能问题。
 */
public final class LogSanitizeUtil {

    private static final int DEFAULT_MAX = 800;

    private LogSanitizeUtil() {
    }

    public static String truncate(Object value) {
        return truncate(value, DEFAULT_MAX);
    }

    public static String truncate(Object value, int maxLen) {
        if (value == null) {
            return "null";
        }
        String text = value instanceof String ? (String) value : GsonUtil.toJson(value);
        if (text == null) {
            return "null";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen) + "...(truncated,len=" + text.length() + ")";
    }
}
