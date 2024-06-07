package org.dromara.milvus.plus.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class LogLevelController {

    // 获取LoggerContext
    private static final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

    /**
     * 设置特定包下所有类的日志级别。
     *
     * @param packageName 包名
     * @param level       日志级别
     */
    public static void setLogLevelForPackage(String packageName, Level level) {
        for (ch.qos.logback.classic.Logger logger : loggerContext.getLoggerList()) {
            if (logger.getName().startsWith(packageName)) {
                logger.setLevel(level);
            }
        }
    }

    /**
     * 动态设置日志开关。
     * 当设置为Level.OFF时，等同于关闭日志。
     *
     * @param packageName 包名
     * @param enabled     是否启用日志
     */
    public static void setLoggingEnabledForPackage(String packageName, boolean enabled, String level) {
        // 开启日志 默认 -> debug
        if (enabled) {
            setLogLevelForPackage(packageName, Level.toLevel(level, Level.DEBUG));
        } else {
            setLogLevelForPackage(packageName, Level.OFF);
        }
    }
}