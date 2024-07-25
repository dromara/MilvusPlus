package org.dromara.milvus.demo;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.InsertResp;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.demo.model.Face;
import org.dromara.milvus.demo.model.FaceMilvusMapper;
import org.dromara.milvus.demo.model.Person;
import org.dromara.milvus.plus.core.conditions.LambdaQueryWrapper;
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
//        getByIdTest();
        vectorQuery();
  //      scalarQuery();
  //        update();
    }

    private void insertFace() {
        List<Face> faces = LongStream.range(1, 10)
                .mapToObj(i -> {
                    Face faceTmp = new Face();
                   // faceTmp.setPersonId(i);
                    List<Float> vectorTmp = IntStream.range(0, 128)
                            .mapToObj(j -> (float) (Math.random() * 100))
                            .collect(Collectors.toList());
                    faceTmp.setFaceVector(vectorTmp);
                    faceTmp.setPersonName(i % 2 == 0 ? "张三" + i : "李四" + i);
                    Person person=new Person();
                    person.setName(faceTmp.getPersonName());
                    person.setAge((int) (i*100));
                    person.setImages(Lists.newArrayList("https://baidu.com"));
                    faceTmp.setPerson(person);
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
        List<Float> vector1 = IntStream.range(0, 128)
                .mapToObj(i -> (float) (Math.random() * 100))
                .collect(Collectors.toList());
//        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper()
//                .vector(Face::getFaceVector, new FloatVec(vector))
//                .like(Face::getPersonName, "张三")
//                .topK(3)
//                .query();
//        log.info("向量查询 query--queryWrapper---{}", JSONObject.toJSONString(query1));

        MilvusResp<List<MilvusResult<Face>>> query = mapper.queryWrapper().
                hybrid(new LambdaQueryWrapper<Face>().vector(Face::getFaceVector, new FloatVec(vector)).topK(2)).
                hybrid(new LambdaQueryWrapper<Face>().vector(Face::getFaceVector, new FloatVec(vector1)).topK(4)).
                ranker(new RRFRanker(20)).
                topK(2).
                query();
        log.info("向量混合查询 query--queryWrapper---{}", JSONObject.toJSONString(query));

    }

    public void scalarQuery() {
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
               // .eq(Face::getPersonId, 2L)
                .jsonContains("person_info[\"images\"]","https://baidu.com")
                .limit(3L)
                .query();
        log.info("标量查询   query--queryWrapper---{}", JSONObject.toJSONString(query2));
    }
//    public void update(){
//        Face faceTmp = new Face();
//        List<Float> vectorTmp = IntStream.range(0, 128)
//                .mapToObj(j -> (float) (Math.random() * 100))
//                .collect(Collectors.toList());
//        faceTmp.setFaceVector(vectorTmp);
//        faceTmp.setPersonName("赵六");
//        MilvusResp<UpsertResp> resp = mapper.updateWrapper().eq(FaceConstants.PERSON_NAME,"张三").update(faceTmp);
//        System.out.printf("===="+ JSON.toJSONString(resp));
//    }
}