package org.dromara.milvus.plus.log.spi.impl;

import org.dromara.milvus.plus.log.spi.LogFrameworkAdapter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
@Service
public class Log4j2Adapter implements LogFrameworkAdapter {
    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public boolean isSupported() {
        try {
            Class.forName("org.apache.logging.log4j.core.config.Configurator");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void setLogLevel(String packageName, String level) {
        try {
            Class<?> configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");
            Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
            
            // 缓存反射方法提升性能
            Method toLevelMethod = levelClass.getMethod("toLevel", String.class);
            Object logLevel = toLevelMethod.invoke(null, level);
            
            Method setLevelMethod = configuratorClass.getMethod("setLevel", String.class, levelClass);
            setLevelMethod.invoke(null, packageName, logLevel);
        } catch (Exception e) {
            throw new IllegalStateException("[Log4j2] Set log level failed: " + e.getMessage(), e);
        }
    }
}
