# 公地引擎 · 分库分表策略设计 v1.0

> 目标：支撑数亿成员、百亿级事件流水，从单体 PostgreSQL 平滑演进到分布式数据架构。

## 一、现状分析

### 1.1 数据量预估

| 表 | 主键 | 逻辑外键 | 预估量级（亿用户） | 增长特征 |
|---|---|---|---|---|
| `member` | BIGINT | — | **亿级** | 用户增长驱动，低增速 |
| `proposal` | BIGINT | → member | 百万级 | 低频 |
| `vote` | BIGINT | → proposal, → member | **百亿级** | 高峰期爆发写入 |
| `payment_ledger_event` | UUID | → transaction | **千亿级** | 追加写入，永不修改 |
| `mutual_claim` | UUID | → member | 千万级 | 中频 |
| `worker_location` | VARCHAR(64) | → member | 百万级（热数据） | 超高频更新 |
| `ai_review_records` | BIGINT | → proposal | 百万级 | 低频 |
| `dispute_records` | BIGINT | → member | 千万级 | 中频 |
| `finance_financing_records` | BIGINT | — | 百万级 | 低频 |
| `early_warning_alert` | UUID | — | 百万级 | 事件驱动 |
| `tech_deployment_records` | BIGINT | — | 万级 | 极低频 |

### 1.2 架构优势（已具备）

- ✅ **无物理外键**：全部逻辑外键（ID 引用），分库零障碍
- ✅ **无 JPA 关联映射**：实体完全解耦，无跨表 JOIN
- ✅ **模块边界清晰**：10 个业务模块 + 1 个公共模块，天然分库边界
- ✅ **SnowflakeId 已就绪**：32 位全局唯一 ID，适合分片路由

---

## 二、演进路线（四阶段）

```
Phase 1（现在 → 百万用户）
  单库 PostgreSQL + Redis 缓存
  ↓ 瓶颈：读写比失衡、worker_location 压力

Phase 2（百万 → 千万用户）
  读写分离（1主 + 3从）+ Redis 全面覆盖
  worker_location 迁移到 Redis Geo
  ↓ 瓶颈：单库写入瓶颈（vote / payment_ledger_event）

Phase 3（千万 → 亿级用户）  ← 本文档核心
  垂直分库（按领域）+ 水平分表（高频表）
  事件溯源表冷热分离
  ↓ 瓶颈：跨域聚合查询

Phase 4（亿级 → 数亿用户）
  CQRS 读模型（PostgreSQL 写 → Elasticsearch 读）
  分布式事务（Saga / Outbox 模式）
  全局 ID 发号器独立部署
```

---

## 三、Phase 3 详细设计：垂直分库 + 水平分表

### 3.1 垂直分库（按领域边界）

```
┌─────────────────────────────────────────────────────────┐
│                    业务应用层（单体）                      │
│              Spring Boot × N 实例（虚拟线程）              │
└──────┬──────────┬──────────┬──────────┬────────────────┘
       │          │          │          │
       ▼          ▼          ▼          ▼
  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌──────────────┐
  │ DB:     │ │ DB:     │ │ DB:     │ │ DB:          │
  │identity │ │governance│ │payment  │ │operations    │
  │         │ │         │ │         │ │              │
  │ member  │ │ proposal│ │ ledger  │ │ dispute      │
  │ (分表)  │ │ vote    │ │ _event  │ │ early_warning│
  │         │ │ (分表)  │ │ (分表)  │ │ ai_review    │
  │         │ │         │ │         │ │ tech_deploy  │
  │         │ │         │ │         │ │ worker_loc⚠️ │
  │ 1主3从  │ │ 1主5从  │ │ 1主5从  │ │ 1主3从       │
  └─────────┘ └─────────┘ └─────────┘ └──────────────┘
       │          │          │
  ┌────┴──────────┴──────────┴────┐
  │ DB: mutual & finance          │
  │ mutual_claim / financing_records│
  │ 1主2从                        │
  └───────────────────────────────┘
```

| 数据库 | 包含表 | 主从比 | 分库依据 |
|---|---|---|---|
| `db_identity` | `member` | 1主:3从 | 核心身份域，独立资源池 |
| `db_governance` | `proposal`, `vote` | 1主:5从 | 投票高峰写入隔离 |
| `db_payment` | `payment_ledger_event` | 1主:5从 | 事件溯源，追加写入，独立 IO |
| `db_mutual` | `mutual_claim` | 1主:2从 | 独立审核流程 |
| `db_finance` | `finance_financing_records` | 1主:2从 | 合规审计隔离 |
| `db_operations` | `dispute_records`, `early_warning_alert`, `ai_review_records`, `tech_deployment_records` | 1主:3从 | 运营域聚合，低中频 |
| ~~`worker_location`~~ | → **迁移到 Redis Geo** | — | 超高频更新不适合 RDBMS |

### 3.2 水平分表（高频大表）

| 表 | 分片键 | 分片算法 | 初始分片数 | 路由方式 |
|---|---|---|---|---|
| `member` | `id` | `id % 64` | 64 库 × 1 表 | SnowflakeId 内含分片信息 |
| `vote` | `proposal_id` | `proposal_id % 128` | 16 库 × 8 表 | 同一提案的投票落在同一分片 |
| `payment_ledger_event` | `transaction_id` | `hash(transaction_id) % 256` | 32 库 × 8 表 | 同一交易的事件链可查 |

#### 分片键选择原则

