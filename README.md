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
            autoID = true // 假设这个ID是自动生成的
    )
    private Long personId; // 人员的唯一标识符

    @MilvusField(
            name = "face_vector", // 字段名称
            dataType = DataType.FloatVector, // 数据类型为浮点型向量
            dimension = 128, // 向量维度，假设人脸特征向量的维度是128
            isPartitionKey = false // 假设这个字段不是分区键
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
public class ApplicationRunnerTest implements ApplicationRunner {
    @Autowired
    private FaceMilvusMapper mapper;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Float> vector = Lists.newArrayList(0.1f,0.2f,0.3f);

        //查询
        MilvusResp<MilvusResultVo<Face>> query =  mapper.queryWrapper()
                .eq(Face::getPersonId, 1l)
                .vector(Face::getFaceVector,vector)
                .limit(100l)
                .query();
        MilvusResp<List<Face>> query2 = mapper.getById(1l);


        //删除
        MilvusResp<DeleteResp> remove= mapper.deleteWrapper()
                .eq(Face::getPersonId, 1l)
                .id(111)
                .remove();
        MilvusResp<DeleteResp> remove2 = mapper.removeById(1l);

        //更新
        Face face=new Face();
        face.setFaceVector(vector);
        MilvusResp<UpsertResp> update = mapper.updateWrapper()
                .eq(Face::getPersonId, 1l)
                .update(face);
        face.setPersonId(1l);
        MilvusResp<UpsertResp> update2 = mapper.updateById(face);
        
         //新增
        MilvusResp<InsertResp> insert = mapper.insertWrapper()
                .put(Face::getFaceVector, vector)
                .insert();
        MilvusResp<InsertResp> insert2 = mapper.insert(face);
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
