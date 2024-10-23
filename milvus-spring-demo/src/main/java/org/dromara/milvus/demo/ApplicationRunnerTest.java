package org.dromara.milvus.demo;

import com.google.common.collect.Lists;
import io.milvus.v2.service.vector.request.ranker.RRFRanker;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.demo.model.Face;
import org.dromara.milvus.demo.model.FaceMilvusMapper;
import org.dromara.milvus.demo.model.Person;
import org.dromara.milvus.plus.core.conditions.LambdaQueryWrapper;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.service.MilvusService;
import org.dromara.milvus.plus.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private MilvusService milvusService;

    public ApplicationRunnerTest(FaceMilvusMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
//        insertFace();
//        selectFace(12);
//        selectFace(11);
//        delFace(11);
//        Thread.sleep(10000);
        countFace(22);
//        getByIdTest();
//        vectorQuery();
//        scalarQuery();
//        update();
    }

    private void selectFace(Integer temp){
        MilvusResp<List<MilvusResult<Face>>> query = mapper.
                queryWrapper()
                .eq(Face::getTemp, temp)
                .query(Face::getPersonName,Face::getTemp);
        log.info("query temp 11--{}", GsonUtil.toJson(query));
    }
    private void countFace(Integer temp){
        MilvusResp<Long> query = mapper.
                queryWrapper()
                .eq(Face::getTemp, temp)
                .count();
        log.info("query temp 11--{}", GsonUtil.toJson(query));
    }
    private void delFace(Integer temp){
        MilvusResp<DeleteResp> remove = mapper.deleteWrapper().eq(Face::getTemp, temp).remove();
        log.info("del temp 11 --{}", GsonUtil.toJson(remove));
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
                    faceTmp.setTemp(i%2==0?11:22);
                    return faceTmp;
                })
                .collect(Collectors.toList());
        MilvusResp<InsertResp> insert = milvusService.insert(faces);

//        //新增
//        MilvusResp<InsertResp> insert = mapper.insertWrapper()
//                .partition("face_001")
//                .insert(faces.iterator());
        log.info("insert--{}", GsonUtil.toJson(insert));
    }

    public void getByIdTest() {
        //id查询
//        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9L);
        MilvusResp<List<MilvusResult<Face>>> query = milvusService.getById(Face.class, 9L);
        log.info("query--getById---{}", GsonUtil.toJson(query));
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
//        log.info("向量查询 query--queryWrapper---{}", GsonUtil.toJson(query1));

        MilvusResp<List<MilvusResult<Face>>> query = mapper.queryWrapper().
                hybrid(new LambdaQueryWrapper<Face>().vector(Face::getFaceVector, vector).like(Face::getPersonName, "张三").topK(2)).
                hybrid(new LambdaQueryWrapper<Face>().vector(Face::getFaceVector, vector1).topK(4)).
                ranker(new RRFRanker(20)).
                topK(2).
                query();
        log.info("向量混合查询 query--queryWrapper---{}", GsonUtil.toJson(query));
    }

    public void scalarQuery() {
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
               // .eq(Face::getPersonId, 2L)
                .jsonContains("person_info[\"images\"]","https://baidu.com")
                .limit(3L)
                .query();
        log.info("标量查询   query--queryWrapper---{}", GsonUtil.toJson(query2));
    }
    public void update(){
        Face faceTmp = new Face();
        List<Float> vectorTmp = IntStream.range(0, 128)
                .mapToObj(j -> (float) (Math.random() * 100))
                .collect(Collectors.toList());
        faceTmp.setFaceVector(vectorTmp);
        faceTmp.setPersonName("赵六");
        MilvusResp<UpsertResp> resp = milvusService.updateById(faceTmp);
//        MilvusResp<UpsertResp> resp = mapper.updateWrapper().eq(FaceConstants.PERSON_NAME,"张三").update(faceTmp);
        System.out.printf("===="+ GsonUtil.toJson(resp));
    }
}