| 原则 | member | vote | payment_ledger_event |
|---|---|---|---|
| **高频查询携带分片键** | ✅ memberId 查 member | ✅ proposalId 查选票 | ✅ transactionId 查流水 |
| **避免跨片查询** | ✅ 按 ID 直达 | ✅ 统计聚合在同一片 | ✅ 交易事件链完整 |
| **写入均匀分布** | ✅ SnowflakeId 均匀 | ✅ proposalId 均匀 | ✅ UUID hash 均匀 |
| **未来扩展不停服** | ✅ 预留 64 分片 | ✅ 预留 128 分片 | ✅ 预留 256 分片 |

#### 分片扩容策略

```
初始：4 库 × 16 表 = 64 分片（member）
扩容：8 库 × 16 表 = 128 分片
  → 只需迁移 4 个库的数据到新库，无需全量重hash
  → 基于 2 的幂次扩容，每轮只搬一半数据
```

### 3.3 worker_location 特殊处理

```
当前：PostgreSQL 表（超高频更新，行锁竞争）
目标：
  ├── Redis Geo（主）：实时位置匹配、附近劳动者搜索
  │     GEOADD worker:geo lng lat workerId
  │     GEORADIUS worker:geo lng lat 5 km ASC COUNT 20
  │
  └── PostgreSQL（辅）：位置历史轨迹（批量写入，按天分区）
        INSERT INTO worker_location_log ... → 按 created_at 分区
```

### 3.4 payment_ledger_event 冷热分离

```
热数据（近 90 天）→ db_payment 分片（SSD）
    ↑ 定时迁移
冷数据（90 天前）→ 冷存储（对象存储 / 归档 PG 表空间）
    ↓ 按月分区
```

---

## 四、技术选型

| 层 | 方案 | 理由 |
|---|---|---|
| 分片中间件 | **Apache ShardingSphere-JDBC** | Java 原生，无 Proxy 层，Spring Boot 无缝集成 |
| 分布式 ID | **SnowflakeId**（已有） | 32 位全局唯一，内置时间戳，趋势递增 |
| 缓存 | **Redis 7 Cluster** | Geo 计算 + 热数据缓存 + 分布式锁 |
| 读模型 | **Elasticsearch 8** | 聚合查询、全文搜索、跨域读模型 |
| 消息队列 | **Apache RocketMQ** | Outbox 事务消息、跨域事件通知 |
| 数据迁移 | **ShardingSphere-Scaling** | 在线扩容、数据全量+增量同步 |

### ShardingSphere 集成示意

```yaml
# application-sharding.yml
spring:
  shardingsphere:
    datasource:
      names: ds_identity_0,ds_identity_1,...,ds_governance_0,...
    rules:
      sharding:
        tables:
          member:
            actual-data-nodes: ds_identity_${0..3}.member_${0..15}
            database-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: member-db-algorithm
            table-strategy:
              standard:
                sharding-column: id
                sharding-algorithm-name: member-table-algorithm
          vote:
            actual-data-nodes: ds_governance_${0..7}.vote_${0..15}
            database-strategy:
              standard:
                sharding-column: proposal_id
                sharding-algorithm-name: vote-db-algorithm
        binding-tables:
          - proposal,vote  # 绑定表，同库避免跨库 JOIN
```

---

## 五、分布式事务策略

| 场景 | 方案 | 原因 |
|---|---|---|
| 单域内事务 | **本地事务**（不变） | 单库内 ACID |
| 跨域最终一致 | **Outbox 模式** | Service 写业务表 + 事件表（同一事务），MQ 消费者异步通知 |
| 支付流水 | **Event Sourcing + Saga** | 事件不可变，补偿事务保证一致性 |
| 投票计票 | **最终一致** | 投票写入分片，计票异步聚合到 ES |

### Outbox 模式示例

```
PaymentService.settle()
  ├── BEGIN TRANSACTION
  │   ├── INSERT INTO payment_ledger_event ...  -- 业务事件
  │   └── INSERT INTO outbox_event ...           -- 发件箱（同一事务）
  └── COMMIT
  
RocketMQ Consumer (async)
  ├── READ outbox_event
  ├── NOTIFY mutual_domain / finance_domain
  └── MARK outbox_event AS sent
```

---

## 六、读模型（CQRS）

```
                     ┌── 写入 ──→ PostgreSQL 分片（强一致）
                     │
业务应用 ────查询───┤
                     │
                     └── 读取 ──→ Elasticsearch（最终一致）
                           ├── member_search_idx（成员搜索）
                           ├── vote_aggregation_idx（投票实时统计）
                           ├── payment_summary_idx（账务汇总）
                           └── dispute_search_idx（争议全文搜索）
```

同步方式：**Change Data Capture**（Debezium → Kafka → ES）

---

## 七、落地优先级

| 优先级 | 任务 | 预计工期 | 前置条件 |
|---|---|---|---|
| P0 | worker_location 迁移到 Redis Geo | 3 天 | Redis Cluster |
| P0 | 读写分离（所有库 1主+N从） | 1 周 | PG 主从配置 |
| P1 | 垂直分库（6 个独立 DB） | 2 周 | ShardingSphere 接入 |
| P1 | payment_ledger_event 冷热分离 | 1 周 | PG 分区表 |
| P2 | member / vote 水平分表 | 3 周 | 垂直分库完成 |
| P2 | Outbox 模式 + RocketMQ | 2 周 | MQ 集群 |
| P3 | CQRS 读模型 + ES | 4 周 | CDC 基础设施 |
| P3 | 在线扩容工具链 | 2 周 | ShardingSphere-Scaling |
