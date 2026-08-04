# 平台共同体 · 技术架构设计 v2.0

> 基于宪章 v0.2（21 章 124 条）设计的技术实现架构
> 技术栈：Java 25 + Spring Boot 4.1.0 + PostgreSQL + Redis
> 开发规范：[CONVENTIONS.md](CONVENTIONS.md)（基于阿里巴巴黄山版）
>
> **v2.0 变更**：整合多身份体系、钱包与支付网关、统一支付渠道（微信/支付宝/银行）、提现、业务订单流转、消息通知、抢单广播，引入 DDD 领域模型设计。

## 一、模块设计（映射宪章条款）

### 核心业务模块（backend/）

| 模块 | 宪章条款 | 职责 |
|------|---------|------|
| `platform-common` | 第1-5条 | 共享基础：六项永久锁、枚举、工具类、全局异常 |
| `platform-identity` | 第3章(11-15) | 成员资格、多身份角色（Member/Worker/Merchant）、实名认证、地址簿 |
| `platform-governance` | 第4章(16-21), 第5章(22-26), 第19章(110-114) | 四院治理、理事会、提案表决、修宪、抽签审议 |
| `platform-asset` | 第2章(6-10), 第20章(115-119) | 共同资产管理、资产锁、三层组织、解散转移 |
| `platform-labor` | 第8章(37-42) | 劳动保障、净收入、算法劳动权、公共任务池 |
| `platform-payment` | 第9章(43-49) | 钱包、流水、统一支付网关、渠道路由、提现、银行卡 |
| `platform-mutual` | 第14章(81-92) | 劳动意外互助保障基金、资格认定、反欺诈 |
| `platform-finance` | 第10章(50-54), 第11章(55-59) | 融资约束、采购公开、关联交易、财务透明 |
| `platform-ai-supervision` | 第12章(60-69) | AI交叉审议、多角色分析、反俘获测试 |
| `platform-tech-governance` | 第13章(70-80) | 开源核验、可复现构建、算法说明、关键权限 |
| `platform-dispute` | 第15章(93-96) | 三级救济、申诉委员会、集体性问题 |
| `platform-early-warning` | 第16章(97-101) | 防异化预警、五类红线、自动措施 |
| `platform-federation` | 第17章(102-106) | 地方合作社、平台联盟、互操作 |
| `platform-emergency` | 第18章(107-109) | 紧急状态、14天限制、监督复盘 |
| `platform-matching` | 第8章(42) | 业务订单、订单流转、抢单广播、匹配引擎 |
| `platform-dispatch` | 第8章(42) | 调度引擎、路径优化、尊重劳动者偏好 |
| `platform-notification` | — | 消息通知：1:1 定向通知、通知模板、多渠道分发 |
| `platform-rating` | 第3章(13) | 双向评价、信用画像、数据可携带 |
| `platform-booth` | 第21章(120-124) | 创立期管理、影子治理、移交 |

### 基础设施模块

| 模块 | 职责 |
|------|------|
| `platform-bootstrap` | 启动入口、全局配置、健康检查、Flyway 迁移 |

## 二、技术选型

| 层面 | 选择 | 理由 |
|------|------|------|
| 语言 | Java 25 (Zulu) | record/sealed/pattern matching；最新 LTS |
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
Controller 层（开放接口、视图适配）       api/
    ↓
Application 层（用例编排、事务边界）       service/
    ↓
Domain 层（聚合根、实体、值对象、领域服务） domain/
    ↓
