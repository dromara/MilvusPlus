package io.github.javpower.milvus.demo;

import com.alibaba.fastjson.JSONObject;
import io.github.javpower.milvus.demo.model.Face;
import io.github.javpower.milvus.demo.test.FaceMilvusMapper;
import io.github.javpower.milvus.plus.model.vo.MilvusResp;
import io.github.javpower.milvus.plus.model.vo.MilvusResultVo;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ApplicationRunnerTest implements ApplicationRunner {
    private final FaceMilvusMapper mapper;

    public ApplicationRunnerTest(FaceMilvusMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        Face face=new Face();
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
        }
        face.setPersonId(1l);
        face.setFaceVector(vector);
        //新增
        MilvusResp<InsertResp> insert = mapper.insert(face); log.info("insert--{}", JSONObject.toJSONString(insert));
        Thread.sleep(1000);
        face.setPersonId(2l);
        MilvusResp<InsertResp> insert2 = mapper.insert(face); log.info("insert--{}", JSONObject.toJSONString(insert2));

        //id查询
        MilvusResp<List<Face>> query = mapper.getById(9l);
        log.info("query--getById---{}", JSONObject.toJSONString(query));
        //向量查询
        MilvusResp<MilvusResultVo<Face>> query1 = mapper.queryWrapper().vector(Face::getFaceVector, vector)
                .ne(Face::getPersonId,1L)
                .topK(3)
                .search();log.info("向量查询 query--queryWrapper---{}", JSONObject.toJSONString(query1));
        //标量查询
        MilvusResp<List<Face>> query4 = mapper.queryWrapper().vector(Face::getFaceVector, vector)
                .eq(Face::getPersonId, 2L)
                .topK(3)
                .query();log.info("标量查询   query--queryWrapper---{}", JSONObject.toJSONString(query4));
        //更新
        vector.clear();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
        }
        MilvusResp<UpsertResp> update = mapper.updateById(face);log.info("update--{}", JSONObject.toJSONString(update));
        //id查询
        MilvusResp<List<Face>> query2 = mapper.getById(1L);log.info("query--getById---{}", JSONObject.toJSONString(query2));
        //删除
        MilvusResp<DeleteResp> remove = mapper.removeById(1L);log.info("remove--{}", JSONObject.toJSONString(remove));
        //查询
        MilvusResp<List<Face>> query3 = mapper.getById(1L);log.info("query--{}", JSONObject.toJSONString(query3));

    }
}