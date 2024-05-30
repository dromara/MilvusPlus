package org.dromara.milvus.demo.java;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.dromara.milvus.demo.model.Face;
import org.dromara.milvus.plus.core.mapper.BaseMilvusMapper;
import org.dromara.milvus.plus.model.MilvusProperties;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.service.impl.MilvusClientBuild;
import io.milvus.v2.client.MilvusClientV2;

import java.util.List;

public class JavaTest {
    public static void main(String[] args) throws InterruptedException {
        MilvusProperties properties=new MilvusProperties();
        properties.setEnable(true);
        properties.setUri("https://in03-a5357975ab80da7.api.gcp-us-west1.zillizcloud.com");
        properties.setToken("xxx");
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
                .partition("face_01")
                .topK(3)
                .query();
        System.out.println("标量查询   query--queryWrapper---{}"+JSONObject.toJSONString(query2));
        build.close();
    }
}
