# 开发规范 · Platform Commons Coding Conventions

> 本规范在《阿里巴巴 Java 开发手册（黄山版）》基础上，结合项目实际编码约定形成。
> 所有协作者提交代码前请逐条对照，CR 不合规代码一律打回。

## 一、通用编码

### 1.1 禁止方法体内写全包名路径

方法体内出现的所有类型必须 `import` 短类名，禁止内联全限定名。

```java
// ❌ 禁止
throw new com.platformcommons.common.exception.BusinessException(
        com.platformcommons.common.api.ResultCode.DATA_NOT_FOUND, "交易不存在");

// ✅ 正确
import com.platformcommons.common.exception.BusinessException;
import com.platformcommons.common.api.ResultCode;

throw new BusinessException(ResultCode.DATA_NOT_FOUND, "交易不存在");
```

### 1.2 注解按行宽从短到长排列

同一目标上的多个注解，按属性文本长度从短到长排列，视觉整齐便于阅读。

```java
// ❌ 杂乱
@PostMapping("/api/payment/charge")
@Valid
@Slf4j

// ✅ 短→长
@Slf4j
@RestController
@RequiredArgsConstructor
```

### 1.3 构造器注入：@RequiredArgsConstructor + final

需要依赖注入时，用 Lombok `@RequiredArgsConstructor` + `final` 字段，禁止手写构造器。

```java
// ❌ 禁止手写
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}

// ✅ 正确
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
}
```

### 1.4 null 兜底用 Optional

```java
// ❌ 禁止
String name = (user != null) ? user.getName() : "匿名";

// ✅ 正确
String name = Optional.ofNullable(user).map(User::getName).orElse("匿名");
```

> **⚠️ orElse vs orElseGet**：`orElse(defaultValue)` 的参数**急切求值**——无论是否为空都会执行。当默认值涉及方法调用（如 `SnowflakeUtils.nextId()`）时必须用 `orElseGet(Supplier)` 懒求值，否则白白消耗资源。
>
> ```java
> // ❌ nextId() 每次都执行，即使 recordId 非空
> var id = Optional.ofNullable(record.recordId()).orElse(SnowflakeUtils.nextId());
>
> // ✅ nextId() 仅在 recordId 为空时才执行
> var id = Optional.ofNullable(record.recordId()).orElseGet(SnowflakeUtils::nextId);
> ```

### 1.5 消除冗余防御校验

信任同模块内的调用链，只在边界（Controller 入口、外部 API 返回）做校验，禁止层层重复判空。

```java
// ❌ Service 里重复校验 Controller 已校验过的参数
public void process(Long id) {
    if (id == null || id <= 0) { ... }  // Controller @Valid 已保证
}

// ✅ 直接使用
public void process(Long id) {
    repository.findById(id)...
}
```

### 1.6 字符串判空统一 StringUtils.hasText()

```java
// ❌ 禁止手动 null + isBlank
if (str != null && !str.isBlank()) { ... }

// ✅ 正确
if (StringUtils.hasText(str)) { ... }
```

> 使用 `org.springframework.util.StringUtils`，无需额外依赖。

---

## 二、Controller 层

### 2.1 类上禁写 @RequestMapping，方法上写全路径

**每个 Controller 方法必须在自己的映射注解上写完整的 API 路径**，类上不聚合前缀。

目的：排查线上问题时可以根据请求 path 直接全局搜索定位到对应方法，不用脑补拼接类前缀 + 方法路径。

```java
// ❌ 禁止——类上聚合前缀
@RestController
@RequestMapping("/api/governance/proposals")
public class GovernanceController {

    @PostMapping                          // 实际路径 = 类前缀 + 空，搜索不到
    public Proposal create(...) { }

    @GetMapping("/{id}/result")
    public VoteResultResponse tally(...) { }  // 实际路径需心算拼接
}

// ✅ 正确——方法上写全路径
@RestController
public class GovernanceController {

    @PostMapping("/api/governance/proposals")
    public Proposal create(...) { }

    @GetMapping("/api/governance/proposals/{id}/result")
    public VoteResultResponse tally(...) { }
}
```

### 2.2 返回裸对象，不手写 R.success()

Controller 方法直接返回业务对象（DTO / Domain），由 `GlobalResponseAdvice` 自动包装为 `R<T>`。

```java
// ❌ 禁止
@PostMapping("/api/members")
public R<MemberResponse> register(@Valid @RequestBody MemberRegisterRequest request) {
    return R.success(memberService.register(request));
}

// ✅ 正确
@PostMapping("/api/members")
public MemberResponse register(@Valid @RequestBody MemberRegisterRequest request) {
    return memberService.register(request);
}
```

> 已由 `GlobalResponseAdvice`（`ResponseBodyAdvice`）统一包装，业务代码与响应结构解耦。

### 2.3 Controller 只做参数校验与调度

Controller 层禁止写业务逻辑，只负责：接收参数 → 调 Service → 返回结果。

---

## 三、统一响应与异常

### 3.1 响应体 R\<T\>

所有接口统一返回 `R<T>` 结构（由 `GlobalResponseAdvice` 自动包装）：

