# 平台共同体 · 技术架构设计 v1.0

> 基于宪章 v0.2（21 章 124 条）设计的技术实现架构
> 技术栈：Java 27 + Spring Boot 4.1.0 + PostgreSQL + Redis
> 开发规范：[CONVENTIONS.md](CONVENTIONS.md)（基于阿里巴巴黄山版）

## 一、模块设计（映射宪章条款）

### 核心业务模块（backend/）

| 模块 | 宪章条款 | 职责 |
|------|---------|------|
| `platform-common` | 第1-5条 | 共享基础：六项永久锁、枚举、工具类、全局异常 |
| `platform-governance` | 第4章(16-21), 第5章(22-26), 第19章(110-114) | 四院治理、理事会、提案表决、修宪、抽签审议 |
| `platform-identity` | 第3章(11-15) | 成员资格、四类成员、权利义务、退出 |
| `platform-asset` | 第2章(6-10), 第20章(115-119) | 共同资产管理、资产锁、三层组织、解散转移 |
| `platform-labor` | 第8章(37-42) | 劳动保障、净收入、算法劳动权、公共任务池 |
| `platform-payment` | 第9章(43-49) | 订单定价、平台服务费、结余分配、劳动者返还 |
| `platform-mutual` | 第14章(81-92) | 劳动意外互助保障基金、资格认定、反欺诈 |
| `platform-finance` | 第10章(50-54), 第11章(55-59) | 融资约束、采购公开、关联交易、财务透明 |
| `platform-ai-supervision` | 第12章(60-69) | AI交叉审议、多角色分析、反俘获测试 |
| `platform-tech-governance` | 第13章(70-80) | 开源核验、可复现构建、算法说明、关键权限 |
| `platform-dispute` | 第15章(93-96) | 三级救济、申诉委员会、集体性问题 |
| `platform-early-warning` | 第16章(97-101) | 防异化预警、五类红线、自动措施 |
| `platform-federation` | 第17章(102-106) | 地方合作社、平台联盟、互操作 |
| `platform-emergency` | 第18章(107-109) | 紧急状态、14天限制、监督复盘 |
| `platform-matching` | 第8章(42) | 匹配引擎、反榨取约束、可解释算法 |
| `platform-dispatch` | 第8章(42) | 调度引擎、路径优化、尊重劳动者偏好 |
| `platform-rating` | 第3章(13) | 双向评价、信用画像、数据可携带 |
| `platform-booth` | 第21章(120-124) | 创立期管理、影子治理、移交 |

### 基础设施模块

| 模块 | 职责 |
|------|------|
| `platform-bootstrap` | 启动入口、全局配置、健康检查 |

## 二、技术选型

| 层面 | 选择 | 理由 |
|------|------|------|
| 语言 | Java 27 (EA) | 宪章要求最新版本；record/sealed/pattern matching |
| 框架 | Spring Boot 4.1.0 | 最新稳定版 |
| 构建 | Gradle 8.x (Groovy DSL) | 多模块管理 |
| 数据库 | PostgreSQL 16 + PostGIS | 空间索引、事务完整性 |
| 缓存 | Redis 7 | 劳动者位置缓存、限流 |
| API 文档 | springdoc-openapi 3.0.3 | OpenAPI 3.1 |
| 持久化 | Spring Data JPA + Flyway | 宪章要求可审计迁移 |
| 测试 | JUnit 5 + H2 + EmbeddedPostgres | |
| 代码规范 | Alibaba P3C PMD 规则 | 黄山版开发手册 |

## 三、阿里 Java 开发手册核心规范落地

| 规范领域 | 具体措施 |
|---------|---------|
| 命名 | 类名 UpperCamelCase；方法/变量 lowerCamelCase；常量全大写下划线；包名全小写 |
| 常量 | 禁止魔法值，全部提取为常量类 |
| OOP | equals 方法用常量在左；包装类比较用 equals；POJO 必须有 toString |
| 集合 | 判空用 isEmpty()；Map 遍历用 entrySet；线程安全用 ConcurrentHashMap |
| 并发 | SimpleDateFormat 线程不安全，用 DateTimeFormatter；线程池用 ThreadPoolExecutor |
| 异常 | 不要捕获大的异常范围；异常不能忽略；方法返回值可以用 Optional |
| 日志 | 使用 SLF4J；日志占位符用 {} 而非字符串拼接；异常日志要打印全部堆栈 |
| MySQL | 禁止 select *；表必须有主键；varchar 取代 char；索引命名 idx_ 前缀 |
| 工程 | 应用分层：Controller → Service → Manager → DAO |

## 四、分层架构（阿里规范应用分层）

```
Controller 层（开放接口、视图适配）
    ↓
Service 层（业务逻辑、组合调用）
    ↓
Manager 层（通用封装、缓存、降级）
    ↓
DAO 层（数据访问、Entity 映射）
```

每个模块内部遵循此分层：
- `api/` → Controller + DTO
- `service/` → 业务逻辑
- `manager/` → 缓存/通用封装
- `repository/` → JPA Entity + Repository
- `domain/` → 领域模型（record/sealed）
