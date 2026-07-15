package org.dromara.milvus.plus.it;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;
import lombok.Data;
import org.dromara.milvus.plus.annotation.ExtraParam;
import org.dromara.milvus.plus.annotation.MilvusCollection;
import org.dromara.milvus.plus.annotation.MilvusField;
import org.dromara.milvus.plus.annotation.MilvusIndex;
import org.dromara.milvus.plus.cache.MilvusCache;
import org.dromara.milvus.plus.converter.MilvusConverter;
import org.dromara.milvus.plus.converter.SqlWhereTranslator;
import org.dromara.milvus.plus.core.conditions.LambdaQueryWrapper;
import org.dromara.milvus.plus.core.mapper.BaseMilvusMapper;
import org.dromara.milvus.plus.model.SchemaMode;
import org.dromara.milvus.plus.model.vo.MilvusResp;
import org.dromara.milvus.plus.model.vo.MilvusResult;
import org.dromara.milvus.plus.model.vo.PageResult;
import org.dromara.milvus.plus.service.ICMService;
import org.dromara.milvus.plus.service.SchemaSyncHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 真实 Milvus 联调（默认 localhost:19530）。
 * 未启动服务时自动 skip，不阻断纯单测。
 */
public class MilvusPlusIntegrationIT {

    private static final String URI = System.getProperty("milvus.uri", "http://localhost:19530");
    private static final String COLLECTION = "mp_it_face";
    private static boolean available;

    private MilvusClientV2 client;
    private TestMapper mapper;
    private ICMService icm;

    @Data
    @MilvusCollection(name = COLLECTION, enableDynamicField = true)
    public static class FaceDoc {
        @MilvusField(name = "id", dataType = DataType.Int64, isPrimaryKey = true, autoID = false)
        private Long id;

        @MilvusField(name = "person_name", dataType = DataType.VarChar, maxLength = 64)
        private String personName;

        @MilvusField(name = "status", dataType = DataType.Int32)
        private Integer status;

        @MilvusField(name = "face_vector", dataType = DataType.FloatVector, dimension = 8)
        @MilvusIndex(indexType = IndexParam.IndexType.IVF_FLAT, metricType = IndexParam.MetricType.L2,
                indexName = "face_vector_idx",
                extraParams = {@ExtraParam(key = "nlist", value = "64")})
        private List<Float> faceVector;
    }

    static class TestMapper extends BaseMilvusMapper<FaceDoc> {
        private final MilvusClientV2 client;

        TestMapper(MilvusClientV2 client) {
            this.client = client;
        }

        @Override
        public MilvusClientV2 getClient() {
            return client;
        }
    }

    @BeforeClass
    public static void checkServer() {
        available = pingHealth("http://localhost:9091/healthz") || canConnect(URI);
        if (!available) {
            System.out.println("[SKIP] Milvus not available at " + URI + " / healthz:9091");
        } else {
            System.out.println("[OK] Milvus available, uri=" + URI);
        }
    }

