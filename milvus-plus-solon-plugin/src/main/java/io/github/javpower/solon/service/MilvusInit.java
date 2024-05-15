package io.github.javpower.solon.service;

import io.github.javpower.milvus.plus.service.AbstractMilvusClientBuilder;
import io.github.javpower.solon.entity.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import org.noear.solon.annotation.*;
import org.noear.solon.core.bean.LifecycleBean;
import org.springframework.beans.BeanUtils;

@Configuration
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {

    //see https://solon.noear.org/article/324
    @Bean
    public MilvusClientV2 init(MilvusProperties milvusProperties) {
        io.github.javpower.milvus.plus.model.MilvusProperties milvusProperties1 = new io.github.javpower.milvus.plus.model.MilvusProperties();
        BeanUtils.copyProperties(milvusProperties, milvusProperties1);
        super.setProperties(milvusProperties1);
        super.initialize();
        return getClient();
    }


    public void start() throws Throwable {

    }

    public void stop() throws Throwable {
        super.close();
    }
}