Infrastructure 层（持久化、缓存、消息）    repository/ manager/
```

每个模块内部包结构：
- `api/` → Controller + DTO（Request/Response record）
- `service/` → 应用服务（用例编排、事务边界，不含业务规则）
- `domain/` → 领域模型（聚合根、实体、值对象、领域事件、领域服务）
- `repository/` → JPA Entity + Repository（持久化映射）
- `manager/` → 缓存 / 第三方适配 / 通用封装（按需）

### 4.1 DDD 分层职责边界

| 层 | 允许 | 禁止 |
|----|------|------|
| **api** | 参数校验、DTO 转换、调用 ApplicationService | ❌ 业务逻辑、直接调 Repository |
| **service (Application)** | 用例编排、事务管理、调用 Domain Service | ❌ 业务规则判断、跨聚合直接操作 |
| **domain** | 业务规则、状态机、不变量守护 | ❌ Spring 依赖、JPA 注解、IO 操作 |
| **repository** | 数据访问、Entity↔DO 转换 | ❌ 业务逻辑 |
| **manager** | 缓存策略、第三方 API 适配 | ❌ 业务规则 |

> **原则**：Domain 层零框架依赖（纯 Java record/sealed），可脱离 Spring 独立测试。

---

## 五、核心业务流程图

### 5.1 多身份与角色生命周期

```
                    ┌─────────────────────────────────────────────────┐
                    │                   Member (核心身份)               │
                    │         id / name / phone / status               │
                    └───────────┬──────────────┬──────────────┬───────┘
                                │              │              │
                    ┌───────────▼──┐  ┌────────▼─────┐  ┌────▼──────────┐
                    │  MemberRole  │  │  MemberRole  │  │  MemberRole   │
                    │  (MEMBER)    │  │  (WORKER)    │  │  (MERCHANT)   │
                    │  ACTIVE      │  │  ACTIVE      │  │  ACTIVE       │
                    └──────┬───────┘  └──────┬───────┘  └───────┬───────┘
                           │                 │                   │
                           │          ┌──────▼───────┐   ┌───────▼───────┐
                           │          │WorkerProfile │   │MerchantProfile│
                           │          │ 服务范围/车型 │   │ 店铺/营业时间  │
                           │          │ 在线状态      │   │ 配送半径       │
                           │          └──────────────┘   └───────────────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
     ┌────────▼───┐  ┌─────▼────┐  ┌───▼────────────┐
     │  Wallet     │  │Address   │  │Identity        │
     │ (钱包)      │  │(地址簿)  │  │Verification    │
     │ balance     │  │ 收货地址  │  │ (实名认证)     │
     │ frozen_amt  │  └──────────┘  └────────────────┘
     └─────────────┘
              │
     ┌────────▼───┐
     │ BankCard   │
     │ (银行卡)    │
     │ 用于提现    │
     └────────────┘
```

**场景示例**：张三同时是外卖商家 + 打车司机 + 普通用户
- 以 **MEMBER** 身份：点外卖、下单打车、参与投票
- 以 **WORKER** 身份：上线接打车订单，出现在抢单广播候选池
- 以 **MERCHANT** 身份：收到自己店铺的外卖订单，确认制作

### 5.2 外卖下单全链路（1:1 定向通知场景）

```
 用户(Member)                 平台                        商家(Merchant)
     │                          │                              │
     │  1. 创建订单              │                              │
     ├─────────────────────────►│                              │
     │                          │  work_order(CREATED)         │
     │                          │  order_type=DELIVERY         │
     │                          │                              │
     │                          │  2. 派发通知                  │
     │                          │  渲染模板 ORDER_CREATED_MERCHANT
     │                          │  notification(recipient=商家) │
     │                          ├─────────────────────────────►│
     │                          │  channels: IN_APP + PUSH     │
     │                          │                              │
     │                          │                              │  3. 确认制作
     │                          │ ◄────────────────────────────┤
     │                          │  order_transition:           │
     │                          │  CREATED→IN_PROGRESS         │
     │                          │                              │
     │  4. 状态推送              │                              │
     │  ◄────────────────────────┤                              │
     │  notification:            │                              │
     │  "商家已接单，正在制作"     │                              │
     │                          │                              │
     │            ...订单流转... │                              │
     │                          │                              │
     │                          │  5. 验收完成                  │
     │                          │  order_transition:           │
     │                          │  SUBMITTED→APPROVED→SETTLED  │
     │                          │                              │
     │                          │  6. 触发结算                  │
     │                          │  payment_order(PAY)          │
     │                          │  → payment_channel_route     │
     │                          │    channel=WECHAT_PAY        │
     │                          │  → payment_ledger_event      │
     │                          │  → wallet_transaction(商家入账)│
     │                          │  → wallet.balance += amount  │
