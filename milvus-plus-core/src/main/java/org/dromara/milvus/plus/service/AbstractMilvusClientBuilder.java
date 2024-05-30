package org.dromara.milvus.plus.service;

import org.dromara.milvus.plus.annotation.MilvusCollection;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.model.MilvusProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class AbstractMilvusClientBuilder implements MilvusClientBuilder, ICMService {

    protected MilvusProperties properties;

    protected MilvusClientV2 client;
    private final  static  String CLASS="*.class";


    public void setProperties(MilvusProperties properties) {
        this.properties = properties;
    }

    @Override
    public void initialize() {
        if (properties.isEnable()) {
            ConnectConfig connectConfig = ConnectConfig.builder()
                    .uri(properties.getUri())
                    .token(properties.getToken())
                    .dbName(properties.getDbName())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .build();
            client = new MilvusClientV2(connectConfig);
            // 初始化逻辑
            handler();
        }
    }

    @Override
    public void close() throws InterruptedException {
        if (client != null) {
            //释放集合+释放client
            Set<String> co = CollectionToPrimaryCache.collectionToPrimary.keySet();
            if(co.size()>0){
                for (String name : co) {
                    ReleaseCollectionReq releaseCollectionReq = ReleaseCollectionReq.builder()
                            .collectionName(name)
                            .build();
                    client.releaseCollection(releaseCollectionReq);
                }
            }
            client.close(100);

        }
    }


    public void handler(){
        if(client!=null){
            List<Class<?>> classes = getClass(properties.getPackages());
            performBusinessLogic(classes);
        }
    }

    @Override
    public MilvusClientV2 getClient() {
        return client;
    }

    //获取指定包下实体类
    private List<Class<?>> getClass(List<String>  packages){
        List<Class<?>> res=new ArrayList<>();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        for (String pg : packages) {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(pg+".") + CLASS;
            try {
                Resource[] resources = resourcePatternResolver.getResources(pattern);
                MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                for (Resource resource : resources) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String classname = reader.getClassMetadata().getClassName();
                    Class<?> clazz = Class.forName(classname);
                    MilvusCollection annotation = clazz.getAnnotation(MilvusCollection.class);
                    if(annotation!=null){
                        res.add(clazz);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return res;
    }
    //缓存+是否构建集合
    public void performBusinessLogic(List<Class<?>> annotatedClasses) {
        for (Class<?> milvusClass : annotatedClasses) {
            createCollection(milvusClass);
        }
    }
}