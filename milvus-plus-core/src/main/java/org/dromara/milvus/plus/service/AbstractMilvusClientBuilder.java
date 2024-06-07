package org.dromara.milvus.plus.service;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.ReleaseCollectionReq;
import io.milvus.v2.service.utility.response.ListAliasResp;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dromara.milvus.plus.annotation.MilvusCollection;
import org.dromara.milvus.plus.cache.CollectionToPrimaryCache;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.model.MilvusEntity;
import org.dromara.milvus.plus.model.MilvusProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractMilvusClientBuilder implements MilvusClientBuilder, ICMService {

    @Setter
    protected MilvusProperties properties;

    protected MilvusClientV2 client;
    private final static String CLASS = "*.class";


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
            // 释放集合+释放client
            Set<String> co = CollectionToPrimaryCache.collectionToPrimary.keySet();
            if (!co.isEmpty()) {
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


    public void handler() {
        if (Objects.isNull(client)) {
            log.warn("initialize handler over!");
        }
        List<Class<?>> classes = getClass(properties.getPackages());
        if (classes.isEmpty()) {
            log.warn("no any collections have been initialized, see if the [packages] parameter is configured correctly. :( !");
            return;
        }
        performBusinessLogic(classes);
    }

    @Override
    public MilvusClientV2 getClient() {
        return client;
    }

    // 获取指定包下实体类
    private List<Class<?>> getClass(List<String> packages) {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        return Optional.ofNullable(packages)
                .orElseThrow(() -> new RuntimeException("model package is null, please configure the [packages] parameter"))
                .stream()
                .map(pg -> {
                    List<Class<?>> res = new ArrayList<>();
                    String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                            + ClassUtils.convertClassNameToResourcePath(pg + ".") + CLASS;
                    try {
                        Resource[] resources = resourcePatternResolver.getResources(pattern);
                        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
                        for (Resource resource : resources) {
                            MetadataReader reader = readerFactory.getMetadataReader(resource);
                            String classname = reader.getClassMetadata().getClassName();
                            Class<?> clazz = Class.forName(classname);
                            MilvusCollection annotation = clazz.getAnnotation(MilvusCollection.class);
                            if (annotation != null) {
                                res.add(clazz);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return res;
                }).flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    // 缓存 + 是否构建集合
    public void performBusinessLogic(List<Class<?>> annotatedClasses) {
        for (Class<?> milvusClass : annotatedClasses) {
            MilvusEntity milvusEntity = MilvusConverter.convert(milvusClass);
            createCollection(milvusEntity);
            aliasProcess(milvusEntity);
        }
    }

    private void aliasProcess(MilvusEntity milvusEntity) {
        if (StringUtils.isBlank(milvusEntity.getCollectionName()) || milvusEntity.getAlias()==null|| milvusEntity.getAlias().isEmpty()) {
            return;
        }
        ListAliasResp listAliasResp = listAliases(milvusEntity);
        Optional.ofNullable(listAliasResp)
                .ifPresent(aliasInfo -> {
                    // 获取不存在的别名
                    List<String> aliasList = milvusEntity.getAlias().stream()
                            .filter(e -> !aliasInfo.getAlias().contains(e))
                            .filter(StringUtils::isNotBlank)
                            .collect(Collectors.toList());
                    log.info("processing alias: {}", aliasList);
                    milvusEntity.setAlias(aliasList);
                    createAlias(milvusEntity);
                });
    }
}