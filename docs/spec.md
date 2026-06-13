# ProRF Agent Build Spec v1.0

## 0. 目标（Mission）

构建一个最小可运行版本 ProRF（射频链路工作流 App），要求：

- 可视化工作流（Graph-based RF chain）
- 节点化射频系统建模
- 可执行链路预算计算
- 支持参数编辑 + 单位系统
- 支持模板化生成
- 架构可扩展为多专业工程平台

最终目标不是 App，而是：

> 一个"工程工作流计算平台内核 + RF领域插件"的可扩展系统

------------------------------------------------------------------------

## 1. 系统分层原则（Hard Boundary Rules）

系统必须严格分层，禁止跨层依赖：

### L0 Platform Core（不可包含任何 RF 概念）

职责：

- Workflow Graph
- Node / Port / Edge
- Execution Engine
- Diff / Cache / Incremental compute
- Plugin registry

禁止：

- RF术语（gain/noise/link budget）
- 工程公式
- UI逻辑

------------------------------------------------------------------------

### L1 Engineering Foundation

职责：

- Quantity / Unit / Dimension
- 数值系统（linear / log）
- Sweep / Monte Carlo
- Validation engine

------------------------------------------------------------------------

### L2 UI System

职责：

- Workflow Canvas
- Node Card UI
- Inspector
- Parameter Editor

禁止：

- 计算逻辑
- RF语义

------------------------------------------------------------------------

### L3 Domain Pack（ProRF）

职责：

- RF Node definitions
- RF formulas
- RF templates
- RF visualization

------------------------------------------------------------------------

### L4 App Shell

职责：

- Android App
- Product config
- Subscription gating
- Theme

------------------------------------------------------------------------

## 2. 仓库结构（必须遵守）

    prorf/
    ├── platform/
    ├── engineering/
    ├── ui/
    ├── domains/rf/
    ├── apps/prorf-android/
    ├── services/
    ├── serialization/
    ├── build-logic/

规则：

- Platform 不允许 import domains
- domains 只能依赖 engineering + platform API
- UI 不允许访问 execution internals

------------------------------------------------------------------------

## 3. Agent 运行循环（核心机制）

每一次智能体迭代必须遵循：

### Step 1: 选择目标（ONE TASK ONLY）

例如：

- 新增节点
- 修复执行器
- 抽象参数系统
- 改 UI inspector

禁止：

- 一次修改多个子系统

------------------------------------------------------------------------

### Step 2: 分析依赖影响

必须明确：

- 哪些模块会被影响
- 是否违反分层规则
- 是否需要新增抽象

------------------------------------------------------------------------

### Step 3: 最小实现（MVP CODE）

原则： \> 先跑起来，再设计完美

要求：

- 不提前抽象过度
- 不做未来功能
- 不写未使用接口

------------------------------------------------------------------------

### Step 4: 自验证（MANDATORY）

必须生成：

- 单元测试
- 或 mock workflow execution
- 或 node graph execution test

通过标准：

- graph 能跑通
- execution output 非空
- 无层级违规依赖

------------------------------------------------------------------------

### Step 5: 提交增量设计记录

必须更新：

    docs/decisions/ADR-xxx.md

记录：

- 为什么这么设计
- 替代方案
- 风险

------------------------------------------------------------------------

## 4. Node 系统规范（核心数据模型）

所有节点必须符合：

    NodeDefinition（静态）
    NodeInstance（运行时）
    NodeExecutor（执行逻辑）
    NodeUI (optional)

### 强制规则：

- NodeDefinition = schema only
- Executor = pure computation
- UI = declarative mapping
- Instance = only parameters + position

------------------------------------------------------------------------

## 5. RF Domain 第一阶段节点集（MVP）

必须优先实现以下节点：

### Source

- SignalSource
- NoiseSource

### Passive

- Attenuator
- Cable
- Filter

### Active

- Amplifier (Gain + NF)

### Channel

- FreeSpacePathLoss

### Receiver

- Receiver
- Sensitivity calculator

------------------------------------------------------------------------

## 6. 计算引擎规则（Execution Engine）

### 必须支持：

- DAG execution
- topological sort
- incremental recompute
- cached node output

### 禁止：

- 在 UI 层计算
- 在 NodeDefinition 内写逻辑

------------------------------------------------------------------------

## 7. 参数系统规范（Critical）

所有参数必须：

    value + unit + dimension

禁止：

- naked double
- implicit unit conversion
- UI字符串解析数值

------------------------------------------------------------------------

## 8. 工作流文件格式（必须稳定）

    {
      "schemaVersion": 1,
      "nodes": [],
      "connections": []
    }

要求：

- 必须支持版本迁移
- 必须可回放执行
- 必须可diff

------------------------------------------------------------------------

## 9. UI 构建规则

Node Card 必须包含：

- 标题
- 参数摘要
- 状态
- 输出摘要

Inspector 必须包含：

- inputs
- parameters
- outputs
- diagnostics
- charts

禁止：

- UI 直接调用 domain logic
- UI 保存业务状态

------------------------------------------------------------------------

## 10. Capability 商业模型（必须预留）

系统所有功能必须通过：

    CapabilityService.has(capabilityId)

禁止：

    if (isProUser)

------------------------------------------------------------------------

## 11. Definition of Done（完成标准）

任何功能必须满足：

### Functional

- 能在 workflow 中运行

### Structural

- 不违反分层依赖

### Computational

- execution output valid

### Test

- unit test passed

### Reproducibility

- workflow JSON 可重放

------------------------------------------------------------------------

## 12. Anti-Patterns（必须避免）

❌ RF logic in UI\
❌ Node contains business logic\
❌ global singleton engine\
❌ hardcoded parameters\
❌ string-based unit handling\
❌ mixed model/UI/entity classes\
❌ premature plugin system overengineering

------------------------------------------------------------------------

## 13. Milestone Roadmap（自动化执行顺序）

### M0: Platform Skeleton

- Graph model
- Node interface
- Execution engine

### M1: Engineering Foundation

- Units system
- Quantity model
- Sweep engine

### M2: UI Canvas MVP

- Node rendering
- inspector
- parameter editor

### M3: RF Domain MVP

- 5 core nodes
- link budget chain
- simple chart output

### M4: Productization

- template system
- capability gating
- persistence

------------------------------------------------------------------------

## 14. Agent Instruction Summary（最重要）

智能体每次工作必须遵守：

> 只做一个最小闭环 → 可运行 → 可测试 → 不破坏架构 → 再扩展

系统的增长方式：

    correct primitives → compose → scale domains → productize

而不是：

    big architecture → incomplete implementation → chaos

------------------------------------------------------------------------

END
