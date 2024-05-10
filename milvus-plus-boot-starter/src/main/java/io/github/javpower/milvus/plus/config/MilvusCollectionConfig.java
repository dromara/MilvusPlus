package io.github.javpower.milvus.plus.config;

import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.service.MilvusCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author xgc
 **/
@Component
public class MilvusCollectionConfig implements ApplicationRunner {

    private final ApplicationContext applicationContext;
    private final MilvusCollectionService milvusCollectionService;

    @Autowired(required = false)
    public MilvusCollectionConfig(ApplicationContext applicationContext, MilvusCollectionService milvusCollectionService) {
        this.applicationContext = applicationContext;
        this.milvusCollectionService = milvusCollectionService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 获取所有带有@MilvusCollection注解的类
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(MilvusCollection.class);
        Class<?>[] annotatedClasses = Arrays.stream(beanNames)
            .map(applicationContext::getType)
            .toArray(Class<?>[]::new);
        // 调用业务处理服务
        if(milvusCollectionService!=null){
            milvusCollectionService.performBusinessLogic(Arrays.asList(annotatedClasses));
        }
    }
}