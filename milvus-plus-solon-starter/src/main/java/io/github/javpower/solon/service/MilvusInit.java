package io.github.javpower.solon.service;

import io.github.javpower.milvus.plus.service.AbstractMilvusClientBuilder;
import io.github.javpower.solon.entity.MilvusProperties;
import io.milvus.v2.client.MilvusClientV2;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.bean.LifecycleBean;
import org.springframework.beans.BeanUtils;

@Component
public class MilvusInit extends AbstractMilvusClientBuilder implements LifecycleBean {


    public static MilvusClientV2 client;
    @Inject
    MilvusProperties milvusProperties;

    // Spring会调用这个方法来初始化client
    @Init
    public void initialize() {
        io.github.javpower.milvus.plus.model.MilvusProperties milvusProperties1=new io.github.javpower.milvus.plus.model.MilvusProperties();
        BeanUtils.copyProperties(milvusProperties,milvusProperties1);
        super.setProperties(milvusProperties1);
        super.initialize();
        client = getClient();
    }

    @Override
    public void start() throws Throwable {

    }

    @Override
    public void stop() throws Throwable {
        super.close();
        LifecycleBean.super.stop();
    }
}