```

### 5.3 打车下单全链路（1:N 广播抢单场景）

```
 用户(Member)                 平台                      附近车主(Worker×N)
     │                          │                              │
     │  1. 下打车单              │                              │
     ├─────────────────────────►│                              │
     │                          │  work_order(CREATED)         │
     │                          │  order_type=RIDE_HAIL        │
     │                          │                              │
     │                          │  2. 创建广播                  │
     │                          │  dispatch_broadcast          │
     │                          │  center=用户位置, r=3km       │
     │                          │  target_count=1              │
     │                          │  expire_at=now+30s           │
     │                          │                              │
     │                          │  3. Geo 查询候选              │
     │                          │  JOIN member_role(WORKER)    │
     │                          │  + worker_profile(ONLINE)    │
     │                          │  + worker_location(3km内)     │
     │                          │                              │
     │                          │  4. 推送抢单通知              │
     │                          ├─────────────────────────────►│ ×N
     │                          │  notification:               │
     │                          │  "附近有打车订单，点击抢单"    │
     │                          │                              │
     │                          │  5. 车主抢单（并发）           │
     │                          │◄─────────────────────────────┤
     │                          │  dispatch_grab_record        │
     │                          │  SELECT FOR UPDATE           │
     │                          │  第一个 → WIN，其余 → LOSE   │
     │                          │                              │
     │                          │  6. 匹配成功                  │
     │                          │  broadcast.status=GRABBED    │
     │                          │  work_order.worker_id=WIN    │
     │                          │  work_order.status=ACCEPTED  │
     │                          │                              │
     │  7. 通知匹配结果          │                              │
     │  ◄────────────────────────┤                              │
     │  "司机张三已接单，         │                              │
     │   车牌京A12345，2分钟后到" │                              │
```

### 5.4 支付与结算全链路（充值 / 消费 / 提现）

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Payment Gateway (统一支付网关)                    │
│                                                                          │
│  payment_order ←────→ payment_channel_route ←────→ payment_ledger_event  │
│  (统一订单)             (渠道路由)                  (渠道事件流)            │
│                              │                                           │
│              ┌───────────────┼───────────────┐                           │
│              ▼               ▼               ▼                           │
│        ┌──────────┐   ┌──────────┐   ┌──────────────┐                   │
│        │微信支付   │   │ 支付宝   │   │ 银联/银行转账 │                   │
│        │WECHAT_PAY│   │ ALIPAY   │   │ UNIONPAY     │                   │
│        └──────────┘   └──────────┘   └──────────────┘                   │
└──────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼                               ▼
           ┌──────────────┐               ┌──────────────────┐
           │ wallet        │               │ withdrawal_      │
           │ wallet_txn    │               │   request        │
           │ (充值/消费)   │               │ withdrawal_      │
           └──────────────┘               │   record (提现)   │
                                          │   → bank_card     │
                                          └──────────────────┘
```

### 5.5 业务订单状态机

```
                    CANCELLED ◄──────────────────────────────────┐
                      │                                         │
                      │ 超时/取消                                │
                      ▼                                         │
  CREATED ──► DISPATCHED ──► ACCEPTED ──► IN_PROGRESS ──► SUBMITTED ──► APPROVED ──► SETTLED ──► CLOSED
                 │              │              │            │           │
                 │              │              │            │           │ 返工
                 │              │              │            └─► REJECTED ──► IN_PROGRESS
                 │              │              │
                 │              │              └──► DISPUTED ──► dispute_records ──► 仲裁
                 │              │
                 │              └── 超时未接单自动撤回 ──► CREATED
                 │
                 └── 取消派单
```

