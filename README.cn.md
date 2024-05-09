# MilvusPlus：向量数据库增强操作库

MilvusPlus 是一个功能强大的 Java 库，旨在简化与 Milvus 向量数据库的交互，为开发者提供类似 MyBatis-Plus 注解和方法调用风格的直观 API。

## 目录

1. [特性](#特性)
2. [快速开始](#快速开始)
3. [应用场景](#应用场景)
4. [注解说明](#注解说明)
5. [贡献](#贡献)
6. [许可证](#许可证)

## 特性

- **注解式配置**：采用与 MyBatis-Plus 类似的注解方式来配置你的实体模型。
- **直观的 API**：直接的 API 设计，让向量数据库操作变得自然而然。
- **易于扩展**：以可扩展性为核心设计，方便新增功能。
- **类型安全**：利用 Java 的类型安全特性，最大程度减少错误。

## 快速开始

将 MilvusPlus 添加到你的项目中：

**Maven:**
```xml
<dependency>
    <groupId>io.github.javpower</groupId>
    <artifactId>milvus-plus-boot-starter</artifactId>
    <version>2.4.0</version>
</dependency>
```

## 应用场景

向量数据库在以下场景中特别有用：

- **相似性搜索**：快速检索与给定向量最相似的项。
- **推荐系统**：根据用户行为和偏好推荐相关内容。
- **图像检索**：在大规模图像库中找到与查询图像最相似的图像。
- **自然语言处理**：将文本转换为向量并执行语义搜索。
- **生物信息学**：分析和比较生物序列，如蛋白质和基因组数据。

## 注解说明

MilvusPlus 引入了几个注解，用于将你的 Java 实体映射到 Milvus 集合：

- `@MilvusCollection`：表示一个 Java 类是一个 Milvus 集合。
- `@MilvusField`：将 Java 字段映射到 Milvus 字段，并提供数据类型、维度等选项。
- `@MilvusIndex`：在 Milvus 字段上定义索引。

示例用法：

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
public static void main(String[] args) {
        MilvusWrapper<Face> wrapper=new MilvusWrapper();
        List<Float> vector = Lists.newArrayList(0.1f,0.2f,0.3f);
        MilvusResp<Face> resp = wrapper.lambda()
                .eq(Face::getPersonId,1l)
                .addVector(vector)
                .query();
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
