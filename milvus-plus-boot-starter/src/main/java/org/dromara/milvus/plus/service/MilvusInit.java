package org.dromara.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.config.MilvusPropertiesConfiguration;
import org.dromara.milvus.plus.log.LogLevelController;
import org.dromara.milvus.plus.model.MilvusProperties;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class MilvusInit extends AbstractMilvusClientBuilder implements InitializingBean, DisposableBean {

    private final MilvusPropertiesConfiguration milvusPropertiesConfiguration;

    private MilvusClientV2 client;

    public MilvusInit(MilvusPropertiesConfiguration milvusPropertiesConfiguration) {
        this.milvusPropertiesConfiguration = milvusPropertiesConfiguration;
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }
    @Override
    public void destroy() throws Exception {
       // super.close();
    }

    public void initialize() {
        printBanner();
        LogLevelController.setLoggingEnabledForPackage("org.dromara.milvus.plus",
                milvusPropertiesConfiguration.isOpenLog(),
                milvusPropertiesConfiguration.getLogLevel());
        MilvusProperties milvusProperties = new MilvusProperties();
        BeanUtils.copyProperties(milvusPropertiesConfiguration, milvusProperties);
        super.setProperties(milvusProperties);
        super.initialize();
        client = getClient();
    }

    @Bean
    public MilvusClientV2 milvusClientV2() {
        return client;
    }

    public void printBanner() {
        String banner =
                "  __  __ _ _                    ____  _           \n" +
                        " |  \\/  (_) |_   ___   _ ___   |  _ \\| |_   _ ___ \n" +
                        " | |\\/| | | \\ \\ / / | | / __|  | |_) | | | | / __|\n" +
                        " | |  | | | |\\ V /| |_| \\__ \\  |  __/| | |_| \\__ \\\n" +
                        " |_|  |_|_|_| \\_/  \\__,_|___/  |_|   |_|\\__,_|___/\n\n";

        System.out.println(banner);
    }
}