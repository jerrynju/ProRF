# ProRF 发布前人工操作清单

> 代码已就绪，以下步骤全部需要人工完成（无法自动化）。
> 按顺序执行；C1 和 C2 必须在 D 步骤之前完成。

---

## C1 · 生成签名 Keystore（约 5 分钟）

**只做一次，keystore 需永久保存。**

```bash
keytool -genkeypair -v \
  -keystore ~/.keystore/prorf-upload.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias upload \
  -dname "CN=ProRF, OU=, O=, L=, S=, C=CN"
```

然后将以下内容写入 `~/.gradle/gradle.properties`（**不要提交到 git**）：

```properties
prorf_keystore=/Users/你的用户名/.keystore/prorf-upload.jks
prorf_keystore_password=你的密码
prorf_key_alias=upload
prorf_key_password=你的密码
```

> ⚠️ `build.gradle.kts` 中 `signingConfigs.release` 已配置好，会自动读取这些属性，无需改动代码。

**备份 keystore 文件**（U盘 / 云盘 / 密码管理器），丢失后无法更新应用。

---

## C2 · Release AAB 构建与 R8 验证（约 30 分钟）

### 2-a 构建

```bash
cd android
./gradlew bundleRelease
# 产物路径：app/build/outputs/bundle/release/app-release.aab
```

### 2-b R8 回归验证（重点）

使用 release 包在真机或 API 26+ 模拟器上完成以下操作：

- [ ] 创建一个链路（从模板），保存，退出，重新打开 → 数据完整
- [ ] 导出 JSON，再导入 → 字段无丢失
- [ ] 在三个主题（Light / Dark / Carbon）之间切换 → 无崩溃
- [ ] 横竖屏旋转编辑器和结果页 → 不崩溃，数据不丢失
- [ ] 中文 / 英文语言切换 → UI 正确刷新

> 最常见的 R8 翻车：`kotlinx-serialization` 的 `@Serializable` 类在混淆后名称被改变，
> 导致 JSON 读写失败。`proguard-rules.pro` 已包含 keep 规则，但必须用真实 release 包验证。

---

## C3 · 设计正式应用图标（约 1–2 小时）

当前图标已有天线设计（蓝色渐变背景 + 白色天线 SVG），可发布但偏简陋。

### 建议设计方向
- 主色：`#004E90` ~ `#0064A4`（已用作图标背景）
- 元素：抛物面天线 / 信号波纹 / 射频波
- 风格：扁平 Material You，圆角感

### 需要的文件
| 文件 | 用途 |
|---|---|
| `android/app/src/main/res/drawable/ic_launcher_foreground.xml` | 替换现有前景（已有天线设计） |
| `android/app/src/main/res/drawable/ic_launcher_background.xml` | 替换现有背景（已有蓝渐变） |
| 512×512 PNG | Play 商店高清图（不放入代码库，仅上传到 Play Console） |

> 若不重新设计，**现有天线图标可以直接用于发布**，满足 Play 技术要求。

---

## C6 · 多设备回归测试（约 2 小时）

用 Android Studio 的 AVD Manager 创建以下模拟器：

| 设备 | API | 分辨率 | 要测试的点 |
|---|---|---|---|
| Pixel 4 (phone) | 26 (Android 8.0) | 1080×2280 | 最低支持版本；边缘情况多 |
| Pixel 6 (phone) | 33 (Android 13) | 1080×2400 | 按应用语言切换（LocaleManager API） |
| Pixel 7 (phone) | 36 (Android 16) | 1080×2400 | 最新系统，edge-to-edge |
| Pixel Tablet | 34 | 2560×1600 | 横屏双栏布局；NavigationRail 显示 |

**每个设备验证：**
- [ ] 冷启动（首次安装 → OnboardingScreen 显示）
- [ ] 链路创建、编辑、保存、导入、导出全流程
- [ ] 结果页四个 Tab（Summary / Table / Chart / Sankey）
- [ ] 语言切换后界面刷新
- [ ] 进程被杀（Recents 划掉）后重开 → 数据恢复

**真机测试（必须）：**
- [ ] 至少一台 Android 手机（任意 API ≥26）完整走查上述流程

---

## D · Play Console 配置（约 3–4 小时，分两段）

