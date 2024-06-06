package org.dromara.solon.service;

import io.milvus.v2.client.MilvusClientV2;
import org.dromara.milvus.plus.log.LogLevelController;
import org.dromara.milvus.plus.model.MilvusProperties;
import org.dromara.milvus.plus.service.AbstractMilvusClientBuilder;
import org.dromara.solon.entity.MilvusPropertiesConfiguration;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.core.bean.LifecycleBean;
import org.springframework.beans.BeanUtils;

@Configuration
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {

    //see https://solon.noear.org/article/324
    @Bean
    public MilvusClientV2 init(MilvusPropertiesConfiguration milvusPropertiesConfiguration) {
        printBanner();
        LogLevelController.setLoggingEnabledForPackage("org.dromara.milvus.plus", milvusPropertiesConfiguration.isOpenLog());
        MilvusProperties milvusProperties = new MilvusProperties();
        BeanUtils.copyProperties(milvusPropertiesConfiguration, milvusProperties);
        super.setProperties(milvusProperties);
        super.initialize();
        return getClient();
    }


    public void start() throws Throwable {

    }

    public void stop() throws Throwable {
        super.close();
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