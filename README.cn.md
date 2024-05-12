# MilvusPlus：向量数据库增强操作库
MilvusPlus 是一个功能强大的 Java 库，旨在简化与 Milvus 向量数据库的交互，为开发者提供类似 MyBatis-Plus 注解和方法调用风格的直观 API。

## 目录

1. [特性](#特性)
2. [快速开始](#快速开始)
3. [应用场景](#应用场景)
4. [自定义注解](#自定义注解)
5. [索引与度量类型详解](#索引与度量类型详解)
6. [使用案例](#使用案例)
7. [贡献](#贡献)
8. [许可证](#许可证)
9. [联系](#联系)

## 特性

- **注解式配置**：采用与 MyBatis-Plus 类似的注解方式配置实体模型。
- **直观的 API**：直接的 API 设计简化数据库操作。
- **易于扩展**：核心设计注重可扩展性。
- **类型安全**：利用 Java 类型安全减少错误。

## 快速开始

通过 Maven 将 MilvusPlus 添加到项目中：

```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.4.0-SNAPSHOT</version>
</dependency>
```

## 应用场景

- **相似性搜索**：快速检索与给定向量最相似的项。
- **推荐系统**：根据用户行为和偏好推荐相关内容。
- **图像检索**：在大规模图像库中找到与查询图像最相似的图像。
- **自然语言处理**：将文本转换为向量并执行语义搜索。
- **生物信息学**：分析和比较生物序列，如蛋白质和基因组数据。

## 自定义注解

- `@MilvusCollection`：标识 Java 类为 Milvus 集合。
- `@MilvusField`：映射 Java 字段到 Milvus 字段。
- `@MilvusIndex`：在 Milvus 字段上定义索引。

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

## 贡献

欢迎贡献！

- 报告问题或建议功能，[创建一个 issue](https://github.com/yourusername/MilvusPlus/issues/new)。
- 提交更改，[创建一个 pull request](https://github.com/yourusername/MilvusPlus/compare)。

## 许可证

MilvusPlus 是开源的，遵循 [许可证](https://github.com/yourusername/MilvusPlus/blob/master/LICENSE)。

## 联系

如有问题或需要支持，请联系 [javpower@163.com](mailto:javpower@163.com) 。
