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

import java.io.IOException;
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
    public void run(ApplicationArguments args) throws InterruptedException, IOException {

          insertFace();
//        Thread.sleep(1000);
  //        selectFace(11);
//        selectFace(11);
//        delFace(11);
//        Thread.sleep(10000);
//        countFace(22);
//        getByIdTest();
//        vectorQuery();
//        scalarQuery();
//        update();
//        selectTextEmbedding();
//        OkHttpClient client = new OkHttpClient();
//
//        MediaType mediaType = MediaType.parse("text/plain");
//        RequestBody body = RequestBody.create(mediaType, "{\"collectionName\":\"face_collection\",\"data\":[{\"person_name\":\"red\",\"person_id\":68,\"face_vector\":[0.11471413182829093,0.9506452386866311,0.3764267630711897,0.017116736332291937,0.7975288220162147,0.2837467793885893,0.45482217562314897,0.7512643321503942,0.5851774373796306,0.29498709649584076,0.5740173220271303,0.008283836490630359,0.043819261649065355,0.7510447248008454,0.26893026366305284,0.7295046143053174,0.7474013871060432,0.49231964145616947,0.6573919547221014,0.5648975702337511,0.9170353198810068,0.8043074271278763,0.8063592804980482,0.7815854305506693,0.3554478122511564,0.531480801706852,0.22560678943537482,0.5506750630287303,0.0022136957722376227,0.7276468730030954,0.6440332075744812,0.5698596907205946,0.5234591603448207,0.354993498425594,0.10620817906176727,0.07369212203402742,0.9813164411361883,0.2738860472889031,0.3271325745522793,0.050917994542803324,0.158202226640898,0.6272926953994711,0.5441414134787672,0.8910999073515409,0.8981121232379727,0.9985463611465781,0.43725436443853805,0.3120131342129935,0.24224544970671125,0.3681398853032727,0.5119269952123464,0.30916828759295356,0.6813753108853584,0.35691818319485047,0.43402512100213664,0.056672838867129594,0.7946916422488417,0.7178773219718055,0.45241404297020327,0.9306370216254345,0.09492427373318102,0.028570525877182007,0.1511031657904387,0.015414492250692469,0.06082024183250767,0.24318125198436924,0.8654445318335819,0.5892043659015123,0.019432939418877915,0.9345634565985648,0.588436900357431,0.7628378546017671,0.7127967940530804,0.5408600565378519,0.9451125627364227,0.50327646514356,0.624020091541091,0.06878519754282042,0.9023873103555076,0.06118535678364112,0.7727462265061942,0.5126902206187238,0.4101262307143809,0.5045701763198585,0.6097751212360454,0.26960758501937354,0.37397712778263625,0.16474237320190754,0.20184603623861452,0.025409236087541087,0.5683571865426373,0.47285055320039815,0.5046689191338607,0.9483211572128358,0.5880427237081676,0.7222636773561573,0.05098737933831754,0.9170210804631558,0.9631766161740742,0.9923927645980319,0.08716668133143668,0.8717887659732193,0.1638853403438041,0.8321367669259312,0.14796279048110583,0.5479872807310744,0.21622665177519096,0.4966695030300612,0.6457899721231348,0.9485180567239628,0.9643994129194424,0.31552473686825233,0.5185638828626413,0.26134044997524963,0.4307649919670753,0.27887814295025404,0.34751074846361885,0.41590517289697715,0.4460242095520486,0.6940091447850067,0.4794804720357553,0.24354302384948556,0.4511238595497853,0.40982391210503444,0.23674501807134418,0.3286329509283674,0.8590652585582985,0.9959059288831775]}]}");
//        Request request = new Request.Builder()
//                .url("http://154.201.90.228:19530/v2/vectordb/entities/insert")
//                .post(body)
////                .addHeader("Authorization", "Bearer 6fab5641a3156d2666feba14390e4ef4b6d376b5dce91faed303eec91a4bdb82239b70b29eb252b981daa3170516245818d4ee12")
//                .addHeader("Accept", "application/json")
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//        Response response = client.newCall(request).execute();
    }



    //    private void selectTextEmbedding(){
//        MilvusResp<List<MilvusResult<Face>>> xx = mapper
//                .queryWrapper()
//                .textVector(Face::getText, "whats the focus of information retrieval?")
//                .textMatch(Face::getText,"retrieval")
//                .topK(2)
//                .query();
//        System.out.println("===");
//    }
//    private void selectFace(Integer temp){
//        MilvusResp<List<MilvusResult<Face>>> query = mapper.
//                queryWrapper()
//                .eq(Face::getTemp, temp)
//                .query(Face::getPersonName,Face::getTemp);
//        log.info("query temp 11--{}", GsonUtil.toJson(query));
//
//        LambdaQueryWrapper<Face> mapper = milvusService.ofQuery(Face.class);
//        MilvusResp<List<MilvusResult<Face>>> test = mapper
//                .eq(Face::getSex, "男")
//                .topK(3)
//                .query();
//        log.info("query temp test--{}", GsonUtil.toJson(test));
//
//    }
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
        List<Face> faces = LongStream.range(1, 3)
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
//                    faceTmp.setText(i % 2 == 0 ?"nformation retrieval is a field of study.":"information retrieval focuses on finding relevant information in large datasets.");
//                    faceTmp.setAge(10);
//                    faceTmp.setSex("男");
                    return faceTmp;
                })
                .collect(Collectors.toList());
        MilvusResp<InsertResp> insert = milvusService.insert(faces);

//        //新增
//        MilvusResp<InsertResp> insert = mapper.insertWrapper()
//                .partition("face_001")
//                .insert(faces.iterator());
//        log.info("insert--{}", GsonUtil.toJson(insert));
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