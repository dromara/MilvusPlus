package org.dromara.milvus.demo;

import com.alibaba.fastjson.JSONObject;
import org.dromara.milvus.demo.model.Face;
import org.dromara.milvus.demo.test.FaceMilvusMapper;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import io.milvus.v2.service.vector.response.InsertResp;
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
    public void run(ApplicationArguments args){
       face();
    }
    private void face(){
        Face face=new Face();
        List<Float> vector = new ArrayList<>();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
        }
        face.setPersonId(1l);
        face.setFaceVector(vector);
        //新增
        List<Face> faces=new ArrayList<>();
        for (int i = 1; i < 10 ;i++){
            Face face1=new Face();
            face1.setPersonId(Long.valueOf(i));
            List<Float> vector1 = new ArrayList<>();
            for (int j = 0; j < 128; j++) {
                vector1.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
            }
            face1.setFaceVector(vector1);
            if(i%2==0){
                face1.setPersonName("张三"+i);
            }else {
                face1.setPersonName("李四"+i);
            }
            faces.add(face1);
        }
        MilvusResp<InsertResp> insert = mapper.insertWrapper().partition("face_001").insert(faces.toArray(new Face[0]));
        log.info("insert--{}", JSONObject.toJSONString(insert));
        //MilvusResp<InsertResp> insert = mapper.insert(faces.toArray(faces.toArray(new Face[0]))); log.info("insert--{}", JSONObject.toJSONString(insert));
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9l);
        log.info("query--getById---{}", JSONObject.toJSONString(query));
        //向量查询
        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper()
                .vector(Face::getFaceVector, vector)
                .like(Face::getPersonName, "张三")
                .topK(3)
                .query();
        log.info("向量查询 query--queryWrapper---{}", JSONObject.toJSONString(query1));
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .limit(3l)
                .query();
        log.info("标量查询   query--queryWrapper---{}", JSONObject.toJSONString(query2));

    }
}