# ProRF — 射频链路预算计算平台（Android MVP）

基于 `RF Link预算应用` 设计文件实现的原生安卓 MVP。

## 技术栈
- Kotlin + Jetpack Compose + Material 3
- kotlinx.serialization（链路 JSON 持久化到 filesDir）
- material3-window-size-class（大屏/桌面自适应）
- 无第三方图表库 — 图表 / Sankey 用 Compose Canvas 自绘

## 功能
- **首页**：链路工作流列表（实时计算余量/接收功率/EIRP 摘要卡）、搜索、模板新建、长按删除、升级横幅
- **编辑器**：垂直节点画布（节点卡 + 连接器功率徽章 + 任意位置插入模块）、右侧快速导航圆点、全局参数栏（频率/带宽/距离/温度）、节点属性底部表单（滑杆+数值输入，上移/下移/删除）、底部实时结果栏
- **结果分析**：摘要指标 / 逐级明细表 / SNR-距离曲线图 / Sankey 功率流向图
- **模块市场**：可下载市场（分类筛选+搜索）/ 我已拥有
- **我的**：订阅管理（基础版/Pro 版，按年/按月）、三套主题（浅色/深色/Carbon）、设置项
- **自适应**：手机 = 底部导航；平板/桌面（宽度 ≥ Medium）= 侧边导航栏 + 首页双列 + 编辑器内嵌结果面板

## 构建
```
# 需要 JDK 17+ 与 Android SDK（compileSdk 36）
gradle assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## 结构
```
app/src/main/java/com/prorf/app/
├── MainActivity.kt          # 自适应导航壳（底部栏 / NavigationRail）
├── data/
│   ├── Models.kt            # 节点/链路/计算结果模型
│   ├── Catalog.kt           # 13 个 RF 模块目录 + 示例链路 + 市场/订阅数据
│   ├── RfEngine.kt          # FSPL/噪声底/链路预算计算引擎
│   └── WorkflowStore.kt     # JSON 文件持久化仓库
└── ui/
    ├── theme/Theme.kt       # 三主题设计系统（设计 token 1:1 移植）
    ├── components/Common.kt # 徽章/指标卡/Chips/参数行等
    └── screens/             # Home / Editor / Results / Market / Profile
```
