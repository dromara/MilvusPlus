#  MilvusPlus：向量数据库增强操作库

## 项目简介

![MilvusPlus](./logo/MilvusPlus.jpg)

> 🔥🔥🔥[MilvusPlus](https://gitee.com/dromara/MilvusPlus)（简称 MP）是一个 [Milvus](https://milvus.io) 的操作工具，旨在简化与 Milvus 向量数据库的交互，为开发者提供类似 MyBatis-Plus 注解和方法调用风格的直观 API,提高效率而生。

## 特性

- **无侵入**：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑
- **损耗小**：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作
- **强大的 CRUD 操作**：通用 MilvusMapper，仅仅通过少量配置即可实现 CRUD 操作，更有强大的条件构造器，满足各类使用需求
- **支持 Lambda 形式调用**：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错
- **支持主键自动生成**：完美解决主键问题
- **支持自定义全局通用操作**：支持全局通用方法注入

- **注解式配置**：采用与 MyBatis-Plus 类似的注解方式配置实体模型。
- **直观的 API**：直接的 API 设计简化数据库操作。
- **易于扩展**：核心设计注重可扩展性。
- **类型安全**：利用 Java 类型安全减少错误。

## 快速开始

自定义扩展支持：

```
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-core</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

Spring应用支持：

```
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

Solon应用支持：

```
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-solon-plugin</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

快照版本需额外加入
```
<repositories>
  <repository>
      <id>ossrh</id>
      <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
          <enabled>true</enabled>
      </releases>
      <snapshots>
          <enabled>true</enabled>
      </snapshots>
  </repository>
</repositories>
```

## 需知
版本
2.4.0 废除
2.4.0-RC1 废除

## 配置文件

```
milvus:
  uri: https://in03-a5357975ab80da7.api.gcp-us-west1.zillizcloud.com
  token: x'x'x'x
  enable: true
  packages:
    - io.github.javpower.milvus.demo.model
```

- `milvus`：定义了与Milvus服务相关的配置。
    - `uri`：Milvus服务的URI，应用程序通过这个URI与Milvus服务进行通信。
    - `token`：用于验证和授权的令牌（Token），确保访问Milvus服务的安全性。
    - `enable`：一个布尔值，用于指示Milvus模块是否应该被启用。
    - `packages`：这些包包含了自定义注解对应的Java类。

## 应用场景

- **相似性搜索**：快速检索与给定向量最相似的项。
- **推荐系统**：根据用户行为和偏好推荐相关内容。
- **图像检索**：在大规模图像库中找到与查询图像最相似的图像。
- **自然语言处理**：将文本转换为向量并执行语义搜索。
- **生物信息学**：分析和比较生物序列，如蛋白质和基因组数据。

## 自定义注解详解

使用自定义注解自动化Milvus数据库集成，提供了以下显著优势：

- **简化开发流程**：通过注解直接在代码中声明数据库结构，不用手动创建集合、属性、索引、分区，项目启动即自动构建，减少手动编写Milvus API调用的需要。
- **提高开发效率**：注解驱动的方式使得数据库结构的创建和管理更加快捷，加快开发速度。
- **增强代码可读性**：将数据库结构定义与业务逻辑代码紧密结合，提高代码的可读性和可维护性。
- **减少错误**：自动化创建数据库结构减少了人为错误的可能性，提高了系统的稳定性。
- **易于维护**：注解的使用使得数据库结构的变更更加集中和明确，便于后期维护和升级。

###  @ExtraParam 注解

- **用途**：定义索引或其他自定义功能的额外参数。
- **属性**：
    - `key()`: 参数的键名。
    - `value()`: 参数的值。

### @MilvusCollection 注解

- **用途**：定义Milvus数据库中的集合。
- **属性**：
    - `name()`: 集合的名称。

### @MilvusField 注解

- **用途**：定义Milvus集合中的字段。
- **属性**：
    - `name()`: 字段名称，默认为Java字段名。
    - `dataType()`: 数据类型，默认为`FLOAT_VECTOR`。
    - `dimension()`: 向量维度，默认为-1。
    - `isPrimaryKey()`: 是否为主键，默认为false。
    - `autoID()`: 是否自动生成ID，默认为false。
    - `description()`: 字段描述，默认为空。
    - `elementType()`: 元素类型，默认为`None`。
    - `maxLength()`: 最大长度，默认为-1。
    - `maxCapacity()`: 最大容量，默认为-1。
    - `isPartitionKey()`: 是否为分区键，默认为false。

### @MilvusIndex 注解

- **用途**：定义Milvus集合中的索引。
- **属性**：
    - `indexType()`: 索引类型，默认为`FLAT`。
    - `metricType()`: 度量类型，默认为`L2`。
    - `indexName()`: 索引名称，默认为空。
    - `extraParams()`: 额外参数，使用`ExtraParam`注解定义。

### @MilvusPartition 注解

- **用途**：定义Milvus集合的分区。
- **属性**：
    - `name()`: 分区的名称数组。

通过这些注解，开发者可以轻松地定义和管理Milvus数据库的结构，实现项目启动时自动构建所需数据库结构的目标。

## CRUD模块介绍

CRUD模块是应用程序中用于处理数据的基本操作集合，即创建(Create)、读取(Read)、更新(Update)和删除(Delete)。在本模块中，我们通过抽象类`BaseMilvusMapper<T>`封装了与Milvus数据库交互的CRUD操作，提供了一种类型安全且易于使用的接口。

### 抽象类`BaseMilvusMapper<T>`

`BaseMilvusMapper<T>`是一个抽象类，它实现了`IAMService`接口，并定义了一系列用于操作Milvus数据库的方法。这个类是所有与Milvus数据库交互的Mapper类的基类。

### 抽象方法`getClient()`

- **功能**：返回一个`MilvusClientV2`实例，用于与Milvus数据库进行通信。

### 方法`queryWrapper()`

- **功能**：创建并返回一个`LambdaQueryWrapper<T>`实例，用于构建查询操作。

### 方法`deleteWrapper()`

- **功能**：创建并返回一个`LambdaDeleteWrapper<T>`实例，用于构建删除操作。

### 方法`updateWrapper()`

- **功能**：创建并返回一个`LambdaUpdateWrapper<T>`实例，用于构建更新操作。

### 方法`insertWrapper()`

- **功能**：创建并返回一个`LambdaInsertWrapper<T>`实例，用于构建插入操作。

### 方法`getById(Serializable ... ids)`

- **功能**：根据提供的ID列表查询数据。
- **参数**：`ids` - 一个可序列化的ID列表。
- **返回**：`MilvusResp<List<MilvusResult<T>>>` - 包含查询结果的响应。

### 方法`removeById(Serializable ... ids)`

- **功能**：根据提供的ID列表删除数据。
- **参数**：`ids` - 一个可序列化的ID列表。
- **返回**：`MilvusResp<DeleteResp>` - 删除操作的响应。

### 方法`updateById(T ... entity)`

- **功能**：根据提供的实体更新数据。
- **参数**：`entity` - 一个实体对象列表。
- **返回**：`MilvusResp<UpsertResp>` - 更新操作的响应。

### 方法`insert(T ... entity)`

- **功能**：插入提供的实体到数据库。
- **参数**：`entity` - 一个实体对象列表。
- **返回**：`MilvusResp<InsertResp>` - 插入操作的响应。

### 方法`lambda(Wrapper<W, T> wrapper)`

- **功能**：创建一个通用的构建器实例，用于不同类型的操作。
- **参数**：`wrapper` - 一个构建器实例。
- **返回**：`W` - 返回构建器实例。

### 流程说明

1. **获取客户端**：通过`getClient()`方法获取与Milvus数据库通信的客户端。
2. **创建构建器**：使用`queryWrapper()`、`deleteWrapper()`、`updateWrapper()`、`insertWrapper()`方法创建相应的操作构建器。
3. **执行操作**：使用构建器构建具体的CRUD操作，并执行。
4. **获取集合名称**：通过实体类上的`@MilvusCollection`注解获取集合名称。
5. **初始化构建器**：使用`lambda()`方法初始化构建器，设置集合名称、客户端、转换缓存和实体类型。

### 优势

- **类型安全**：通过泛型`T`确保操作的数据类型一致性。
- **简化操作**：封装CRUD操作，简化代码编写和数据库交互。
- **易于扩展**：抽象类设计使得扩展新的操作或自定义行为变得简单。
- **注解驱动**：利用`@MilvusCollection`注解自动获取集合名称，减少硬编码。

通过`BaseMilvusMapper<T>`类，开发者可以方便地实现与Milvus数据库的交互，提高开发效率并降低出错率。

## IAMService接口模块介绍

`IAMService`接口模块提供了一套完整的身份认证与访问管理（Identity and Access Management, IAM）功能，用于管理Milvus数据库中的角色、用户以及权限。该模块允许开发者通过简单的方法调用来执行用户和角色的创建、查询、删除、权限授予和撤销等操作。

### 方法`getClient()`

- **功能**：获取`MilvusClientV2`实例，用于与Milvus服务进行通信。

### 默认方法`createRole(String roleName)`

- **功能**：创建一个新的角色。
- **参数**：`roleName` - 要创建的角色名称。

### 默认方法`createUser(String userName, String password)`

- **功能**：创建一个新的用户。
- **参数**：
    - `userName` - 要创建的用户的名称。
    - `password` - 用户的密码。

### 默认方法`describeRole(String roleName)`

- **功能**：获取指定角色的权限信息。
- **参数**：`roleName` - 要查询的角色名称。
- **返回**：`DescribeRoleResp` - 包含角色权限信息的对象。

### 默认方法`describeUser(String userName)`

- **功能**：获取指定用户的详细信息。
- **参数**：`userName` - 要查询的用户名称。
- **返回**：`DescribeUserResp` - 包含用户详细信息的对象。

### 默认方法`dropRole(String roleName)`

- **功能**：删除指定的自定义角色。
- **参数**：`roleName` - 要删除的角色名称。

### 默认方法`dropUser(String userName)`

- **功能**：删除指定的用户。
- **参数**：`userName` - 要删除的用户名。

### 默认方法`grantPrivilege(String roleName, String objectType, String privilege, String objectName)`

- **功能**：授予角色特定的权限。
- **参数**：
    - `roleName` - 要授予权限的角色名称。
    - `objectType` - 权限对象的类型。
    - `privilege` - 要授予的权限名称。
    - `objectName` - 权限适用的对象名称。

### 默认方法`grantRole(String roleName, String userName)`

- **功能**：授予用户特定角色。
- **参数**：
    - `roleName` - 要授予给用户的角色名称。
    - `userName` - 已存在的用户名称。

### 默认方法`listRoles()`

- **功能**：列出所有自定义角色。
- **返回**：角色名称的列表。

### 默认方法`listUsers()`

- **功能**：列出所有现有用户的用户名。
- **返回**：用户名的列表。

### 默认方法`revokePrivilege(String roleName, String objectType, String privilege, String objectName, String databaseName)`

- **功能**：撤销已分配给角色的权限。
- **参数**：
    - `roleName` - 要从中撤销权限的角色名称。
    - `objectType` - 权限对象的类型。
    - `privilege` - 要撤销的权限名称。
    - `objectName` - 要撤销权限的API名称。
    - `databaseName` - 可选的数据库名称。

### 默认方法`revokeRole(String roleName, String userName)`

- **功能**：撤销用户的角色。
- **参数**：
    - `roleName` - 要撤销的角色名称。
    - `userName` - 现有用户的用户名。

### 默认方法`updatePassword(String userName, String password, String newPassword)`

- **功能**：更新指定用户的密码。
- **参数**：
    - `userName` - 现有用户的用户名。
    - `password` - 用户当前的密码。
    - `newPassword` - 用户的新密码。

### 优势

- **简化管理**：通过统一的接口简化了用户和角色的管理。
- **类型安全**：接口方法使用明确的参数类型，减少了错误。
- **易于集成**：默认方法使得实现细节可以轻松集成到现有系统中。
- **细粒度控制**：支持细粒度的权限控制，增强了安全性。
- **异常处理**：方法中包含异常处理机制，确保了操作的稳定性。

`IAMService`接口模块是构建安全、可靠且易于管理的Milvus数据库访问控制系统的基础。

## 条件构造器

条件构造器模块是一个用于动态构建查询条件的抽象基类`ConditionBuilder<T>`。它提供了一套丰富的方法来生成各种查询条件，从而简化数据库查询的构建过程。

### 功能概述

- **动态属性映射**：自动将实体对象的属性映射为查询条件。
- **支持多种条件**：包括等于、不等于、大于、小于、范围、空值检查、IN条件、LIKE条件等。
- **逻辑操作**：支持AND、OR和NOT逻辑操作，以构建复杂的查询条件。
- **类型安全**：泛型`T`确保了条件构造过程中的类型安全。
- **易于扩展**：抽象类设计允许开发者根据需要扩展更多条件构造功能。

### 主要方法

### `getPropertiesMap(T t)`

- **功能**：将实体对象的属性及其值映射为Map。

### 基本条件方法

- `eq`、`ne`、`gt`、`ge`、`lt`、`le`：分别用于添加等于、不等于、大于、大于等于、小于、小于等于条件。

### 范围和空值检查

- `between`：添加范围条件。
- `isNull`、`isNotNull`：添加空值检查条件。

### IN条件和LIKE条件

- `in`：添加IN条件，用于匹配字段值是否在给定的列表中。
- `like`：添加LIKE条件，用于执行模式匹配查询。

### JSON和数组操作

- `jsonContains`、`jsonContainsAll`、`jsonContainsAny`：用于处理JSON字段的包含检查。
- `arrayContains`、`arrayContainsAll`、`arrayContainsAny`：用于处理数组字段的包含检查。
- `arrayLength`：检查数组字段的长度。

### 逻辑操作

- `and`、`or`、`not`：用于组合多个条件构造器对象，执行逻辑AND、OR和NOT操作。

### 辅助方法

- `convertValue`、`convertValues`：用于将值转换为适合查询的格式。
- `wrapFieldName`：用于包装字段名，可能需要根据具体的查询语言调整。

### 抽象方法

- `buildFilters`：需要子类实现的具体过滤条件字符串构建逻辑。

### 使用示例

```java
ConditionBuilder<MyEntity> builder = new ConditionBuilder<>();
builder.eq("name", "John").gt("age", 18).and(new ConditionBuilder<MyEntity>().like("email", "example.com"));
String filters = builder.buildFilters();
```



## 索引与度量类型详解

### 索引类型（IndexType）

- **INVALID**：无效索引类型，仅用于内部标记。
- **FLAT**：暴力搜索，适用于小规模数据集。
- **IVF_FLAT**：倒排索引平面模式，适用于中等规模数据集。
- **IVF_SQ8**：倒排索引量化模式，适用于大规模数据集，牺牲精度提升速度。
- **IVF_PQ**：倒排索引产品量化模式，适用于大规模高维数据集，平衡速度和精度。
- **HNSW**：分层导航小世界图，提供快速搜索，适用于大规模数据集。
- **DISKANN**：基于磁盘的近似最近邻搜索，适用于存储在磁盘上的大规模数据集。
- **AUTOINDEX**：自动选择最优索引类型。
- **SCANN**：使用扫描和树结构加速搜索。
- **GPU_IVF_FLAT、GPU_IVF_PQ**：GPU 加速索引，适用于 GPU 环境。
- **BIN_FLAT、BIN_IVF_FLAT**：二进制向量专用索引。
- **TRIE**：适用于字符串类型的字典树索引。
- **STL_SORT**：适用于标量字段的排序索引。

### 度量类型（MetricType）

- **INVALID**：无效度量类型，仅用于内部标记。
- **L2**：欧几里得距离，适用于浮点向量。
- **IP**：内积，用于计算余弦相似度。
- **COSINE**：余弦相似度，适用于文本和图像搜索。
- **HAMMING**：汉明距离，适用于二进制向量。
- **JACCARD**：杰卡德相似系数，适用于集合相似度计算。



## 使用案例

以下是使用 MilvusPlus 进行向量搜索的示例：

```
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

## 欢迎给项目点赞！！

https://gitee.com/dromara/MilvusPlus

## 贡献

欢迎贡献！

- 报告问题或建议功能，[创建一个 issue](https://gitee.com/giteeClass/MilvusPlus/issues/new)。
- 提交更改，[创建一个 pull request](https://gitee.com/giteeClass/MilvusPlus/compare)。

## 许可证

MilvusPlus 是开源的，遵循 [许可证](https://gitee.com/giteeClass/MilvusPlus/blob/master/LICENSE)。

## 联系

如有问题或需要支持，请联系 [javpower@163.com](mailto:javpower@163.com) 。
