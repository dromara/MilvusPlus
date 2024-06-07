package org.dromara.milvus.demo;

import com.alibaba.fastjson.JSONObject;
import io.milvus.v2.service.vector.response.InsertResp;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.demo.mapper.FaceMilvusMapper;
import org.dromara.milvus.demo.model.Face;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Component
@Slf4j
public class ApplicationRunnerTest implements ApplicationRunner {
    private final FaceMilvusMapper mapper;

    public ApplicationRunnerTest(FaceMilvusMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        insertFace();
        getByIdTest();
        vectorQuery();
        scalarQuery();
    }

    private void insertFace() {
        List<Face> faces = LongStream.range(1, 10)
                .mapToObj(i -> {
                    Face faceTmp = new Face();
                    faceTmp.setPersonId(i);
                    List<Float> vectorTmp = IntStream.range(0, 128)
                            .mapToObj(j -> (float) (Math.random() * 100))
                            .collect(Collectors.toList());
                    faceTmp.setFaceVector(vectorTmp);
                    faceTmp.setPersonName(i % 2 == 0 ? "张三" + i : "李四" + i);
                    return faceTmp;
                })
                .collect(Collectors.toList());
        //新增
        MilvusResp<InsertResp> insert = mapper.insertWrapper()
                .partition("face_001")
                .insert(faces.iterator());
        log.info("insert--{}", JSONObject.toJSONString(insert));
    }

    public void getByIdTest() {
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9L);
        log.info("query--getById---{}", JSONObject.toJSONString(query));
    }

    public void vectorQuery() {
        //向量查询
        List<Float> vector = IntStream.range(0, 128)
                .mapToObj(i -> (float) (Math.random() * 100))
                .collect(Collectors.toList());
        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper().alias("alias_face")
                .vector(Face::getFaceVector, vector)
                .like(Face::getPersonName, "张三")
                .topK(3)
                .query();
        log.info("向量查询 query--queryWrapper---{}", JSONObject.toJSONString(query1));
    }

    public void scalarQuery() {
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .limit(3L)
                .query();
        log.info("标量查询   query--queryWrapper---{}", JSONObject.toJSONString(query2));
    }
}