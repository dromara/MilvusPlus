# Local Milvus for MilvusPlus

用 Docker 拉起单机 Milvus，方便本地调试 Plus 与跑 demo。

> 镜像默认走 **DaoCloud 镜像前缀**（`docker.m.daocloud.io/...`），避免直连 Docker Hub 过慢/失败。  
> `milvus` 镜像约 **1.8GB**，首次拉取可能需要几分钟，属正常现象。

## 启动

```bash
cd docker
docker compose up -d
```

等待 `milvus-plus-standalone` 健康（首次约 1～2 分钟）：

```bash
docker compose ps
curl -f http://localhost:9091/healthz
```

## 连接配置

Spring / Solon：

```yaml
milvus:
  enable: true
  uri: http://localhost:19530
  # token / username / password 本地 standalone 一般不需要
  packages:
    - com.example.entity
  schema-mode: AUTO_ADD
  open-log: true
```

- gRPC / SDK 端口：`19530`
- 健康检查：`9091`
- MinIO 控制台：`http://localhost:9001`（minioadmin / minioadmin）

## 停止与清理

```bash
# 停容器，保留数据
docker compose down

# 停容器并删除数据卷目录（慎用）
docker compose down
rm -rf volumes
```

## 版本说明

| 组件 | 版本/说明 |
|------|-----------|
| 本 compose 中的 Milvus | `milvusdb/milvus:v2.5.14` standalone（易本地跑） |
| 当前 Plus 依赖 SDK | `milvus-sdk-java:3.0.4`（官方对齐 Milvus 3.0.x） |

多数 CRUD / 向量检索 / 过滤表达式可在本环境验证。若要测 **3.0 专属能力**，请换成官方 3.0 部署方式后再连。

## 冒烟检查

容器健康后，可用 demo 模块或最小 Java 代码连 `http://localhost:19530` 建集合、插入、查询。
