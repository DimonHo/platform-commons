# Platform-Commons MVP 编码推进计划

> **For Claude:** REQUIRED SUB-SKILL: Use executing-plans to implement this plan task-by-task.

**Goal:** 一个真实可跑的 MVP：走通 **注册 → 下单 → 接单 → 完成 → 结算** 全链路，数据全部落 PG，可用 curl 端到端验证。

**Architecture:** 单库扁平 schema（V1 11 表 + V2 18 表已就绪），20 模块 Gradle 多模块，JPA + Flyway + PG16。MVP 不引入分库分表/消息队列/网关，模块间通过 Service 接口（compileOnly）调用。

**Tech Stack:** Java 25 / Spring Boot 4.1 / Gradle 9.6.1 / PG16 / Redis7（仅预留）/ Flyway / Lombok

---

## 0. 现状基线（已核实，2026-08-06）

| 模块 | 代码量 | 状态 | MVP 角色 |
|---|---|---|---|
| platform-common | 24 文件 | ✅ 测试通过（Snowflake/Trace/GlobalResponseAdvice） | 基础设施 |
| platform-identity | 51 文件 | ⚠️ 全链路已写但 **全部 untracked 未提交** | **P1 注册** |
| platform-matching | 47 文件 | ⚠️ WorkOrder/Dispatch/Matching 三 Controller 齐全，**MatchingEngine 内存态** | **P2 下单接单** |
| platform-payment | 64 文件 | ⚠️ Wallet/Payment/Gateway/BankCard/Withdrawal 齐全，**PaymentService 内存台账** | **P3 结算** |
| platform-notification | 21 文件 | 已实现 | 旁路（MVP 不接） |
| governance/mutual/dispute/finance/ai-supervision/tech-governance/early-warning | 10-15 文件 | V1 表代码 | 不阻塞 MVP |
| asset/booth/dispatch/emergency/federation/labor/rating | 空壳 | 无代码 | MVP 不启动 |

**关键事实：**
- V1 `member` 表含 `roles` 列，MemberEntity 字段匹配 ✅（记忆中"roles 断裂"已不存在，无需修复）
- `work_order` 状态机完备：CREATED→DISPATCHED→ACCEPTED→…→SETTLED，`order_no` 唯一约束可做幂等
- V2 的 `member_role` 与 V1 `member.roles` 语义重叠 → **MVP 保留 member.roles，member_role 降级为只读旁路**（不阻塞，避免双写）
- 目前仅 platform-common 有测试；identity/matching/payment 均无测试

---

## 1. 阶段总览

| 阶段 | 内容 | 交付物 | 依赖 |
|---|---|---|---|
| **R0** | DDD-B 分包重构（已完成：`306c480` `8b3e520` `e5f3461`） | identity/matching/payment 聚合分包 | — |
| **P0** | 基线收口：JDK25 编译全绿 | build 全绿 | R0 |
| **P1** | 注册链路（identity） | 注册/角色/资料 curl 可验 | P0 |
| **P2** | 下单+接单链路（matching） | 内存态 → JPA 落库，状态机可走 | P1 |
| **P3** | 完成+结算链路（payment） | 结算落库 + 钱包入账 + 金额守恒 | P2 |
| **P4** | 端到端联调 | seed + E2E 脚本一键跑通 | P3 |

**MVP 状态机裁剪**（对应 work_order.status）：`CREATED → DISPATCHED → ACCEPTED → COMPLETED → SETTLED`，CANCELLED 兜底。IN_PROGRESS/SUBMITTED/APPROVED/DISPUTED 留待 V1.1。

---

## 2. P0：基线收口

**功能点：**
1. JDK 25 安装（清华 Adoptium 镜像，`/opt/data/jdk25`）——此前仅有 JDK 21，toolchain 强制 25
2. 全模块 `build` 编译绿（验证 R0 重构后 import/package 一致）

**改动文件：** 无代码改动

**验收目标：**
```bash
export JAVA_HOME=/opt/data/jdk25
cd /opt/data/workspace/platform-commons
./gradlew build -x test   # 期望：BUILD SUCCESSFUL，0 error
```

---

## 3. P1：注册链路（platform-identity）

**功能点：**