| 状态 | 含义 | 触发方 |
|------|------|--------|
| CREATED | 订单已创建 | MEMBER |
| DISPATCHED | 已派发（广播中/定向指派） | SYSTEM |
| ACCEPTED | 劳动者/商家已接单 | WORKER / MERCHANT |
| IN_PROGRESS | 服务进行中 | WORKER / MERCHANT |
| SUBMITTED | 提交验收 | WORKER / MERCHANT |
| APPROVED | 验收通过 | MEMBER / SYSTEM(超时自动) |
| REJECTED | 验收不通过，需返工 | MEMBER |
| SETTLED | 已结算（触发支付） | SYSTEM |
| CLOSED | 订单关闭（终态） | SYSTEM |
| CANCELLED | 已取消（终态） | MEMBER / SYSTEM |
| DISPUTED | 争议中 | MEMBER / WORKER |

### 5.6 消息通知流

```
                    ┌─────────────────────────┐
                    │   Business Event         │
                    │ (订单创建/支付成功/...)   │
                    └───────────┬─────────────┘
                                │
                    ┌───────────▼─────────────┐
                    │ NotificationService      │
                    │ 1. 加载模板               │
                    │ 2. 占位符渲染             │
                    │ 3. 写入 notification      │
                    └───────────┬─────────────┘
                                │
                ┌───────────────┼───────────────┐
                ▼               ▼               ▼
          ┌──────────┐   ┌──────────┐   ┌──────────────┐
          │ IN_APP   │   │ PUSH     │   │ SMS          │
          │ WebSocket│   │ 极光/FCM │   │ 阿里云短信    │
          └──────────┘   └──────────┘   └──────────────┘
```

| 通知类型 | 渠道 | 场景 |
|----------|------|------|
| ORDER_CREATED_MERCHANT | IN_APP + PUSH | 外卖下单 → 商家 |
| ORDER_ACCEPTED | IN_APP + PUSH | 商家接单 → 用户 |
| DISPATCH_AVAILABLE | IN_APP + PUSH | 打车下单 → 附近车主（抢单） |
| DISPATCH_MATCHED | IN_APP + PUSH | 抢单成功 → 用户+车主 |
| PAYMENT_SUCCESS | IN_APP + SMS | 支付成功 → 用户 |
| WITHDRAWAL_RESULT | IN_APP + SMS | 提现结果 → 用户 |
| SETTLE_COMPLETED | IN_APP | 订单结算 → 劳动者 |

---

## 六、DDD 领域模型

### 6.1 限界上下文（Bounded Context）

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Platform Commons 系统                              │
│                                                                             │
│  ┌─────────────┐   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐ │
│  │  Identity    │   │  Matching    │   │  Payment     │   │ Notification │ │
│  │  身份域       │◄─►│  匹配域       │◄─►│  支付域       │◄─►│  通知域       │ │
│  │              │   │              │   │              │   │              │ │
│  │ Member       │   │ WorkOrder    │   │ Wallet       │   │ Notification │ │
│  │ MemberRole   │   │ OrderTrans   │   │ WalletTxn    │   │ Template     │ │
│  │ WorkerProfile│   │ Dispatch     │   │ PaymentOrder │   │              │ │
│  │ MerchantProf │   │ GrabRecord   │   │ Withdrawal   │   │              │ │
│  │ Identity     │   │              │   │ BankCard     │   │              │ │
│  │ Address      │   │              │   │ LedgerEvent  │   │              │ │
│  └──────┬───────┘   └──────┬───────┘   └──────┬───────┘   └──────────────┘ │
│         │                  │                  │                            │
│         │    ┌─────────────┼──────────────────┘                            │
│         │    │             │                                               │
│         ▼    ▼             ▼                                               │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐   ┌────────────┐ │
│  │ Governance   │   │  Mutual      │   │  Dispute     │   │EarlyWarning│ │
│  │ 治理域        │   │  互助域       │   │  争议域       │   │ 预警域      │ │
│  └──────────────┘   └──────────────┘   └──────────────┘   └────────────┘ │
│                                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐                   │
│  │ Finance      │   │ TechGovern   │   │ AISupervision│                   │
│  │ 财务域        │   │ 技术治理域    │   │ AI监督域     │                   │
│  └──────────────┘   └──────────────┘   └──────────────┘                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 6.2 核心域详细模型

