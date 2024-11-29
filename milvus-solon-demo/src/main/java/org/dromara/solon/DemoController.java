package org.dromara.solon;

import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.extern.slf4j.Slf4j;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.util.GsonUtil;
import org.dromara.solon.test.model.Face;
import org.dromara.solon.test.test.FaceMilvusMapper;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
public class DemoController {
    @Inject
    FaceMilvusMapper mapper;
    @Mapping("/test")
    @Get
    public void hello() {


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
            faces.add(face1);
        }
        MilvusResp<InsertResp> insert = mapper.insert(faces.toArray(faces.toArray(new Face[0]))); log.info("insert--{}", GsonUtil.toJson(insert));
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9l);
        log.info("query--getById---{}", GsonUtil.toJson(query));
        //向量查询
        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper()
                .vector(Face::getFaceVector, vector)
                .ne(Face::getPersonId, 1L)
                .topK(3)
                .query();
        log.info("向量查询 query--queryWrapper---{}", GsonUtil.toJson(query1));
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .topK(3)
                .query();
        log.info("标量查询   query--queryWrapper---{}", GsonUtil.toJson(query2));
        //更新
        vector.clear();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
        }
        MilvusResp<UpsertResp> update = mapper.updateById(face);log.info("update--{}", GsonUtil.toJson(update));
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query3 = mapper.getById(1L);log.info("query--getById---{}", GsonUtil.toJson(query3));
        //删除
        MilvusResp<DeleteResp> remove = mapper.removeById(1L);log.info("remove--{}",GsonUtil.toJson(remove));
        //查询
        MilvusResp<List<MilvusResult<Face>>> query4 = mapper.getById(1L);log.info("query--{}", GsonUtil.toJson(query4));

    }
}