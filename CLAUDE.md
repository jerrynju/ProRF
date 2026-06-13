# ProRF 项目架构指导原则

> 来源：ProRF Agent Build Spec v1.0

## 核心目标

构建"工程工作流计算平台内核 + RF领域插件"的可扩展系统，而非单纯的 App。

---

## 系统分层规则（Hard Boundary Rules）

禁止跨层依赖，违反即拒绝合并。

| 层级 | 名称 | 职责 | 禁止 |
|------|------|------|------|
| L0 | Platform Core | Workflow Graph、Node/Port/Edge、Execution Engine、Plugin Registry | RF术语、工程公式、UI逻辑 |
| L1 | Engineering Foundation | Quantity/Unit/Dimension、数值系统、Sweep/Monte Carlo、Validation | — |
| L2 | UI System | Workflow Canvas、Node Card、Inspector、Parameter Editor | 计算逻辑、RF语义 |
| L3 | Domain Pack (ProRF) | RF Node definitions、RF formulas、RF templates | — |
| L4 | App Shell | Android App、Product config、Subscription gating、Theme | — |

**导入规则：**
- `platform/` 不允许 import `domains/`
- `domains/` 只能依赖 `engineering/` + platform API
- `ui/` 不允许访问 execution internals

---

## 仓库结构

```
prorf/
├── platform/         # L0
├── engineering/      # L1
├── ui/               # L2
├── domains/rf/       # L3
├── apps/prorf-android/  # L4
├── services/
├── serialization/
├── build-logic/
```

---

## Agent 运行循环（每次迭代必须遵守）

### Step 1 — 选择目标（ONE TASK ONLY）
- 每次只做一个最小闭环任务
- 禁止一次修改多个子系统

### Step 2 — 分析依赖影响
- 明确哪些模块受影响
- 验证是否违反分层规则

### Step 3 — 最小实现（MVP CODE）
- 先跑起来，再优化
- 不提前抽象、不做未来功能、不写未使用接口

### Step 4 — 自验证（MANDATORY）
- 必须提供单元测试 或 mock workflow execution
- 通过标准：graph 能跑通、execution output 非空、无层级违规

### Step 5 — 提交增量设计记录
- 更新 `docs/decisions/ADR-xxx.md`，记录设计原因、替代方案、风险

---

## Node 系统规范

所有节点必须拆分为四个角色：

```
NodeDefinition  — schema only（静态）
NodeInstance    — only parameters + position（运行时）
NodeExecutor    — pure computation（执行逻辑）
NodeUI          — declarative mapping（可选）
```

---

## RF Domain MVP 节点集（第一阶段必须实现）

- **Source**: SignalSource, NoiseSource
- **Passive**: Attenuator, Cable, Filter
- **Active**: Amplifier (Gain + NF)
- **Channel**: FreeSpacePathLoss
- **Receiver**: Receiver, Sensitivity calculator

---

## 计算引擎规则

必须支持：DAG execution、topological sort、incremental recompute、cached node output

禁止：在 UI 层计算、在 NodeDefinition 内写逻辑

---

## 参数系统规范

所有参数必须携带：`value + unit + dimension`

禁止：naked double、implicit unit conversion、UI字符串解析数值

---

## 工作流文件格式

```json
{
  "schemaVersion": 1,
  "nodes": [],
  "connections": []
}
```

必须支持：版本迁移、可回放执行、可 diff

---

## 商业能力门控

所有功能必须通过：

```kotlin
CapabilityService.has(capabilityId)
```

禁止：`if (isProUser)`

---

## Definition of Done

任何功能提交前必须满足：

- **Functional**: 能在 workflow 中运行
- **Structural**: 不违反分层依赖
- **Computational**: execution output valid
- **Test**: unit test passed
- **Reproducibility**: workflow JSON 可重放

---

## Anti-Patterns（禁止）

- RF logic in UI
- Node contains business logic
- global singleton engine
- hardcoded parameters
- string-based unit handling
- mixed model/UI/entity classes
- premature plugin system overengineering

---

## Milestone Roadmap

| 里程碑 | 内容 |
|--------|------|
| M0 Platform Skeleton | Graph model、Node interface、Execution engine |
| M1 Engineering Foundation | Units system、Quantity model、Sweep engine |
| M2 UI Canvas MVP | Node rendering、inspector、parameter editor |
| M3 RF Domain MVP | 5 core nodes、link budget chain、simple chart |
| M4 Productization | template system、capability gating、persistence |

---

## 最核心原则

> 只做一个最小闭环 → 可运行 → 可测试 → 不破坏架构 → 再扩展

系统增长路径：`correct primitives → compose → scale domains → productize`
