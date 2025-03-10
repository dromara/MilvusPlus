package org.dromara.milvus.plus.log;

import org.dromara.milvus.plus.log.spi.LogFrameworkAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class LogAdapterFactory {
    private static volatile LogFrameworkAdapter cachedAdapter;

    public static LogFrameworkAdapter getAdapter() {
        if (cachedAdapter == null) {
            synchronized (LogAdapterFactory.class) {
                if (cachedAdapter == null) {
                    ServiceLoader<LogFrameworkAdapter> loader = ServiceLoader.load(LogFrameworkAdapter.class);
                    List<LogFrameworkAdapter> adapters = new ArrayList<>();

                    for (LogFrameworkAdapter adapter : loader) {
                        if (adapter.isSupported()) {
                            adapters.add(adapter);
                        }
                    }

                    if (adapters.isEmpty()) {
                        throw new IllegalStateException(
                                "No supported logging framework found! Please add one of:\n" +
                                        "- Logback: ch.qos.logback:logback-classic\n" +
                                        "- Log4j2: org.apache.logging.log4j:log4j-core + log4j-slf4j2-impl\n" +
                                        "- JUL: JDK built-in (no extra dependencies needed)"
                        );
                    }

                    // 按优先级排序
                    adapters.sort(Comparator.comparingInt(LogFrameworkAdapter::getPriority));
                    cachedAdapter = adapters.get(0);
                }
            }
        }
        return cachedAdapter;
    }
}
