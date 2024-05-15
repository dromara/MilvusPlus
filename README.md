# MilvusPlus: Enhanced Operations for Vector Databases

MilvusPlus is a powerful Java library that streamlines interactions with Milvus vector databases, offering an intuitive API for developers familiar with MyBatis-Plus style annotations and method invocations.

## Table of Contents

1. [Features](#features)
2. [Getting Started](#getting-started)
3. [Application Scenarios](#application-scenarios)
4. [Annotations](#annotations)
5. [Contributing](#contributing)
6. [License](#license)

## Features

- **Annotation-based Configuration**: Similar to MyBatis-Plus, use annotations to configure your entity models.
- **Intuitive API**: A straightforward API that makes vector database operations feel natural.
- **Extensible**: Designed with extensibility in mind, allowing for easy addition of new features.
- **Type-Safe**: Leverage Java's type safety to minimize errors.

## Getting Started

Add MilvusPlus to your project:

**Maven:**

Core：
```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-core</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```


Spring：

```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

Solon：

```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-solon-plugin</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```


## Application Scenarios

Vector databases are particularly useful in:

- **Similarity Search**: Quickly find items most similar to a given vector.
- **Recommendation Systems**: Recommend content based on user behavior and preferences.
- **Image Retrieval**: Identify images most similar to a query image in a large database.
- **Natural Language Processing**: Perform semantic searches by converting text to vectors.
- **Bioinformatics**: Analyze and compare biological sequences like proteins and genomes.

## Annotations

MilvusPlus introduces several annotations to map your Java entities to Milvus collections:

- `@MilvusCollection`: Denotes a Java class as a Milvus collection.
- `@MilvusField`: Maps a Java field to a Milvus field with options for data type, dimension, and more.
- `@MilvusIndex`: Defines an index on a Milvus field.

Example usage:


```java

@Data
@MilvusCollection(name = "face_collection") // 指定Milvus集合的名称
public class Face {
    @MilvusField(
            name = "person_id", // 字段名称
            dataType = DataType.Int64, // 数据类型为64位整数
            isPrimaryKey = true, // 标记为主键
    )
    private Long personId; // 人员的唯一标识符

    @MilvusField(
            name = "face_vector", // 字段名称
            dataType = DataType.FloatVector, // 数据类型为浮点型向量
            dimension = 128, // 向量维度，假设人脸特征向量的维度是128
    )
    @MilvusIndex(
            indexType = IndexParam.IndexType.IVF_FLAT, // 使用IVF_FLAT索引类型
            metricType = IndexParam.MetricType.L2, // 使用L2距离度量类型
            indexName = "face_index", // 索引名称
            extraParams = { // 指定额外的索引参数
                    @ExtraParam(key = "nlist", value = "100") // 例如，IVF的nlist参数
            }
    )
    private List<Float> faceVector; // 存储人脸特征的向量
}
```
```
@Component
public class FaceMilvusMapper extends MilvusMapper<Face> {
    
}

@Component
@Slf4j
public class ApplicationRunnerTest implements ApplicationRunner {
    private final FaceMilvusMapper mapper;

    public ApplicationRunnerTest(FaceMilvusMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void run(ApplicationArguments args){
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
        MilvusResp<InsertResp> insert = mapper.insert(faces.toArray(faces.toArray(new Face[0]))); log.info("insert--{}", JSONObject.toJSONString(insert));
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9l);
        log.info("query--getById---{}", JSONObject.toJSONString(query));
        //向量查询
        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper()
                .vector(Face::getFaceVector, vector)
                .ne(Face::getPersonId, 1L)
                .topK(3)
                .query();
        log.info("向量查询 query--queryWrapper---{}", JSONObject.toJSONString(query1));
        //标量查询
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .limit(3)
                .query();
        log.info("标量查询   query--queryWrapper---{}", JSONObject.toJSONString(query2));
        //更新
        vector.clear();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // 这里仅作为示例使用随机数
        }
        MilvusResp<UpsertResp> update = mapper.updateById(face);log.info("update--{}", JSONObject.toJSONString(update));
        //id查询
        MilvusResp<List<MilvusResult<Face>>> query3 = mapper.getById(1L);log.info("query--getById---{}", JSONObject.toJSONString(query3));
        //删除
        MilvusResp<DeleteResp> remove = mapper.removeById(1L);log.info("remove--{}", JSONObject.toJSONString(remove));
        //查询
        MilvusResp<List<MilvusResult<Face>>> query4 = mapper.getById(1L);log.info("query--{}", JSONObject.toJSONString(query4));

    }
}
```

## Contributing

Contributions are welcome!

- Report issues or suggest features by [opening an issue](https://github.com/yourusername/MilvusPlus/issues/new).
- Submit changes by [creating a pull request](https://github.com/yourusername/MilvusPlus/compare).

## License

MilvusPlus is open source and available under the [License](https://github.com/yourusername/MilvusPlus/blob/master/LICENSE).

## Contact

For questions or support, reach out to [javpower@163.com](mailto:javpower@163.com).