| # | 功能点 | 接口 | 说明 |
|---|---|---|---|
| 1 | 会员注册 | `POST /api/v1/members` | name+phone 落库，Snowflake 生成 id |
| 2 | 角色资料 | `POST /api/v1/profiles/worker` `/merchant` | worker_profile / merchant_profile 落库 |
| 3 | 角色申请 | `POST /api/v1/members/{id}/roles` | member_role 落库（旁路，不阻塞） |
| 4 | 身份认证 | `POST /api/v1/verifications` | 提交 + 审核状态流转 |
| 5 | 地址簿 | `POST /api/v1/addresses` | 冗余字段，CRUD 即可 |

**改动文件：** 均为已存在文件（DDD-B 后新路径），补测试：
- `backend/platform-identity/src/test/java/.../MemberServiceTest.java`（新建）
- 已有 `application/impl/MemberServiceImpl.java` 等如缺 `@Transactional` 补上（多表写必须同事务）
- **必修**：`domain/member/MemberEntity.java` 删 `roles` 字段（V2 已 `DROP COLUMN roles`，启动 JPA 校验会失败）；同步清理 `MemberServiceImpl` 的 `serializeRoles/deserializeRoles/hasWorker` 与 `MemberRegisterRequest.roles`（角色职责已迁至 `member_role` 表，走 `ProfileController` 的角色端点）

**验收目标：**
```bash
./gradlew :backend:platform-identity:test   # 期望：PASS
# 启动 bootstrap 后（PG 需先建库）：
curl -s -X POST localhost:8080/api/v1/members -H 'Content-Type: application/json' \
  -d '{"name":"张师傅","phone":"13800000001"}'    # 期望：200，返回 id
curl -s -X POST localhost:8080/api/v1/profiles/worker -H 'Content-Type: application/json' \
  -d '{"memberId":1,"skills":["木工"],"hourlyRate":80}'   # 期望：200
psql -c "SELECT id,name,roles FROM member; SELECT member_id FROM worker_profile;"  # 期望：各 1 行
```

---

## 4. P2：下单+接单链路（platform-matching）

**功能点：**

| # | 功能点 | 接口 | 落库 |
|---|---|---|---|
| 1 | 下单 | `POST /api/v1/work-orders` | work_order(CREATED) |
| 2 | 派单广播 | `POST /api/v1/dispatch/broadcasts` | dispatch_broadcast + work_order→DISPATCHED |
| 3 | 抢单 | `POST /api/v1/dispatch/grabs` | dispatch_grab_record + work_order→ACCEPTED + worker_id 回填 |
| 4 | 状态流转 | `POST /api/v1/work-orders/{id}/transitions` | order_transition 全量落库（审计） |

**核心改造：内存态 → JPA**（DDD-B 后新路径）
- `application/impl/MatchingEngineServiceImpl.java`：删 ConcurrentHashMap `workerStore`，`match()`/`listWorkers()` 改读 `domain/location/WorkerLocationRepository`（`findAll` + 反榨取过滤 `findByActiveOrdersLessThan`）；`registerWorker` 已落库保留
- `application/impl/DispatchServiceImpl.java`：抢单并发用 `@Version` 乐观锁（`domain/workorder/WorkOrderEntity` 加 `version` 列，V3 迁移脚本已就绪；`grabbedCount` 读改写需乐观重试）
- `application/impl/WorkOrderServiceImpl.java`：状态迁移校验已完备（TRANSITION_RULES），仅需补测试

**改动文件：**
- 修改：`application/impl/MatchingEngineServiceImpl.java`、`application/impl/DispatchServiceImpl.java`
- 修改：`domain/workorder/WorkOrderEntity.java`（+`version` 字段）
- 已建：`backend/platform-bootstrap/src/main/resources/db/migration/V3__add_payment_transaction_and_version.sql`（含 work_order.version）
- 新建：`platform-matching/src/test/java/.../WorkOrderFlowTest.java`（状态机 + 乐观锁并发用例）

**验收目标：**
```bash
./gradlew :backend:platform-matching:test   # 期望：PASS（含并发抢单用例）
curl -s -X POST localhost:8080/api/v1/work-orders -H 'Content-Type: application/json' \
  -d '{"memberId":1,"title":"修水管","amount":200,"orderType":"SERVICE","lat":30.1,"lng":120.2}'  # 200
curl -s -X POST localhost:8080/api/v1/dispatch/grabs -H 'Content-Type: application/json' \
  -d '{"workOrderId":1,"workerId":2}'   # 期望：200
psql -c "SELECT status,worker_id FROM work_order WHERE id=1;"  # 期望：ACCEPTED | 2
psql -c "SELECT count(*) FROM order_transition;"               # 期望：≥3（CREATE/DISPATCH/ACCEPT）
```

