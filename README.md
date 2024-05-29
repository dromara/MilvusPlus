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
    <version>2.4.0</version>
</dependency>
```


Spring：

```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.4.0</version>
</dependency>
```

Solon：

```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-solon-plugin</artifactId>
    <version>2.4.0</version>
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
@MilvusCollection(name = "face_collection") // Specifies the name of the Milvus collection
public class Face {
    @MilvusField(
            name = "person_id", // Field Name
            dataType = DataType.Int64, // Data type is 64-bit integer
            isPrimaryKey = true // Mark as Primary Key
    )
    private Long personId; // Unique identifier of the person

    @MilvusField(
            name = "face_vector", // Field Name
            dataType = DataType.FloatVector, // The data type is a floating point vector
            dimension = 128 // Vector dimension, assuming that the dimension of the face feature vector is 128
    )
    @MilvusIndex(
            indexType = IndexParam.IndexType.IVF_FLAT, // Using the IVF FLAT index type
            metricType = IndexParam.MetricType.L2, // Using the L 2 Distance Metric Type
            indexName = "face_index", // Index Name
            extraParams = { // Specify additional index parameters
                    @ExtraParam(key = "nlist", value = "100") // For example, the nlist parameter for IVF
            }
    )
    private List<Float> faceVector; // Storing vectors of face features
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
            vector.add((float) (Math.random() * 100)); // Using random numbers here as an example only
        }
        face.setPersonId(1l);
        face.setFaceVector(vector);
        
        // add
        List<Face> faces=new ArrayList<>();
        for (int i = 1; i < 10 ;i++){
            Face face1=new Face();
            face1.setPersonId(Long.valueOf(i));
            List<Float> vector1 = new ArrayList<>();
            for (int j = 0; j < 128; j++) {
                vector1.add((float) (Math.random() * 100)); // Using random numbers here as an example only
            }
            face1.setFaceVector(vector1);
            faces.add(face1);
        }
        MilvusResp<InsertResp> insert = mapper.insert(faces.toArray(faces.toArray(new Face[0]))); log.info("insert--{}", JSONObject.toJSONString(insert));
        
        // id query
        MilvusResp<List<MilvusResult<Face>>> query = mapper.getById(9l);
        log.info("query--getById---{}", JSONObject.toJSONString(query));
        
        // VECTOR QUERY
        MilvusResp<List<MilvusResult<Face>>> query1 = mapper.queryWrapper()
                .vector(Face::getFaceVector, vector)
                .ne(Face::getPersonId, 1L)
                .topK(3)
                .query();
        log.info("VectorQuery query--queryWrapper---{}", JSONObject.toJSONString(query1));
        
        // SCALAR QUERY
        MilvusResp<List<MilvusResult<Face>>> query2 = mapper.queryWrapper()
                .eq(Face::getPersonId, 2L)
                .limit(3)
                .query();
        log.info("ScalarQuery   query--queryWrapper---{}", JSONObject.toJSONString(query2));
        
        // update
        vector.clear();
        for (int i = 0; i < 128; i++) {
            vector.add((float) (Math.random() * 100)); // Using random numbers here as an example only
        }
        MilvusResp<UpsertResp> update = mapper.updateById(face);log.info("update--{}", JSONObject.toJSONString(update));
        
        // id Query
        MilvusResp<List<MilvusResult<Face>>> query3 = mapper.getById(1L);log.info("query--getById---{}", JSONObject.toJSONString(query3));
        
        // del
        MilvusResp<DeleteResp> remove = mapper.removeById(1L);log.info("remove--{}", JSONObject.toJSONString(remove));
        
        // query
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