#### ① Identity Context（身份域）

| 层级 | 对象 | 类型 | 说明 |
|------|------|------|------|
| **聚合根** | `Member` | Aggregate Root | 核心身份，持有 name/phone/status，管理角色集合 |
| 实体 | `MemberRole` | Entity | 角色注册项，状态机 PENDING→ACTIVE→SUSPENDED→REVOKED |
| 实体 | `WorkerProfile` | Entity | 劳动者档案（服务范围、车型、在线状态）— WORKER 角色专属 |
| 实体 | `MerchantProfile` | Entity | 商家档案（店铺、营业时间）— MERCHANT 角色专属 |
| 实体 | `IdentityVerification` | Entity | 实名认证（加密证件号、人脸核验） |
| 实体 | `Address` | Entity | 地址簿（收货/居住地址） |
| 值对象 | `RoleType` | Value Object (enum) | MEMBER / WORKER / MERCHANT / ADMIN / REVIEWER |
| 值对象 | `RoleStatus` | Value Object (enum) | PENDING / ACTIVE / SUSPENDED / REVOKED |
| 值对象 | `GeoLocation` | Value Object (record) | latitude + longitude，不可变 |
| 领域事件 | `RoleActivated` | Domain Event | 角色激活（WORKER 上线触发抢单池注册） |
| 领域事件 | `MemberRegistered` | Domain Event | 新成员注册（触发钱包自动创建） |

**聚合不变量**：
- 一个 Member 同时可持有多个 RoleType，但每种角色只能有一个 MemberRole
- WorkerProfile / MerchantProfile 仅在对应角色为 ACTIVE 时可操作
- Member 被冻结时，所有角色联动 SUSPENDED

#### ② Matching Context（匹配域）

| 层级 | 对象 | 类型 | 说明 |
|------|------|------|------|
| **聚合根** | `WorkOrder` | Aggregate Root | 业务订单，状态机守护者 |
| 实体 | `OrderTransition` | Entity | 状态流转记录（不可变追加日志） |
| **聚合根** | `DispatchBroadcast` | Aggregate Root | 抢单广播（独立生命周期） |
| 实体 | `DispatchGrabRecord` | Entity | 抢单记录，并发竞争裁决 |
| 值对象 | `OrderType` | Value Object (enum) | LABOR / DELIVERY / RIDE_HAIL / MUTUAL_ASSIST |
| 值对象 | `OrderStatus` | Value Object (enum) | CREATED → ... → CLOSED（11 态） |
| 值对象 | `BroadcastType` | Value Object (enum) | GRAB（抢单）/ ASSIGN（指派） |
| 值对象 | `Money` | Value Object (record) | amount + currency，不可变 |
| 领域事件 | `OrderCreated` | Domain Event | 订单创建 → 触发通知/广播 |
| 领域事件 | `OrderAccepted` | Domain Event | 接单 → 触发用户通知 |
| 领域事件 | `OrderSettled` | Domain Event | 结算 → 触发支付域 |
| 领域事件 | `DispatchGrabbed` | Domain Event | 抢单成功 → 触发匹配完成 |
| 领域服务 | `OrderStateMachine` | Domain Service | 校验状态迁移合法性，拒绝非法流转 |

**聚合不变量**：
- `OrderTransition` 必须遵循状态机合法迁移路径
- `DispatchBroadcast.grabbedCount <= targetCount`（不可超额匹配）
- 同一 broadcast + worker 只能有一条 grab_record（唯一约束）
- SETTLED 状态必须有关联的 payment_order

#### ③ Payment Context（支付域）

