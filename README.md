# 平台共同体 · Platform Commons

> **公共所有 · 劳动优先 · 民主治理 · 透明可核验**
>
> 基于《平台共同体宪章 v0.2》构建的数字平台合作社基础设施。

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

## 项目定位

平台共同体是由劳动者、消费者、商户与服务提供者、公共成员共同治理的公共利益数字共同体。

**不以资本增值或股东利润最大化为目的**，以组织真实需求、降低交易成本、保障劳动尊严、扩展公共就业、建设共同技术基础设施为目的。

## 技术栈

| 项目 | 版本 |
|------|------|
| Java | 27 (EA) |
| Spring Boot | 4.1.0 |
| PostgreSQL | 16 + PostGIS |
| 构建工具 | Gradle (Groovy DSL) |
| 开发规范 | [CONVENTIONS.md](CONVENTIONS.md)（基于阿里黄山版） |
| 开源许可证 | AGPL-3.0 |

## 六项永久锁

本平台的技术架构严格遵循宪章六项永久原则：

1. **资本无治理权** — 出资不换取表决权
2. **公共资产不可分割** — 资产只能留在共同体或转交同类公共组织
3. **权力可撤销** — 所有授权有期限、罢免程序、自动失效日
4. **技术无超级个人** — 关键操作多方批准、全量留痕
5. **核心系统可验证开源** — AGPL-3.0 + 可复现构建
6. **AI 无最终统治权** — AI 仅作审议，不作最终决定

## 模块架构

```
platform-commons/
├── backend/
│   ├── platform-common/          # 共享基础：常量、枚举、异常、工具
│   ├── platform-bootstrap/       # 启动入口
│   ├── platform-identity/        # 成员资格（宪章第3章）
│   ├── platform-governance/      # 四院治理（宪章第4-5章）
│   ├── platform-asset/           # 共同资产管理（宪章第2章）
│   ├── platform-labor/           # 劳动保障（宪章第8章）
│   ├── platform-payment/         # 支付分账（宪章第9章）
│   ├── platform-mutual/          # 劳动意外互助基金（宪章第14章）
│   ├── platform-matching/        # 匹配引擎（反榨取约束）
│   ├── platform-dispatch/        # 调度引擎（尊重劳动者偏好）
│   ├── platform-rating/          # 双向评价信用
│   ├── platform-finance/         # 融资采购合规（宪章第10-11章）
│   ├── platform-ai-supervision/  # AI公共监督（宪章第12章）
│   ├── platform-tech-governance/ # 算法代码治理（宪章第13章）
│   ├── platform-dispute/         # 申诉争议救济（宪章第15章）
│   ├── platform-early-warning/   # 防异化预警（宪章第16章）
│   ├── platform-federation/      # 地方合作社联盟（宪章第17章）
│   ├── platform-emergency/       # 紧急状态（宪章第18章）
│   └── platform-booth/           # 创立期管理（宪章第21章）
├── CHARTER.md                    # 宪章 v0.2
├── ARCHITECTURE.md               # 技术架构设计
└── settings.gradle               # Gradle 多模块配置
```

## 快速开始

```bash
# 配置 JDK 27
source env.sh

# 编译
./gradlew compileJava

# 运行测试
./gradlew test
```

## 开发规范

提交代码前请阅读 [**CONVENTIONS.md**](CONVENTIONS.md)，核心要点：

- Controller 类上禁写 `@RequestMapping` 路径，每个方法写 API 全路径
- 方法体内禁止全包名，必须 import 短类名
- 注解按行宽从短到长排列
- `@RequiredArgsConstructor` + `final` 替代手写构造器
- null 兜底用 `Optional.ofNullable(x).orElse(d)`
- 字符串判空统一 `StringUtils.hasText()`
- Controller 返回裸对象，不手写 `R.success()`（由 `GlobalResponseAdvice` 自动包装）
- 异步任务统一走 `VirtualThreads`，禁止裸创建线程；全局已启用虚拟线程

## 许可证

AGPL-3.0 — 任何通过网络提供服务的修改版本必须公开源代码。
