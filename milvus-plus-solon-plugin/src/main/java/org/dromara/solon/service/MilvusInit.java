package org.dromara.solon.service;

import org.dromara.milvus.plus.service.AbstractMilvusClientBuilder;
import org.dromara.solon.entity.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import org.noear.solon.annotation.*;
import org.noear.solon.core.bean.LifecycleBean;
import org.springframework.beans.BeanUtils;

@Configuration
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {

    //see https://solon.noear.org/article/324
    @Bean
    public MilvusClientV2 init(MilvusProperties milvusProperties) {
        org.dromara.milvus.plus.model.MilvusProperties milvusProperties1 = new org.dromara.milvus.plus.model.MilvusProperties();
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