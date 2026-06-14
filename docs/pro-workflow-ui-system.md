---

# 📘 Pro Workflow UI System v1.0（正式文档版）

---

# 1. 文档定位

## 1.1 文档性质

本文档定义：

> 一个面向模块化工程计算系统的通用 UI 操作系统规范

适用于：

* RF / DSP / 通信系统
* 控制系统 / 仿真系统
* AI Workflow / LLM Pipeline
* 工程计算与建模工具

---

## 1.2 系统边界

本 UI 系统：

* ❌ 不绑定任何行业语义
* ❌ 不绑定具体公式
* ❌ 不绑定业务逻辑
* ❌ 不负责计算实现

只定义：

> 计算图（Graph）的可视化表达体系

---

# 2. 核心设计哲学

---

## 2.1 UI 本体定义

```text
UI = Workflow Graph 的可视化投影
```

系统本质：

```text
Workflow (DAG)
    → Execution Engine
    → UI Projection Layer
```

---

## 2.2 三大原则

### P1：结构优先（Structure First）

UI优先表达：

* 节点关系
* 数据流
* 计算依赖

而不是功能按钮。

---

### P2：图即真相（Graph as Source of Truth）

唯一数据源：

```text
Workflow Graph
```

UI只是渲染结果。

---

### P3：Schema驱动一切（Schema Driven UI）

所有 UI 元素来源：

* Node Schema
* Port Schema
* Parameter Schema
* Execution Schema

---

# 3. 系统总体架构

---

```text
Pro Workflow UI System
│
├── Navigation Layer
├── Workspace Layer
├── Workflow Layer
├── Node System Layer
├── Edge System Layer
├── Inspector Layer
├── Execution Layer
├── Library Layer
├── Visualization Layer
└── Design System Layer
```

---

# 4. Navigation Layer（导航层）

---

## 4.1 职责

提供系统入口结构，不涉及业务语义。

---

## 4.2 页面结构

```text
Home
Workspace
Workflow Editor
Library
Analysis
Settings
```

---

## 4.3 设计规则

* 不展示计算细节
* 不绑定领域类型
* 只表达系统结构

---

# 5. Workspace Layer（工程空间）

---

## 5.1 定义

Workspace 是工程项目容器：

```text
Workspace = {
  workflows[],
  scenarios[],
  datasets[],
  results[],
  versions[]
}
```

---

## 5.2 UI结构

* 左侧：项目树
* 中间：Workflow Tabs
* 右侧：Global Inspector

---

## 5.3 核心能力

* 多 workflow 管理
* scenario 参数集
* experiment tracking
* version control

---

# 6. Workflow Layer（核心画布系统）

---

## 6.1 定义

```text
Workflow = Directed Acyclic Graph (DAG)
```

---

## 6.2 Canvas结构

```text
Layer 1: Grid Background
Layer 2: Edge Layer
Layer 3: Node Layer
Layer 4: Execution Overlay
```

---

## 6.3 Canvas规则

* 无限画布
* 可缩放语义层级
* 节点自动吸附
* 支持子图折叠

---

## 6.4 交互模型

* drag / connect
* multi-select
* pan / zoom
* collapse graph

---

# 7. Node System（节点系统）

---

## 7.1 Node定义

```text
Node = Computational Unit
```

---

## 7.2 标准结构

```text
┌────────────────────┐
│ Header             │
├────────────────────┤
│ Parameter Summary  │
├────────────────────┤
│ Input Ports        │
│ Output Ports       │
├────────────────────┤
│ Result Preview     │
└────────────────────┘
```

---

## 7.3 Node三层模型

### ① Definition

* schema
* ports
* parameters

### ② Instance

* value
* position
* state

### ③ Execution

* inputs
* outputs
* diagnostics

---

## 7.4 多端口模型

支持：

* 多输入
* 多输出
* fan-in / fan-out
* broadcast

---

## 7.5 Node状态

| 状态      | 含义  |
| ------- | --- |
| idle    | 未执行 |
| running | 执行中 |
| success | 成功  |
| warning | 警告  |
| error   | 错误  |
| cached  | 缓存  |

