# Dromara MilvusPlus: Enhanced Vector Database Operations Library

## Project Introduction

<div style="display: inline-block; border: 4px solid #ccc; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); margin: 10px; padding: 10px;">
  <img src="./logo/milvus.png" alt="MilvusPlus" style="border-radius: 10px;" />
</div>

> 🔥🔥🔥 [MilvusPlus](https://milvus-plus.m78cloud.cn) (short for MP) is an operational tool for [Milvus](https://milvus.io), designed to simplify interactions with the Milvus vector database, providing developers with an intuitive API similar to MyBatis-Plus annotations and method call style, born to improve efficiency.

## Features

- **Non-Invasive**: It only enhances without making changes; its introduction will not affect existing projects, as smooth as silk.
- **Low Overhead**: It automatically injects basic CRUD operations upon startup, with almost no performance loss, and operates directly on objects.
- **Powerful CRUD Operations**: The universal MilvusMapper can achieve CRUD operations with just a small amount of configuration, and has a powerful condition builder to meet all kinds of usage needs.
- **Intuitive API**: The direct API design simplifies database operations, and MilvusService provides a rich API.
- **Support for Lambda-style Calls**: Lambda expressions make it easy to write various query conditions without worrying about field errors.
- **Support for Automatic Primary Key Generation**: Perfectly solves the primary key issue.
- **Support for Custom Global Operations**: Supports global method injection.
- **Annotation-based Configuration**: Uses an annotation method similar to MyBatis-Plus to configure entity models.
- **Easy to Extend**: The core design focuses on extensibility.
- **Type Safety**: Uses Java type safety to reduce errors.

## Quick Start

Custom extension support:

```
<dependency>
    <groupId>org.dromara.milvus-plus</groupId>
    <artifactId>milvus-plus-core</artifactId>
    <version>2.2.4</version>
</dependency>
```

Spring application support:

```
<dependency>
    <groupId>org.dromara.milvus-plus</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.2.4</version>
</dependency>
```

Solon application support:

```
<dependency>
    <groupId>org.dromara.milvus-plus</groupId>
    <artifactId>milvus-plus-solon-plugin</artifactId>
    <version>2.2.4</version>
</dependency>
```

## Notes

- Version 2.0.0 requires the use of index annotations to define indexes; otherwise, an error will occur at startup, and adding them later will be ineffective, requiring the collection to be deleted first.
- Version 2.0.0 has not yet released the MilvusService functionality.

## Configuration File

```
milvus:
  uri: https://in03-a5357975ab80da7.api.gcp-us-west1.zillizcloud.com
  token: x'x'x'x
  enable: true
  packages:
    - com.example.entity
```

- `milvus`: Defines configurations related to the Milvus service.
    - `uri`: The URI of the Milvus service, through which the application communicates with the Milvus service.
    - `token`: A token for verification and authorization, ensuring the security of access to the Milvus service.
    - `enable`: A boolean value indicating whether the Milvus module should be enabled.
    - `packages`: These packages contain Java classes corresponding to custom annotations, which you can consider as the package where your custom entity classes are located.

## Application Scenarios

- **Similarity Search**: Quickly retrieve items most similar to a given vector.
- **Recommendation System**: Recommend relevant content based on user behavior and preferences.
- **Image Retrieval**: Find the most similar images to the query image in a large-scale image library.
- **Natural Language Processing**: Convert text into vectors and perform semantic searches.
- **Bioinformatics**: Analyze and compare biological sequences, such as protein and genomic data.

## Detailed Explanation of Custom Annotations

Using custom annotations to automate the integration of the Milvus database provides the following significant advantages:

- **Simplifies the Development Process**: The database structure is declared directly in the code through annotations, without the need to manually create collections, attributes, indexes, and partitions. The project starts automatically and builds, reducing the need to manually write Milvus API calls.
- **Improves Development Efficiency**: The annotation-driven approach makes the creation and management of the database structure more convenient, speeding up development.
- **Enhances Code Readability**: Tightly integrates the definition of the database structure with business logic code, improving the readability and maintainability of the code.
- **Reduces Errors**: Automated creation of the database structure reduces the possibility of human errors, improving the stability of the system.
- **Easy to Maintain**: The use of annotations makes changes to the database structure more centralized and clear, facilitating later maintenance and upgrades.

### @ExtraParam Annotation

- **Purpose**: Defines additional parameters for indexes or other custom functions.
- **Attributes**:
    - `key()`: The key name of the parameter.
    - `value()`: The value of the parameter.

### @MilvusCollection Annotation

- **Purpose**: Defines a collection in the Milvus database.
- **Attributes**:
    - `name()`: The name of the collection.

### @MilvusField Annotation

- **Purpose**: Defines a field in the Milvus collection.
- **Attributes**:
    - `name()`: The field name, defaulting to the Java field name.
    - `dataType()`: The data type, defaulting to `FLOAT_VECTOR`.
    - `dimension()`: The vector dimension, defaulting to -1.
    - `isPrimaryKey()`: Whether it is the primary key, defaulting to false.
    - `autoID()`: Whether to automatically generate an ID, defaulting to false.
    - `description()`: The field description, defaulting to empty.
    - `elementType()`: The element type, defaulting to `None`.
    - `maxLength()`: The maximum length, defaulting to -1.
    - `maxCapacity()`: The maximum capacity, defaulting to -1.
    - `isPartitionKey()`: Whether it is a partition key, defaulting to false.

### @MilvusIndex Annotation

- **Purpose**: Defines an index in the Milvus collection.
- **Attributes**:
    - `indexType()`: The index type, defaulting to `FLAT`.
    - `metricType()`: The metric type, defaulting to `L2`.
    - `indexName()`: The index name, defaulting to empty.
    - `extraParams()`: Additional parameters, defined using the `ExtraParam` annotation.

### @MilvusPartition Annotation

- **Purpose**: Defines partitions of the Milvus collection.
- **Attributes**:
    - `name()`: An array of partition names.

Through these annotations, developers can easily define and manage the structure of the Milvus database, achieving the goal of automatically building the required database structure when the project starts.

## Detailed Explanation of Index and Metric Types

### Index Types (IndexType)

- **INVALID**: An invalid index type, used only for internal marking.
- **FLAT**: Brute force search, suitable for small-scale datasets.
- **IVF_FLAT**: Inverted file flat mode, suitable for medium-scale datasets.
- **IVF_SQ8**: Inverted file quantization mode, suitable for large-scale datasets, sacrificing accuracy for speed.
- **IVF_PQ**: Inverted file product quantization mode, suitable for large-scale high-dimensional datasets, balancing speed and accuracy.
- **HNSW**: Hierarchical navigation small-world graph, providing fast search, suitable for large-scale datasets.
- **DISKANN**: Disk-based approximate nearest neighbor search, suitable for large-scale datasets stored on disk.
- **AUTOINDEX**: Automatically selects the optimal index type.
- **SCANN**: Accelerates search using scanning and tree structures.
- **GPU_IVF_FLAT, GPU_IVF_PQ**: GPU-accelerated indexes, suitable for GPU environments.
- **BIN_FLAT, BIN_IVF_FLAT**: Dedicated index for binary vectors.
- **TRIE**: A dictionary tree index suitable for string types.
- **STL_SORT**: A sorting index suitable for scalar fields.

### Metric Types (MetricType)

- **INVALID**: An invalid metric type, used only for internal marking.
- **L2**: Euclidean distance, suitable for floating-point vectors.
- **IP**: Inner product, used for calculating cosine similarity.
- **COSINE**: Cosine similarity, suitable for text and image searches.
- **HAMMING**: Hamming distance, suitable for binary vectors.
- **JACCARD**: Jaccard similarity coefficient, suitable for set similarity calculations.

## MilvusMapper Functionality

`MilvusMapper` is a general-purpose interface for operating the Milvus database, providing a series of data manipulation methods, including querying, deleting, updating, and inserting. The following is a functional description of `MilvusMapper` and its related classes:

### MilvusMapper<T>

`MilvusMapper` is a generic abstract class that inherits from `BaseMilvusMapper`, providing basic methods for interacting with the Milvus client.

- **Get Milvus Client**: `getClient()` - Returns a `MilvusClientV2` instance.

### BaseMilvusMapper<T>

`BaseMilvusMapper` is an abstract class that defines basic operations for interacting with the Milvus database.

- **Create Search Builder Instance**: `queryWrapper()` - Creates a `LambdaQueryWrapper` instance.
- **Create Delete Builder Instance**: `deleteWrapper()` - Creates a `LambdaDeleteWrapper` instance.
- **Create Update Builder Instance**: `updateWrapper()` - Creates a `LambdaUpdateWrapper` instance.
- **Create Insert Builder Instance**: `insertWrapper()` - Creates a `LambdaInsertWrapper` instance.

#### Data Operations

- **Get Data by ID**: `getById(Serializable ... ids)`
    - **Function**: Query data based on the provided list of IDs.
    - **Parameters**: `ids` - A list of serializable IDs.
    - **Return**: `MilvusResp<List<MilvusResult<T>>>` - The response containing the query results.

- **Delete Data**: `removeById(Serializable ... ids)`
    - **Function**: Delete data based on the provided list of IDs.
    - **Parameters**: `ids` - A list of serializable IDs.
    - **Return**: `MilvusResp<DeleteResp>` - The response of the deletion operation.

- **Update Data**: `updateById(T ... entity)`
    - **Function**: Update data based on the provided entities.
    - **Parameters**: `entity` - A list of entity objects.
    - **Return**: `MilvusResp<UpsertResp>` - The response of the update operation.

- **Insert Data**: `insert(T ... entity)`
    - **Function**: Insert the provided entities into the database.
    - **Parameters**: `entity` - A list of entity objects.
    - **Return**: `MilvusResp<InsertResp>` - The response of the insertion operation.

#### Builder Methods

- **Create General Builder Instance**: `lambda(Wrapper<W, T> wrapper)` - Initializes and returns a builder instance.

### LambdaQueryWrapper<T> Class Functional Documentation

`LambdaQueryWrapper<T>` is a builder class used to construct and execute Milvus search queries. It provides a series of methods to set query parameters and ultimately execute the query.

#### Constructors

- **LambdaQueryWrapper()**: No-argument constructor.
- **LambdaQueryWrapper(String collectionName, MilvusClientV2 client, ConversionCache conversionCache, Class<T> entityType)**: Constructor that initializes the collection name, Milvus client, type conversion cache, and entity type.

#### Partition Settings

- **partition(String ... partitionName)**: Adds one or more partition names to the query.
- **partition(FieldFunction<T,?>... partitionName)**: Adds partition names based on the provided field functions.

#### Search Parameter Settings

- **searchParams(Map<String, Object> searchParams)**: Sets search parameters.

- The following are the parameters supported by searchParams and their descriptions:
    - metric_type
      Type: String
      Description: Specifies the metric type used for the search operation. It must be consistent with the metric type used when indexing vector fields.
      Optional values:
      L2: Euclidean distance, suitable for vector searches in high-dimensional spaces.
      IP: Inner product, suitable for cosine similarity searches.
      COSINE: Cosine similarity, the same as inner product, suitable for measuring the angle between vectors.
      Example:
      searchParams.put("metric_type", "L2");
    - radius
      Type: float
      Description: Sets the minimum similarity threshold for the search operation. When metric_type is set to L2, this value should be greater than range_filter; otherwise, it should be less than range_filter.
      Example:
      searchParams.put("radius", 0.5f);
    - range_filter
      Type: float
      Description: Limits the similarity range of the search operation. When metric_type is set to IP or COSINE, this value should be greater than radius; otherwise, it should be less than radius.
      Example:
      searchParams.put("range_filter", 0.3f);
      Use Example
      The following is an example of using searchParams, showing how to build a search request and set specific search parameters:

```java
Map<String, Object> searchParams = new HashMap<>();
searchParams.put("metric_type", "L2");
searchParams.put("radius", 0.5f);
searchParams.put("range_filter", 0.3f);
```

- **radius(Object radius)**: Sets the search radius.
- **rangeFilter(Object rangeFilter)**: Sets the range filter.
- **metricType(Object metric_type)**: Sets the metric type.

#### Result Settings

- **outputFields(List<String> outputFields)**: Sets the fields to be returned.
- **roundDecimal(int roundDecimal)**: Sets the number of decimal places for the returned distance values.

#### Query Condition Construction

- **eq(String fieldName, Object value)**: Adds an equal condition.
- **ne(String fieldName, Object value)**: Adds a not equal condition.
- **gt(String fieldName, Object value)**: Adds a greater than condition.
- **ge(String fieldName, Object value)**: Adds a greater than or equal condition.
- **lt(String fieldName, Object value)**: Adds a less than condition.
- **le(String fieldName, Object value)**: Adds a less than or equal condition.
- **between(String fieldName, Object start, Object end)**: Adds a range condition.
- **isNull(String fieldName)**: Adds a null check condition.
- **isNotNull(String fieldName)**: Adds a not null check condition.
- **in(String fieldName, List<?> values)**: Adds an IN condition.
- **like(String fieldName, String value)**: Adds a LIKE condition.

#### JSON and Array Operations

- **jsonContains(String fieldName, Object value)**: Adds a JSON contains condition.
- **jsonContainsAll(String fieldName, List<?> values)**: Adds a JSON contains all values condition.
- **jsonContainsAny(String fieldName, List<?> values)**: Adds a JSON contains any value condition.
- **arrayContains(String fieldName, Object value)**: Adds an array contains condition.
- **arrayContainsAll(String fieldName, List<?> values)**: Adds an array contains all values condition.
- **arrayContainsAny(String fieldName, List<?> values)**: Adds an array contains any value condition.
- **arrayLength(String fieldName, int length)**: Adds an array length condition.

#### Logical Operations

- **and(ConditionBuilder<T> other)**: Adds an AND condition.
- **or(ConditionBuilder<T> other)**: Adds an OR condition.
- **not()**: Adds a NOT condition.

#### Vector Search Settings

- **annsField(String annsField)**: Sets the vector field to be searched.
- **vector(List<?> vector)**: Adds the vector to be searched.
- **vector(String annsField, List<?> vector)**: Sets the vector field and adds the vector to be searched.
- **topK(Integer topK)**: Sets the top-k results to be returned.
- **limit(Long limit)**: Sets the limit on the number of query results.

#### Executing Queries

- **query()**: Builds and executes the search request, returning a wrapped `MilvusResp` object containing the query results.
- **query(FieldFunction<T,?> ... outputFields)**: Sets the output fields and executes the query.
- **query(String ... outputFields)**: Sets the output fields and executes the query.
- **getById(Serializable ... ids)**: Gets data by ID.

#### Helper Methods

- **buildSearch()**: Builds a complete search request object.
- **buildQuery()**: Builds a query request object.

The `LambdaQueryWrapper<T>` class provides a wealth of methods to build complex search queries, supporting various conditions, logical operations, JSON and array operations, and vector searches. By calling these methods in a chain, users can flexibly construct search requests and obtain the desired query results.

### LambdaDeleteWrapper<T>

`LambdaDeleteWrapper` is a builder class used to construct and execute deletion operations.

- **Add Partition**: `partition(String partitionName)`
- **Add Equal Condition**: `eq(String fieldName, Object value)`
- **Add Not Equal Condition**: `ne(String fieldName, Object value)`
- **Add ID to Deletion List**: `id(Object id)`

#### Executing Deletion

- **Execute Deletion**: `remove()` - Builds and executes the deletion request.
- **Delete by ID**: `removeById(Serializable ... ids)`

### LambdaUpdateWrapper<T>

`LambdaUpdateWrapper` is a builder class used to construct and execute update operations.

- **Add Partition**: `partition(String partitionName)`
- execute insertion operations.

- **Add Partition**: `partition(String partitionName)`
- **Add Field Value**: `put(String fieldName, Object value)`

#### Executing Insertion

- **Insert Data**: `insert()` - Builds and executes the insertion request.
- **Insert Multiple Data**: `insert(T ... t)`

## MilvusService Functionality

`MilvusService` is a comprehensive service that provides full management of the Milvus database. It implements multiple interfaces: `IAMService` (Identity and Access Management Service), `ICMService` (Collection Management Service), and `IVecMService` (Vector Management Service).

### Identity and Access Management (IAMService)

The `IAMService` interface provides functions for creating, deleting, querying users and roles, as well as granting and revoking permissions.

- **Create Role**: `createRole(String roleName)`
- **Create User**: `createUser(String userName, String password)`
- **Describe Role Permissions**: `describeRole(String roleName)`
- **Describe User Information**: `describeUser(String userName)`
- **Drop Role**: `dropRole(String roleName)`
- **Drop User**: `dropUser(String userName)`
- **Grant Role Permissions**: `grantPrivilege(String roleName, String objectType, String privilege, String objectName)`
- **Grant User Role**: `grantRole(String roleName, String userName)`
- **List All Roles**: `listRoles()`
- **List All Users**: `listUsers()`
- **Revoke Role Permissions**: `revokePrivilege(String roleName, String objectType, String privilege, String objectName, String databaseName)`
- **Revoke User Role**: `revokeRole(String roleName, String userName)`
- **Update User Password**: `updatePassword(String userName, String password, String newPassword)`

### Collection Management (ICMService)

The `ICMService` interface provides functions for creating, deleting, querying, renaming, and managing indexes of collections.

- **Create Collection**: `createCollection(MilvusEntity milvusEntity)`
- **Add Field**: `addField(String collectionName, AddFieldReq ... addFieldReq)`
- **Get Field**: `getField(String collectionName, String fieldName)`
- **Describe Collection**: `describeCollection(String collectionName)`
- **Drop Collection**: `dropCollection(String collectionName)`
- **Check if Collection Exists**: `hasCollection(String collectionName)`
- **Get Collection Statistics**: `getCollectionStats(String collectionName)`
- **Rename Collection**: `renameCollection(String oldCollectionName, String newCollectionName)`
- **Create Index for Collection**: `createIndex(String collectionName, List<IndexParam> indexParams)`
- **Describe Index of Collection**: `describeIndex(String collectionName, String fieldName)`
- **Drop Index of Collection**: `dropIndex(String collectionName, String fieldName)`
- **Get Loading Status of Collection or Partition**: `getLoadState(String collectionName, String partitionName)`
- **Load Collection Data into Memory**: `loadCollection(String collectionName)`
- **Release Collection Data from Memory**: `releaseCollection(String collectionName)`
- **Create Partition in Collection**: `createPartition(String collectionName, String partitionName)`
- **Drop Partition in Collection**: `dropPartition(String collectionName, String partitionName)`
- **Check if Partition Exists**: `hasPartition(String collectionName, String partitionName)`
- **List All Partitions in Collection**: `listPartitions(String collectionName)`
- **Load Partitions of Collection into Memory**: `loadPartitions(String collectionName, List<String> partitionNames)`
- **Release Partitions of Collection from Memory**: `releasePartitions(String collectionName, List<String> partitionNames)`

### Vector Management (IVecMService)

The `IVecMService` interface provides functions for inserting, updating, querying, deleting vectors, and performing similarity searches.

- **Delete Entities**: `delete(String collectionName, String partitionName, String filter, List<Object> ids)`
- **Get Entities by ID**: `get(String collectionName, String partitionName, List<Object> ids, List<String> outputFields)`
- **Insert Data**: `insert(String collectionName, List<JSONObject> data, String partitionName)`
- **Query by Scalar Filter Condition**: `query(String collectionName, List<String> partitionNames, List<String> outputFields, List<Object> ids, String filter, ConsistencyLevel consistencyLevel, long offset, long limit)`
- **Perform Vector Similarity Search**: `search(String collectionName, List<String> partitionNames, String annsField, int topK, String filter, List<String> outputFields, List<Object> data, long offset, long limit, int roundDecimal, Map<String, Object> searchParams, long guaranteeTimestamp, long gracefulTime, ConsistencyLevel consistencyLevel, boolean ignoreGrowing)`
- **Upsert Data**: `upsert(String collectionName, String partitionName, List<JSONObject> data)`

### Public Method

In addition to the above functionalities, `MilvusService` also provides a public method to obtain a `MilvusClientV2` instance:

- **Get Milvus Client**: `getClient()`

## Usage Example

Here is an example of using MilvusPlus for vector search:

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
