package org.dromara.milvus.plus.log.spi.impl;

import org.dromara.milvus.plus.log.spi.LogFrameworkAdapter;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
@Service
public class LogbackAdapter implements LogFrameworkAdapter {
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isSupported() {
        try {
            // 检测关键类是否存在，避免直接导入
            Class.forName("ch.qos.logback.classic.Logger");
            Class.forName("ch.qos.logback.classic.Level");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public void setLogLevel(String packageName, String level) {
        try {
            // 反射获取 Logback 相关类和方法
            Class<?> loggerContextClass = Class.forName("ch.qos.logback.classic.LoggerContext");
            Class<?> levelClass = Class.forName("ch.qos.logback.classic.Level");

            // 获取 Level.toLevel 方法
            Method toLevelMethod = levelClass.getMethod("toLevel", String.class);
            Object logLevel = toLevelMethod.invoke(null, level);

            // 获取 LoggerContext 实例
            Object loggerContext = LoggerFactory.getILoggerFactory();
            if (!loggerContextClass.isInstance(loggerContext)) {
                throw new IllegalStateException("LoggerFactory is not Logback");
            }

            // 调用 LoggerContext.getLoggerList()
            Method getLoggerListMethod = loggerContextClass.getMethod("getLoggerList");
            Iterable<?> loggers = (Iterable<?>) getLoggerListMethod.invoke(loggerContext);

            // 遍历并设置日志级别
            for (Object logger : loggers) {
                Method getNameMethod = logger.getClass().getMethod("getName");
                String name = (String) getNameMethod.invoke(logger);
                if (name.startsWith(packageName)) {
                    Method setLevelMethod = logger.getClass().getMethod("setLevel", levelClass);
                    setLevelMethod.invoke(logger, logLevel);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("[Logback] Set log level failed: " + e.getMessage(), e);
        }
    }
}