---

# 8. Edge System（连接系统）

---

## 8.1 Edge定义

```text
Edge = Typed Data Connection
```

---

## 8.2 Edge结构

```text
Edge = {
  source_port,
  target_port,
  data_type,
  unit,
  validation_rules
}
```

---

## 8.3 Edge类型

* data flow（主线）
* dependency（虚线）
* control flow
* feedback loop

---

## 8.4 视觉规则

| 类型         | 颜色     |
| ---------- | ------ |
| data       | blue   |
| dependency | gray   |
| error      | red    |
| cache      | purple |

---

# 9. Inspector Layer（属性系统）

---

## 9.1 定义

Inspector 是唯一编辑入口。

---

## 9.2 结构

```text
Tabs:
- Parameters
- Inputs
- Outputs
- Diagnostics
- Charts
```

---

## 9.3 参数系统

参数必须具备：

```text
Value + Unit + Dimension
```

支持：

* 数值输入
* 单位切换
* 表达式输入
* sweep range

---

# 10. Execution Layer（执行系统）

---

## 10.1 执行模型

```text
DAG Execution Pipeline
```

流程：

```text
Validate → Topological Sort → Execute → Cache → Propagate
```

---

## 10.2 增量计算

```text
dirty node → downstream recompute
```

---

## 10.3 Execution UI

* 节点高亮流动
* edge 数值变化
* cache 命中标记
* error propagation

---

# 11. Library Layer（节点库）

---

## 11.1 定义

Library = 可组合计算模块系统

---

## 11.2 分类

* Source Nodes
* Processing Nodes
* Channel Nodes
* Receiver Nodes
* Analysis Nodes

---

## 11.3 UI结构

* category → card grid → drag into canvas

---

# 12. Visualization Layer（可视化系统）

---

## 12.1 图表类型

* time series
* waterfall
* gain chain
* SNR curve
* frequency response

---

## 12.2 原则

* 工程语义优先
* 数据解释优先
* 不做娱乐图表

---

# 13. Design System（视觉体系）

---

## 13.1 设计哲学

> 精密工程感 > 装饰性 UI

---

## 13.2 Light Theme 色彩系统

### Primary

* Blue: #2F80ED
* Green: #27AE60

---

### Semantic Colors

| 类型      | 颜色     |
| ------- | ------ |
| success | green  |
| warning | orange |
| error   | red    |
| info    | blue   |

---

## 13.3 布局原则

* 左：结构
* 中：Graph
* 右：Inspector
* 底：状态栏

---

## 13.4 Node视觉

* 白底卡片
* 微阴影
* 彩色边框表示类型
* 数值高亮

---

## 13.5 Edge视觉

* gradient line
* direction arrow
* running animation

---

# 14. 跨领域统一抽象

---

```text
Node = Function
Edge = Data Flow
Workflow = Graph
Scenario = Parameter Set
Result = Execution Output
```

---

适配领域：

* RF
* DSP
* Control
* AI pipeline
* Simulation

---

# 15. 系统级约束（强制规则）

---

## R1 UI无状态原则

UI 不存业务状态

---

## R2 Graph唯一真相

所有数据必须来源 Graph

---

## R3 Schema驱动

所有UI必须可由 schema 生成

---

## R4 Execution隔离

UI 不参与计算逻辑

---

## R5 领域插件隔离

UI不绑定任何 domain

---

# 16. 系统总结

```text
Pro Workflow UI System =
    Visual Programming OS
    for Engineering Computation
```

---

# 如果你下一步要继续推进，我建议直接进入这三件事：

### 🚀 1. 把这个 UI 系统拆成 Jetpack Compose 组件库（可开发）

### 🚀 2. 定义 Node JSON Schema（AI可生成节点）

### 🚀 3. 定义 Execution Engine v1（可跑 DAG）

只要说一句：

> “进入可开发架构阶段”

我可以直接把它变成 **工程级代码结构 + Gradle模块拆分方案 + 第一批GitHub issues**。
