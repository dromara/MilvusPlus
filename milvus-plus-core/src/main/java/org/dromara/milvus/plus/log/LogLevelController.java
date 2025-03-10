package org.dromara.milvus.plus.log;

public class LogLevelController {
    public static void setLogLevelForPackage(String packageName, String level) {
        LogContext.setLogLevel(packageName, level);
    }

    public static void setLoggingEnabledForPackage(String packageName, boolean enabled, String level) {
        LogContext.setLoggingEnabled(packageName, enabled, level);
    }
}
