# ProRF v1.0 Google Play 发布计划

> **目标**：纯免费、中英双语，上架 Google Play。
> **更新日期**：2026-06-12（第十四轮改进完成，累计 121 项修复）

---

## 一、当前状态

**代码已就绪。** 阶段 A（付费 UI 移除）/ B（双语）/ C-extra（14 轮迭代优化）全部完成，可编译运行。

| 模块 | 状态 |
|---|---|
| 字符串资源（EN + ZH，含全部 UI 文案） | ✅ |
| 数据模型（Models / Catalog / WorkflowStore） | ✅ |
| 首页（搜索、筛选、排序、批量操作、收藏、标签） | ✅ |
| 编辑器（节点增删改、撤销/重做、自动保存、参数分区） | ✅ |
| 结果页（摘要/表格/图表/Sankey，带参数标注） | ✅ |
| 模板库（5 个模板：卫星/5G/微波/雷达/空白） | ✅ |
| 设置页（主题三选、语言切换、使用帮助、关于） | ✅ |
| 引导页（OnboardingScreen，仅首次展示） | ✅ |
| RF 计算引擎（FSPL、Friis 级联 NF、温度噪声、IF 带宽） | ✅ |
| 无障碍（contentDescription、触控目标 ≥48dp） | ✅ |
| R8 混淆配置（proguard-rules.pro + kotlinx-serialization keep） | ✅ |

---

## 二、发布前待办（全部手工步骤）

### 🔴 阻塞项（必须完成才能发布）

#### C1 · 签名 Keystore
```bash
keytool -genkeypair -v -keystore prorf-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
- 将 keystore 路径和密码写入 `~/.gradle/gradle.properties`（**绝不入库**）
- `app/build.gradle.kts` 添加 `signingConfigs.release` 并在 `buildTypes.release` 中引用
- Play Console 启用 **Play App Signing**（上传密钥 ≠ 签名密钥）

#### C2 · Release AAB 构建
- [x] 确认 `versionCode = 1`，`versionName = "1.0.0"`
- [ ] `./gradlew bundleRelease` 生成 AAB
- [ ] **R8 回归验证**（⚠️ 最常见翻车点）：用 release 包完整测试序列化读写旧存档、三主题切换、横竖屏旋转

#### C3 · 正式应用图标
- [ ] 设计 512×512 PNG（Play 商店用）
- [ ] 自适应图标：`ic_launcher_foreground.xml`（前景）+ `ic_launcher_background.xml`（背景）
- [ ] 替换 `res/mipmap-*/ic_launcher*`

#### C6 · 多设备回归
- [ ] API 26 / 30 / 36 模拟器（涵盖最低支持版本到最新）
- [ ] 平板横屏（双栏布局自适应）
- [ ] 至少一台真机（冷启动 + 进程被杀后恢复）

---

### 🟡 Play Console 配置（D1–D9）

| # | 任务 | 备注 |
|---|---|---|
| D1 | 创建应用 | 名称 ProRF，默认 en-US，补充 zh-CN 商店列表 |
| D2 | 隐私政策 URL | 零权限零收集，一页静态页（GitHub Pages 即可），中英双语 |
| D3 | 数据安全表单 | 申报「不收集、不共享数据」 |
| D4 | 内容分级 | 工具类 → Everyone |
| D5 | 目标受众 | 18+（避开儿童政策） |
| D6 | 应用类别 | Tools 或 Productivity；填联系邮箱 |
| D7 | 商店素材 | 见下表 |
| D8 | 测试轨道 | 先内测自测；⚠️ 2023-11 后注册个人账号需 12 人 × 14 天封闭测试 |
| D9 | 正式发布 | 分阶段放量 20% → 50% → 100%；盯 Vitals（崩溃 <1.09%，ANR <0.47%） |

**D7 商店素材清单：**

| 素材 | 规格 |
|---|---|
| 图标 | 512×512 PNG |
| Feature Graphic | 1024×500（推荐：深色主题编辑器 + 双语 slogan） |
| 手机截图 ≥4 张 | 1080×2400，中英各一套（首页/编辑器/图表/Sankey） |
| 平板截图 | 7" 和 10" 各 ≥1 张（横屏双栏） |
| 简短描述 | ≤80 字符，中英各一份 |
| 完整描述 | ≤4000 字符，中英各一份（卖点：离线/免费/无广告/零权限） |

---

## 三、排期与风险

```
D1   生成 keystore → 配置签名 → bundleRelease → R8 回归
D2   多设备回归 → 设计图标 → 准备截图和商店文案
D3   Play Console 填写 → 上传内测 AAB → 开始自测
D3–D17  （若个人账号）封闭测试 12 人 × 14 天，期间修复反馈
D18+ 申请正式发布 → 审核 1–7 天 → 分阶段放量
```

**关键风险：**
1. **封闭测试 14 天**是最大排期变量。AAB 一就绪立即在 Console「仪表盘 → 发布正式版的资格」确认账号类型。
2. **R8 + kotlinx-serialization**：必须用 release 包做存档读写回归，debug 包通过不代表 release 包没问题。
3. **商标冲突**：发布前在 Play 搜索确认 "ProRF" 无冲突；备选名 "ProRF — RF Link Budget"。

---

## 四、累计改进统计（14 轮，共 121 项）

| 轮次 | 数量 | 主要方向 |
|---|---|---|
| 第 1 轮 | 16 | UI/UX 基础优化（空状态、触控目标、保存反馈等） |
| 第 2 轮 | 8 | i18n 残留、代码审查缺陷（版本号、性能、撤销/重做） |
| 第 3 轮 | 6 | 用户体验细节（locale 格式化、复制去重、无障碍） |
| 第 4 轮 | 10 | 编译 bug、性能（derivedStateOf）、批量操作 |
| 第 5 轮 | 15 | 引导页、自动保存、标签编辑、导入反馈 |
| 第 6 轮 | 10 | 序列化安全、数据完整性、物理计算（温度噪声）、鲁棒性 |
| 第 7 轮 | 5 | 引导页完善、搜索清除、图表 Y 轴小数 |
| 第 8 轮 | 13 | 错误消息、引导合并、ID 碰撞、i18n 残留、操作反馈 |
| 第 9 轮 | 5 | 编译 bug（import 顺序）、竞争条件、Friis 级联、空状态 |
| 第 10 轮 | 6 | 图表 RX 链计算修正、locale、重命名刷新、后台 IO |
| 第 11 轮 | 4 | EditorSnapshot 文件级、频率显示、接收机 IF 带宽 |
| 第 12 轮 | 4 | 参考参数标注（infoOnly）、NodeCard 预览过滤 |
| 第 14 轮 | 8 | 图表 X 轴刻度、Sankey 参数标注、相对时间、触觉反馈、使用帮助 |
| **合计** | **121** | |

---

## 五、发布后建议优化（非阻塞）

| 项目 | 说明 |
|---|---|
| UX2 | 图表/Sankey 缩放平移手势 |
| FX1 | 更多主题/配色方案 |
| FX2 | 云端备份（Google Drive） |
| FX3 | 编辑器画布缩放 |
| FX4 | 图表导出为图片 |
| 架构 | 引入 ViewModel（当前纯 Composable + 回调，现有规模可接受） |
