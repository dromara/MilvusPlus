// LogFrameworkAdapter.java
package org.dromara.milvus.plus.log.spi;

public interface LogFrameworkAdapter {
    /**
     * 适配器优先级（数值越小优先级越高）
     */
    int getPriority();

    /**
     * 是否支持当前日志框架
     */
    boolean isSupported();

    /**
     * 设置指定包的日志级别
     */
    void setLogLevel(String packageName, String level);
}
