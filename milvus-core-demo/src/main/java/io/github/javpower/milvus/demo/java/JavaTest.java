package io.github.javpower.milvus.demo.java;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.github.javpower.milvus.demo.model.Face;
import io.github.javpower.milvus.plus.core.mapper.BaseMilvusMapper;
import io.github.javpower.milvus.plus.model.MilvusProperties;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResult;
import io.github.javpower.milvus.plus.service.impl.MilvusClientBuild;
import io.milvus.v2.client.MilvusClientV2;

import java.util.List;

public class JavaTest {
    public static void main(String[] args) throws InterruptedException {
        MilvusProperties properties=new MilvusProperties();
        properties.setEnable(true);
        properties.setUri("xxxxx");
        properties.setToken("xxxx");
        properties.setPackages(Lists.newArrayList("io.github.javpower.milvus.demo.model"));
        MilvusClientBuild build = new MilvusClientBuild(properties);
        build.initialize();
        MilvusClientV2 client = build.getClient();
        BaseMilvusMapper<Face> mapper=new BaseMilvusMapper<Face>() {
            @Override
            public MilvusClientV2 getClient() {
                return client;
            }
        };
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .topK(3)
                .query();
        System.out.println("标量查询   query--queryWrapper---{}"+JSONObject.toJSONString(query2));
        build.close();
    }
}