    private static boolean pingHealth(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(1500);
            c.setReadTimeout(1500);
            c.setRequestMethod("GET");
            return c.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean canConnect(String uri) {
        try {
            MilvusClientV2 c = new MilvusClientV2(ConnectConfig.builder().uri(uri).build());
            c.listCollections();
            c.close(3);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Before
    public void setUp() {
        org.junit.Assume.assumeTrue("Milvus server required", available);
        MilvusCache.clear();
        client = new MilvusClientV2(ConnectConfig.builder().uri(URI).build());
        mapper = new TestMapper(client);
        icm = () -> client;
        // clean leftover
        dropIfExists();
        SchemaSyncHelper.sync(MilvusConverter.convert(FaceDoc.class), client, SchemaMode.AUTO_ADD, false);
    }

    @After
    public void tearDown() {
        if (!available || client == null) {
            return;
        }
        try {
            dropIfExists();
        } catch (Exception ignored) {
        }
        try {
            client.close(3);
        } catch (Exception ignored) {
        }
        MilvusCache.clear();
    }

    private void dropIfExists() {
        Boolean has = client.hasCollection(HasCollectionReq.builder().collectionName(COLLECTION).build());
        if (Boolean.TRUE.equals(has)) {
            client.dropCollection(DropCollectionReq.builder().collectionName(COLLECTION).build());
        }
        // allow meta settle
        sleep(500);
    }

    @Test
    public void insert_query_search_delete_flow() {
        List<FaceDoc> docs = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            FaceDoc d = new FaceDoc();
            d.setId((long) i);
            d.setPersonName(i % 2 == 0 ? "zhang" + i : "li" + i);
            d.setStatus(i <= 3 ? 1 : 0);
            d.setFaceVector(randomVector(8, i));
            docs.add(d);
        }

        MilvusResp<InsertResp> insertResp = mapper.insert(docs);
        Assert.assertTrue(insertResp.isSuccess());
        flushSoft();

        // scalar filter + lambda
        MilvusResp<List<MilvusResult<FaceDoc>>> q1 = mapper.queryWrapper()
                .eq(FaceDoc::getStatus, 1)
                .query();
        Assert.assertTrue(q1.isSuccess());
        Assert.assertEquals(3, q1.getData().size());

        // complex or
        MilvusResp<List<MilvusResult<FaceDoc>>> q2 = mapper.queryWrapper()
                .eq(FaceDoc::getStatus, 0)
                .or(w -> w.eq(FaceDoc::getPersonName, "li1"))
                .query();
        Assert.assertTrue(q2.isSuccess());
        Assert.assertTrue(q2.getData().size() >= 2);

        // sqlWhere
        String translated = SqlWhereTranslator.toMilvusExpr("status = 1 AND person_name like 'li%'");
        Assert.assertTrue(translated.contains("==") || translated.contains("status"));
        MilvusResp<List<MilvusResult<FaceDoc>>> q3 = mapper.queryWrapper()
                .sqlWhere("status = 1 AND person_name like 'li%'")
                .query();
        Assert.assertTrue(q3.isSuccess());
        Assert.assertFalse(q3.getData().isEmpty());

        // raw filter
        MilvusResp<List<MilvusResult<FaceDoc>>> q4 = mapper.queryWrapper()
                .filter("status == 1")
                .query();
        Assert.assertEquals(3, q4.getData().size());

        // vector search
        MilvusResp<List<MilvusResult<FaceDoc>>> search = mapper.queryWrapper()
                .vectorSearch(FaceDoc::getFaceVector, randomVector(8, 1))
                .top(3)
                .query();
        Assert.assertTrue(search.isSuccess());
        Assert.assertFalse(search.getData().isEmpty());
        Assert.assertNotNull(search.getData().get(0).getScore() != null
                ? search.getData().get(0).getScore()
                : search.getData().get(0).getDistance());

        // page
        MilvusResp<PageResult<FaceDoc>> page = mapper.queryWrapper()
                .eq(FaceDoc::getStatus, 1)
                .page(1, 2);
        Assert.assertTrue(page.isSuccess());
        Assert.assertEquals(3, page.getData().getTotal());
        Assert.assertEquals(2, page.getData().getRecords().size());

        // getById
        MilvusResp<List<MilvusResult<FaceDoc>>> byId = mapper.getById(1L, 2L);
        Assert.assertEquals(2, byId.getData().size());

        // partial update
        FaceDoc patch = new FaceDoc();
        patch.setId(1L);
        patch.setPersonName("updated_li1");
        MilvusResp<UpsertResp> up = mapper.updateWrapper().partial(true).updateById(patch);
        Assert.assertTrue(up.isSuccess());
        flushSoft();
        FaceDoc loaded = mapper.getById(1L).getData().get(0).getEntity();
        Assert.assertEquals("updated_li1", loaded.getPersonName());

        // delete
        MilvusResp<DeleteResp> del = mapper.removeById(5L);
        Assert.assertTrue(del.isSuccess());
        flushSoft();
        Assert.assertTrue(mapper.getById(5L).getData().isEmpty()
                || mapper.getById(5L).getData().stream().allMatch(r -> r.getEntity() == null));
    }

    @Test
    public void ensureSchema_autoAdd_and_dynamicCollection() {
        // schema already created in setUp
        icm.ensureSchema(FaceDoc.class);

        String dyn = COLLECTION + "_t1";
        // drop dyn if exists
        if (Boolean.TRUE.equals(client.hasCollection(HasCollectionReq.builder().collectionName(dyn).build()))) {
            client.dropCollection(DropCollectionReq.builder().collectionName(dyn).build());
            sleep(300);
        }
        icm.ensureSchema(FaceDoc.class, dyn, SchemaMode.AUTO_ADD);

        FaceDoc d = new FaceDoc();
        d.setId(100L);
        d.setPersonName("tenant");
        d.setStatus(1);
        d.setFaceVector(randomVector(8, 9));
        MilvusResp<InsertResp> resp = mapper.forCollection(FaceDoc.class, dyn).insert(d);
        Assert.assertTrue(resp.isSuccess());
        flushSoft();
        List<MilvusResult<FaceDoc>> rows = mapper.forCollection(FaceDoc.class, dyn)
                .queryWrapper()
                .eq(FaceDoc::getId, 100L)
                .query()
                .getData();
        Assert.assertEquals(1, rows.size());
        Assert.assertEquals("tenant", rows.get(0).getEntity().getPersonName());

        client.dropCollection(DropCollectionReq.builder().collectionName(dyn).build());
    }

    @Test
    public void truncateCollection_keepsSchema() {
        FaceDoc d = new FaceDoc();
        d.setId(1L);
        d.setPersonName("x");
        d.setStatus(1);
        d.setFaceVector(randomVector(8, 1));
        mapper.insert(d);
        flushSoft();
        Assert.assertFalse(mapper.queryWrapper().query().getData().isEmpty());

        icm.truncateCollection(COLLECTION);
        flushSoft();
        // after truncate, query may return empty
        Assert.assertTrue(mapper.queryWrapper().query().getData().isEmpty());
        // can still insert
        mapper.insert(d);
        flushSoft();
        Assert.assertEquals(1, mapper.queryWrapper().query().getData().size());
    }

    private static List<Float> randomVector(int dim, int seed) {
        return IntStream.range(0, dim)
                .mapToObj(i -> (float) ((seed * 13 + i) % 100) / 100.0f)
                .collect(Collectors.toList());
    }

    private void flushSoft() {
        sleep(800);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