| 字段 | 说明 |
|------|------|
| `code` | `0` 成功，其他为错误码 |
| `message` | 提示信息 |
| `data` | 业务数据 |
| `traceId` | 链路追踪 ID |
| `timestamp` | 响应时间戳（毫秒） |

### 3.2 错误码 ResultCode

错误码分段：

| 段 | 含义 |
|----|------|
| `0` | 成功 |
| `1xxxx` | 通用客户端错误 |
| `5xxxx` | 服务端错误 |
| `6xxxx` | 业务错误 |

业务异常统一抛 `BusinessException(ResultCode, message)`，由全局异常处理器兜底。

---

## 四、异步与虚拟线程

### 4.1 全局已启用虚拟线程

项目已在 Spring Boot 层面全局启用虚拟线程（`spring.threads.virtual.enabled=true`）。

- Controller 请求处理、`@Async`、`Stream.parallelStream()` 等框架调度场景**自动使用虚拟线程**
- 新人**无需手动创建线程池**处理 IO 密集型任务

### 4.2 手动异步 → 统一走 VirtualThreads

需要手动发起异步任务时，**必须使用 `VirtualThreads` 工具类**，禁止裸用 `Thread.startVirtualThread` / `Executors.newVirtualThreadPerTaskExecutor`。

该工具类内部自动传播 MDC 链路追踪上下文（`TraceContext.wrap`），调用方无需手动处理。

```java
// 1. fire-and-forget（无返回值）
VirtualThreads.runAsync(() -> sendNotification(userId));

// 2. 需要返回值
CompletableFuture<String> future = VirtualThreads.supplyAsync(() -> queryExternalApi());

// 3. 批量并发（ExecutorService 必须 try-with-resources 关闭）
try (ExecutorService pool = VirtualThreads.newExecutor("batch-import")) {
    for (var item : items) {
        pool.submit(() -> processItem(item));
    }
}
```

### 4.3 使用红线

| 🚫 禁止 | ✅ 正确 |
|---------|---------|
| `new Thread()` 创建平台线程 | `VirtualThreads.runAsync()` |
| 裸用 `Thread.startVirtualThread` | `VirtualThreads.runAsync()` |
| 裸用 `Executors.newVirtualThreadPerTaskExecutor` | `VirtualThreads.newExecutor(name)` |
| `synchronized` 包裹 IO 阻塞代码 | 用 `ReentrantLock` 替代 |
| `ThreadLocal` + 虚拟线程（内存膨胀） | 避免，或用 scoped values |
| 池化虚拟线程 | 一个任务一个虚拟线程，用完即弃 |

### 4.3 CPU 密集型 → PlatformThreads

加解密、压缩、序列化、大计算等 **CPU 密集型任务**用 `PlatformThreads`，不要用虚拟线程。

全局唯一共享固定大小线程池（核心数 = `Runtime.availableProcessors()`），内置 MDC 传播 + JVM shutdown hook 自动优雅停机。

```java
// 1. 无返回值
PlatformThreads.runAsync(() -> compressImage(bytes));

// 2. 有返回值
CompletableFuture<byte[]> future = PlatformThreads.supplyAsync(() -> encrypt(data));

// 3. 批量并发
List<CompletableFuture<Result>> futures = tasks.stream()
        .map(t -> PlatformThreads.supplyAsync(() -> process(t)))
        .toList();
futures.forEach(CompletableFuture::join);
```

> **选择指南**：IO 密集型 → `VirtualThreads`；CPU 密集型 → `PlatformThreads`。

### 4.4 @Async 自动传播 traceId

`MdcTaskDecorator` 已注册为 Spring Bean，`@Async` 方法会自动继承调用方的 `traceId`，无需手动传递。

---

## 五、分层架构

严格遵循阿里规范应用分层：

```
Controller（api/）    → 开放接口、参数校验、视图适配
    ↓
Service（service/）   → 业务逻辑、组合调用
    ↓
Manager（manager/）   → 通用封装、缓存、降级（按需）
    ↓
Repository（repository/） → 数据访问、Entity 映射
```

- `api/` → Controller + DTO（Request/Response record）
- `service/` → 业务逻辑
- `domain/` → 领域模型（优先 record / sealed）
- `repository/` → JPA Entity + Repository

---

## 六、日志

- 统一使用 Lombok `@Slf4j`
- 占位符用 `{}`，禁止字符串拼接
- 异常日志打印完整堆栈：`log.error("msg", exception)`

---

## 七、基础设施约定

| 约定 | 说明 |
|------|------|
| Java 版本 | 25 (Zulu) |
| Spring Boot | 4.1.0 |
| Gradle | 9.6.1（运行时 JDK 21，编译 JDK 25） |
| 包名 | `com.platformcommons.{模块名}` |
| 链路追踪 | `TraceContext`（MDC + `TraceIdFilter`），`@Async` 用 `MdcTaskDecorator` 自动传播 |
| ID 生成 | `SnowflakeUtils`（32 位） |
| 虚拟线程 | 已全局启用 |
