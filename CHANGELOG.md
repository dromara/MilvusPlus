# Changelog

> **2.3.0 详细使用文档**：[docs/2.3.0-使用指南.md](./docs/2.3.0-使用指南.md)

## 2.3.0

### Dependency

- Upgrade `milvus-sdk-java` from `2.5.5` to **`3.0.4`**（对齐 Milvus server **3.0.x**）。
- 移除写死的 `protobuf-java 3.24.0`，改用 SDK 自带 `3.25.5`，避免 `IllegalAccessError`。
- 继续保持 **Java 8** + **Spring Boot 2.7.x** 基线。

### Bug fixes

- **Lambda and/or/not 条件拼接**：重写逻辑语义与括号优先级（见下文「条件表达式」）；修复 `likeRight`、`buildFilters` 重复调用污染、null 值 NPE。
- **addField**：真正调用 `MilvusClientV2.addCollectionField`（Gitee `IJLLKL`）。
- **getField**：从 `describeCollection` 读取已有集合字段。
- **GsonUtil**：修复 Date 序列化器被反序列化器覆盖（GitCode #1）；`put` 跳过 null key。
- **Insert/Update**：跳过 null key，避免动态字段 NPE（Gitee `IC3W8Q`）。
- **Search**：优先 `limit`；`metricType` 走 `SearchReq`；`radius`/`range_filter` 保留在 searchParams（Gitee `IBUDP0`）。
- **Hybrid**：`filter` 替代废弃 `expr`；ranker 对齐 `CreateCollectionReq.Function`。
- **GetResp**：改用 `getGetResults()`。
- **缓存空 NPE**：实体未扫描时懒加载 `MilvusConverter.convert`（Gitee `I9RDOD` 同类问题）。
- SDK 3.x Builder 去 Lombok 泛型适配。

### Features（Plus 体验向）

#### Schema 演进
- 配置项 `milvus.schema-mode`：`IGNORE` | `VALIDATE` | `AUTO_ADD` | `RECREATE`
- 配置项 `milvus.enable-recreate`：仅开发环境允许删表重建
- API：`ensureSchema(Class)` / `ensureSchema(Class, SchemaMode)` / `ensureSchema(Class, collectionName, mode)`
- 启动扫描实体时按 schema-mode 同步集合结构

#### 动态集合
- `mapper.forCollection("face_t1")` / `mapper.queryWrapper("face_t1")`
- `milvusService.forCollection(Face.class, "face_t1")`
- 动态名自动复用逻辑实体主键映射

#### 检索语义化
- `vectorSearch` / `textSearch` / `range` / `top`
- `options(SearchOptions)` / `options(opt -> ...)` 专家参数收口
- `page(pageNum, pageSize)` 标量分页（向量检索禁用，避免语义混淆）
- `byIds(...)` 主键批量获取别名
- `MilvusResult.getScore()` 作为 distance 语义别名
- **原生 filter**：`.filter("status == 1")` / `.where(...)`
- **类 SQL WHERE 子集（实验）**：`.sqlWhere("status = 1 AND name LIKE '%x%'")` → Milvus boolean expr（非完整 MySQL）

#### 本地测试
- `docker/docker-compose.yml`：一键启动 standalone Milvus（19530，DaoCloud 镜像）
- 集成测试 `MilvusPlusIntegrationIT`：insert/query/or/sqlWhere/vector/page/partial/dynamicCollection/truncate

#### 联调修复
- `numPartitions` 空指针（未配置分区数时）
- 无 filter 的 query 自动补 limit
- `count(*)` 禁止带 pagination
- `partial()` 改为读改写，兼容 2.5 不支持服务端 partial upsert
- `truncateCollection` 在 2.5 上回退为分批 delete

#### 写入/运维
- `updateWrapper().partial(true)` / `partial()` → SDK partial upsert
- `truncateCollection(name|Class)`
- 注解增强：`@MilvusField(isClusteringKey, defaultValue)`

#### 工程体验
- 统一异常 `MilvusPlusException`（含 error code）
- 日志截断工具 `LogSanitizeUtil`（避免长向量刷屏）
- packages 扫描失败给出明确错误码

### 配置示例

```yaml
milvus:
  enable: true
  uri: http://localhost:19530
  packages:
    - com.example.entity
  # 推荐新项目：
  schema-mode: AUTO_ADD
  enable-recreate: false   # 生产必须 false
  open-log: true
  log-level: INFO
```

### 使用示例

```java
// Schema 自动补字段
milvusService.ensureSchema(Face.class);

// 动态集合
faceMapper.forCollection("face_" + tenantId)
    .insert(face);

// 语义化检索
faceMapper.queryWrapper()
    .vectorSearch(Face::getFaceVector, emb)
    .eq(Face::getStatus, 1)
    .range(0.1, 0.8)
    .top(10)
    .options(opt -> opt.metricType(IndexParam.MetricType.IP).ignoreGrowing(true))
    .query();

// 文本检索（隐藏 _sparse）
faceMapper.queryWrapper()
    .textSearch(Face::getContent, "关键词")
    .top(10)
    .query();

// 标量分页
MilvusResp<PageResult<Face>> page = faceMapper.queryWrapper()
    .eq(Face::getStatus, 1)
    .page(1, 20);

// 部分更新
faceMapper.updateWrapper()
    .partial()
    .updateById(partialEntity);
```

### 条件表达式（and / or / not）

旧实现问题：
- `or(nested)` 只把 nested **内部**用 `||` 拼上，再与外层 **AND**，语义错误
- 嵌套 `and(w.eq().or(...))` 会把内部 OR 弄丢
- `buildFilters()` 会修改内部列表，重复调用条件重复
- `likeRight` 引号拼错

新语义（对齐 MyBatis-Plus 心智）：
```java
// (status == 1) || (type == 'A' && level == 2)
.eq(Face::getStatus, 1)
.or(w -> w.eq(Face::getType, "A").eq(Face::getLevel, 2))

// status == 1 && ((type == 'A') || (type == 'B'))
.eq(Face::getStatus, 1)
.and(w -> w.eq(Face::getType, "A").or(w2 -> w2.eq(Face::getType, "B")))

// not (status == 1 && type == 'A')
.eq(...).eq(...).not()
```

已加单测：`ConditionBuilderLogicTest`（15 cases）。

### Compatibility notes

- 目标服务端：**Milvus 3.0.x**。若生产仍是 2.6.x，请评估兼容性后再升级。
- Hybrid `ranker(...)` 接受 `CreateCollectionReq.Function` 子类（`RRFRanker`/`WeightedRanker` 仍可用）。
- 已有集合新增字段通常需 **nullable** 或 **defaultValue**；`AUTO_ADD` 会尽量强制 nullable。
- `page()` 仅用于标量 query；向量 ANN 请用 `top/limit`。