---

## 5. P3：完成+结算链路（platform-payment）

**功能点：**

| # | 功能点 | 接口 | 落库 |
|---|---|---|---|
| 1 | 完成 | transition COMPLETED | work_order 终态前置 |
| 2 | 结算 | `POST /api/v1/payments/settle` | payment_order(SUCCESS) + work_order→SETTLED |
| 3 | 分账 | SettlementRule 佣金规则 | 平台佣金 + 工人实收拆分 |
| 4 | 钱包入账 | `POST /api/v1/wallets/{id}/transactions` | wallet 余额变更 + wallet_transaction 流水 |
| 5 | 对账 | 金额守恒断言 | 流水合计 = 订单金额 |

**核心改造：内存台账 → JPA**（DDD-B 后新路径）
- `application/impl/PaymentServiceImpl.java`：删 ConcurrentHashMap `transactionStore` → 新建 `domain/transaction/TransactionEntity` + `TransactionRepository` 落库（V3 表 `payment_transaction` 已就绪）；幂等键 `order_id` 唯一约束兜底
- 结算事务顺序：查 work_order(SETTLED 前置校验) → 建 payment_transaction → 扣需求方 wallet → 加 worker wallet → 同事务提交，任一失败整体回滚
- `domain/transaction/LedgerEventEntity.java`（原 payment 模块 M 文件，已随 R3 合并）——V2 已 ALTER 补列，映射正常

**改动文件：**
- 修改：`application/impl/PaymentServiceImpl.java`（JPA 化 + 事务 + 幂等）
- 新建：`domain/transaction/TransactionEntity.java`、`domain/transaction/TransactionRepository.java`
- 新建：`platform-payment/src/test/java/.../SettlementFlowTest.java`（结算 + 金额守恒）

**验收目标：**
```bash
./gradlew :backend:platform-payment:test   # 期望：PASS
curl -s -X POST localhost:8080/api/v1/payments/settle -H 'Content-Type: application/json' \
  -d '{"workOrderId":1}'    # 期望：200，返回 settlement 明细
psql -c "SELECT status FROM work_order WHERE id=1;"                      # 期望：SETTLED
psql -c "SELECT balance FROM wallet WHERE member_id=2;"                  # 期望：amount - 佣金
psql -c "SELECT sum(amount) FROM wallet_transaction WHERE work_order_id=1;"  # 期望：= 订单金额（守恒）
```

---

## 6. P4：端到端联调

**功能点：**
1. `scripts/seed.sql`：预置 merchant + worker + 双方钱包初始余额
2. `scripts/e2e.sh`：curl 串行执行 注册→下单→接单→完成→结算，每步断言 HTTP 200 + 关键状态
3. 失败即停（`set -e` + 每步 grep 校验响应体）

**改动文件：**
- 新建：`scripts/seed.sql`、`scripts/e2e.sh`

**验收目标：**
```bash
./gradlew :backend:platform-bootstrap:bootRun &   # 或 docker 起 PG 后
bash scripts/e2e.sh   # 期望：打印全链路日志，最后输出 "MVP E2E PASS: 注册→下单→接单→完成→结算"
```

---

## 7. 风险与决策

| 风险 | 决策（首推） | 备选 |
|---|---|---|
| 抢单并发超卖 | work_order 加 `version` 乐观锁，重试 3 次 | Redis 分布式锁（MVP 不引入） |
| 结算重复调用 | `order_no` 唯一约束 + 状态前置校验幂等 | 幂等表（V1.1） |
| member.roles 与 member_role 重叠 | MVP 保留 member.roles 单写 | 迁移到 member_role（V1.1 删列） |
| identity 未提交代码与 schema 脱节 | P0 先提交再编译，暴露即修 | — |

**关键决策（首推）：** 结算为全链路唯一写事务，worker 到账与 work_order 终态强一致；订单金额 `DECIMAL(19,4)` 全链路 BigDecimal，禁止 double。

---

## 8. 执行方式选择

计划落盘后二选一：

**1. Subagent-Driven（首推）**：本会话逐阶段分派子代理实现，每阶段完成做 code review 再进下一阶段，节奏快且质量可控。

**2. 并行 Session**：新开会话用 executing-plans 批量执行，按 checkpoint 验收。