| 层级 | 对象 | 类型 | 说明 |
|------|------|------|------|
| **聚合根** | `Wallet` | Aggregate Root | 用户钱包，余额 + 冻结额 |
| 实体 | `WalletTransaction` | Entity | 钱包流水（不可变追加账本） |
| **聚合根** | `PaymentOrder` | Aggregate Root | 统一支付订单 |
| 实体 | `PaymentChannelRoute` | Entity | 渠道路由记录（可重试多渠道） |
| **聚合根** | `WithdrawalRequest` | Aggregate Root | 提现申请（独立状态机） |
| 实体 | `WithdrawalRecord` | Entity | 提现渠道执行记录 |
| 实体 | `BankCard` | Entity | 银行卡绑定 |
| 实体 | `LedgerEvent` | Entity | 渠道事件流水（原有，增强渠道归属） |
| 值对象 | `PaymentDirection` | Value Object (enum) | PAY（支付）/ RECHARGE（充值） |
| 值对象 | `ChannelCode` | Value Object (enum) | WECHAT_PAY / ALIPAY / UNIONPAY / BANK_TRANSFER |
| 值对象 | `WalletTxnDirection` | Value Object (enum) | IN / OUT |
| 领域事件 | `WalletDebited` | Domain Event | 钱包支出 |
| 领域事件 | `WalletCredited` | Domain Event | 钱包入账 |
| 领域事件 | `PaymentSucceeded` | Domain Event | 支付成功 → 触发通知 |
| 领域事件 | `WithdrawalCompleted` | Domain Event | 提现完成 → 触发通知 |
| 领域服务 | `PaymentGateway` | Domain Service | 渠道路由策略（SPI 接口，按渠道分发） |
| 领域服务 | `WalletLedger` | Domain Service | 余额变更 + 追加流水（原子操作） |

**聚合不变量**：
- `Wallet.balance >= 0`（不可透支）
- `WalletTransaction.balanceAfter` 必须与写入后的 Wallet.balance 一致
- `PaymentOrder.status=PAID` 时必须有成功的 channel_route
- `WithdrawalRequest.amount <= Wallet.balance - frozenAmount`

#### ④ Notification Context（通知域）

| 层级 | 对象 | 类型 | 说明 |
|------|------|------|------|
| **聚合根** | `Notification` | Aggregate Root | 单条通知消息 |
| 实体 | `NotificationTemplate` | Entity | 模板（占位符渲染） |
| 值对象 | `NotificationCategory` | Value Object (enum) | ORDER / PAYMENT / GOVERNANCE / SYSTEM |
| 值对象 | `DeliveryChannel` | Value Object (enum) | IN_APP / PUSH / SMS（可组合） |
| 值对象 | `NotificationStatus` | Value Object (enum) | PENDING / SENT / DELIVERED / READ / FAILED |
| 领域事件 | `NotificationRead` | Domain Event | 已读（更新统计） |
| 领域服务 | `NotificationRenderer` | Domain Service | 模板加载 + 占位符替换 |
| 领域服务 | `ChannelDispatcher` | Domain Service | 多渠道分发（SPI 接口） |

**聚合不变量**：
- `Notification.status` 只能单向推进：PENDING → SENT → DELIVERED → READ
- FAILED 为终态，不可回退

### 6.3 上下文映射（Context Map）

```
                     Identity
                   ╱    │    ╲
            U/D   U/D   U/D   U/D
             ╱      │      ╲
    Matching ◄──────┼──────► Payment
        │           │           │
     U/D         [OHS]       U/D
        │           │           │
        ▼           ▼           ▼
    Notification ◄──┘           │
        │                       │
     [PL]                    [PL]
        │                       │
        ▼                       ▼
    Governance ◄────────── Dispute
        │                       │
        ▼                       ▼
    EarlyWarning ◄────── AISupervision
                               ▲
                               │
                          TechGovern
```

| 关系类型 | 符号 | 含义 | 示例 |
|----------|------|------|------|
| U/D | 上游/下游 | 客户-供应商关系 | Identity→Matching（身份提供成员信息） |
| OHS | 开放主机服务 | 公开 API + 协议 | Notification 对外提供通知发送接口 |
| PL | 遵奉者 | 被动跟随上游模型 | Dispute 遵奉 Matching 的订单概念 |
| ACL | 防腐层 | 翻译+隔离 | 各域引用 member_id 时不直接依赖 Identity 的内部模型 |

**核心上下文关系**：

