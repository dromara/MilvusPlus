package io.github.javpower.milvus.plus.service;

import io.github.javpower.milvus.plus.annotation.MilvusCollection;
import io.github.javpower.milvus.plus.builder.CollectionSchemaBuilder;
import io.github.javpower.milvus.plus.cache.CollectionToPrimaryCache;
import io.github.javpower.milvus.plus.converter.MilvusEntityConverter;
import io.github.javpower.milvus.plus.model.MilvusEntity;
import io.github.javpower.milvus.plus.model.MilvusProperties;
import io.milvus.exception.MilvusException;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.*;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractMilvusClientBuilder implements MilvusClientBuilder {

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
            MilvusEntity milvusEntity = MilvusEntityConverter.convert(milvusClass);
            try {
                String collectionName = milvusEntity.getCollectionName();
                // 检查集合是否存在
                boolean collectionExists = client.hasCollection(
                        HasCollectionReq.builder().collectionName(collectionName).build()
                );
                if (collectionExists) {
                    // 获取集合的详细信息
                    DescribeCollectionResp collectionInfo = client.describeCollection(
                            DescribeCollectionReq.builder().collectionName(collectionName).build()
                    );
                    // 检查字段是否一致，这里需要实现字段比较逻辑
                    List<String> existingFieldNames = collectionInfo.getFieldNames();
                    List<AddFieldReq> requiredFields = milvusEntity.getMilvusFields();
                    List<String> requiredFieldNames = requiredFields.stream().map(AddFieldReq::getFieldName).collect(Collectors.toList());
                    if (!new HashSet<>(existingFieldNames).containsAll(requiredFieldNames) || !new HashSet<>(requiredFieldNames).containsAll(existingFieldNames)) {
                        // 字段不一致，删除并重新创建集合
                        client.dropCollection(
                                DropCollectionReq.builder().collectionName(collectionName).build()
                        );
                        // 创建新集合
                        create(milvusEntity,client);
                    }
                } else {
                    // 创建新集合
                    create(milvusEntity,client);
                }
                //加载集合
                loadStatus(collectionName);
            } catch (MilvusException e) {
                throw new RuntimeException("Error handling Milvus collection", e);
            }
        }
    }
    private void create(MilvusEntity milvusEntity,MilvusClientV2 client){
        // 创建新集合
        CollectionSchemaBuilder schemaBuilder = new CollectionSchemaBuilder(
                milvusEntity.getCollectionName(), client
        );
        schemaBuilder.addField(milvusEntity.getMilvusFields().toArray(new AddFieldReq[0]));
        List<IndexParam> indexParams = milvusEntity.getIndexParams();
        log.info("-------create schema---------");
        schemaBuilder.createSchema();
        if (indexParams != null && !indexParams.isEmpty()) {
            schemaBuilder.createIndex(indexParams);
            log.info("-------create index---------");
        }
    }
    private void loadStatus(String collectionName){
        //集合加载状态+加载集合
        GetLoadStateReq getLoadStateReq = GetLoadStateReq.builder()
                .collectionName(collectionName)
                .build();
        Boolean resp = client.getLoadState(getLoadStateReq);
        log.info("LoadState-->{}", resp);
        if(!resp){
            LoadCollectionReq loadCollectionReq = LoadCollectionReq.builder()
                    .collectionName(collectionName)
                    .build();
            client.loadCollection(loadCollectionReq);
            log.info("loadCollection-----");

        }
    }
}