### D-前置：账号确认
打开 [Play Console 仪表盘](https://play.google.com/console) → 「创建应用的资格」  
确认是否需要 12 人 × 14 天封闭测试。**今天确认，若需要立即招募测试者（建议招募 ≥15 人留余量）。**

### D1 创建应用
- 名称：**ProRF**（若冲突备选 "ProRF — RF Link Budget"）
- 默认语言：English (en-US)
- 应用类型：App；类别：Tools（或 Productivity）

### D2 隐私政策（硬性要求）

内容模板（可托管在 GitHub Pages）：

```markdown
# Privacy Policy — ProRF

ProRF collects no user data. The app operates entirely offline.
- No internet permissions are requested.
- No analytics, crash reporting, or advertising SDKs are included.
- All data (link budgets) is stored locally on your device.
- Nothing is transmitted to any server.

Last updated: 2026-06-12
Contact: jerrychen19920402@gmail.com
```

中文版：
```markdown
# 隐私政策 — ProRF

ProRF 不收集任何用户数据，完全离线运行。
- 未申请任何网络权限
- 不含分析、崩溃上报或广告 SDK
- 所有数据（链路预算）仅保存在本地设备
- 不向任何服务器传输任何信息

最后更新：2026-06-12
联系：jerrychen19920402@gmail.com
```

### D3 数据安全表单
- 不收集数据：✓
- 不共享数据：✓
- 数据加密传输：不适用（不传输）
- 用户可请求删除数据：不适用（无账号体系）

### D4–D6 快速填写
| 字段 | 填写内容 |
|---|---|
| 内容分级 | 工具类 → Everyone |
| 目标受众 | 18+（避开儿童政策审查） |
| 应用类别 | Tools |
| 联系邮箱 | jerrychen19920402@gmail.com |

### D7 商店文案

**英文简短描述（≤80 字符）：**
```
RF link budget calculator with visual node editor & real-time analysis
```

**英文完整描述（建议内容）：**
```
ProRF is a professional RF link budget calculator for engineers and students.

FEATURES
• Visual node editor — chain TX → propagation → RX blocks
• Real-time results — margin, SNR, EIRP, sensitivity, noise floor
• 5 built-in templates — Satellite, 5G, Microwave, Radar, Blank
• Friis cascade noise figure calculation
• Distance-vs-SNR chart with logarithmic X axis
• Sankey power flow diagram
• Full undo/redo history
• Import/export as JSON

PRIVACY
• 100% offline — no internet required
• Zero permissions
• No ads, no analytics, no account
• Your data never leaves your device

Available in English and Chinese (Simplified).
```

**中文简短描述（≤80 字符）：**
```
射频链路预算计算器，可视化节点编辑，实时计算余量与SNR
```

**中文完整描述：**
```
ProRF 是面向射频工程师和学生的专业链路预算计算器。

功能特性
• 可视化节点编辑器 — 按 TX → 传播 → RX 顺序拖放模块
• 实时计算结果 — 余量、SNR、EIRP、灵敏度、噪声基底
• 5 个内置模板 — 卫星、5G、微波、雷达、空白
• Friis 级联噪声系数计算
• 距离-SNR 曲线图（对数 X 轴）
• 桑基功率流图
• 完整撤销/重做历史
• JSON 导入/导出

隐私保护
• 100% 离线，无需联网
• 零权限申请
• 无广告、无分析、无账号
• 数据仅保存在本地，绝不上传

支持英文与简体中文界面。
```

### D7 截图要求（1080×2400，手机）

至少拍以下 4 张（中英各一套 = 8 张）：
1. HomeScreen — 显示 3 个链路卡片（深色主题更美观）
2. EditorScreen — 打开 5G 模板，展开节点参数面板
3. ResultsScreen Chart Tab — 显示 SNR/余量曲线
4. ResultsScreen Sankey Tab — 显示功率流图

### D8 测试轨道
1. 上传 AAB 到**内部测试**轨道（自动生效，可立即自测）
2. 确认封闭测试要求后，创建**封闭测试**轨道并邀请 ≥12 名测试者
3. 14 天后申请**正式发布**

### D9 正式发布
- 分阶段放量：20% → 7 天观察 → 50% → 100%
- 盯 Android Vitals：崩溃率 < 1.09%，ANR < 0.47%

---

## 完成后状态确认

所有步骤完成后，在此打勾：

- [ ] C1：keystore 已生成，gradle.properties 已配置，备份已保存
- [ ] C2：`bundleRelease` 成功，R8 回归全部通过
- [ ] C3：图标已确认（现有或重新设计）
- [ ] C6：3 个模拟器 + 1 台真机全部通过
- [ ] D：Play Console 已配置，AAB 已上传内测轨道
- [ ] （若个人账号）封闭测试 14 天完成
- [ ] 正式发布申请已提交