| 上游 → 下游 | 关系 | 协作方式 |
|-------------|------|----------|
| Identity → Matching | U/D | Matching 通过 memberId 查询劳动者/商家信息 |
| Identity → Payment | U/D | Payment 通过 memberId 查询钱包归属 |
| Matching → Payment | U/D | 订单 SETTLED 时触发 Payment 结算 |
| Matching → Notification | OHS | 订单状态变更通过 Notification 域发送通知 |
| Payment → Notification | OHS | 支付/提现结果通过 Notification 域发送通知 |
| Matching → Dispute | U/D | 订单 DISPUTED 时创建争议记录 |
| Dispute → Matching | ACL | 仲裁结果回流（返工/退款）时翻译为订单状态变更 |
| All → Notification | OHS | 所有域的事件通知统一走 Notification 开放主机服务 |

### 6.4 领域事件流（跨上下文协作）

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Identity: MemberRegistered                                             │
│      │ (event)                                                          │
│      ├──► Payment: 自动创建 Wallet                                       │
│      └──► Notification: 发送欢迎通知                                     │
│                                                                         │
│  Identity: RoleActivated(WORKER)                                        │
│      │ (event)                                                          │
│      └──► Matching: 注册到抢单候选池                                      │
│                                                                         │
│  Matching: OrderCreated(DELIVERY)                                       │
│      │ (event)                                                          │
│      ├──► Notification: ORDER_CREATED_MERCHANT → 通知商家                │
│      │                                                                 │
│  Matching: OrderCreated(RIDE_HAIL)                                      │
│      │ (event)                                                          │
│      ├──► Matching: 创建 DispatchBroadcast                               │
│      └──► Notification: DISPATCH_AVAILABLE → 广播给附近车主               │
│                                                                         │
│  Matching: DispatchGrabbed                                              │
│      │ (event)                                                          │
│      └──► Notification: DISPATCH_MATCHED → 通知用户+车主                  │
│                                                                         │
│  Matching: OrderSettled                                                 │
│      │ (event)                                                          │
│      ├──► Payment: 创建 PaymentOrder + 渠道路由                           │
│      ├──► Payment: WalletTransaction(商家入账)                            │
│      └──► Notification: SETTLE_COMPLETED → 通知劳动者                     │
│                                                                         │
│  Payment: PaymentSucceeded                                              │
│      │ (event)                                                          │
│      └──► Notification: PAYMENT_SUCCESS → 通知用户                        │
│                                                                         │
│  Payment: WithdrawalCompleted                                           │
│      │ (event)                                                          │
│      └──► Notification: WITHDRAWAL_RESULT → 通知用户                      │
└─────────────────────────────────────────────────────────────────────────┘
```

### 6.5 聚合设计原则

| 原则 | 落地方式 |
|------|----------|
| **聚合内强一致** | 单个聚合内的操作在同一个数据库事务中完成 |
| **聚合间最终一致** | 跨聚合通过领域事件（Outbox 模式 + 异步消息）保证一致性 |
| **引用用 ID** | 聚合之间只持有对方的 ID（memberId / orderId），不持有对象引用 |
| **事务边界 = 聚合边界** | 一次事务只修改一个聚合根 |
| **领域事件先行** | 聚合状态变更后发布事件，由消费者异步更新其他聚合 |

### 6.6 聚合与表映射

| 聚合根 | 所属上下文 | 持有表 | 守护不变量 |
|--------|-----------|--------|-----------|
| `Member` | Identity | member, member_role, worker_profile, merchant_profile, identity_verification, address | 角色-档案一致性 |
| `Wallet` | Payment | wallet, wallet_transaction | 余额非负、流水连续 |
| `PaymentOrder` | Payment | payment_order, payment_channel_route | 支付-渠道一致性 |
| `WithdrawalRequest` | Payment | withdrawal_request, withdrawal_record | 提现状态机合法 |
| `WorkOrder` | Matching | work_order, order_transition | 订单状态机合法 |
| `DispatchBroadcast` | Matching | dispatch_broadcast, dispatch_grab_record | 抢单不超额 |
| `Notification` | Notification | notification, notification_template | 状态单向推进 |
