# 🛠️ 开发者指南 (Developer Guide)

面向本地记账 (LocalBill Recording) 项目的开发者文档，涵盖架构设计、工程结构、开发环境搭建、代码贡献规范与测试说明。

> 面向终端用户，请阅读 [README.md](README.md)；版本发布说明请阅读 [RELEASE_NOTES.md](RELEASE_NOTES.md)。

---

## 📋 目录

1. [项目概览](#1-项目概览)
2. [技术栈与版本](#2-技术栈与版本)
3. [工程目录结构](#3-工程目录结构)
4. [分层架构详解](#4-分层架构详解)
5. [核心领域模型](#5-核心领域模型)
6. [响应式架构 (Reactive UDF)](#6-响应式架构-reactive-udf)
7. [开发环境搭建](#7-开发环境搭建)
8. [构建与运行](#8-构建与运行)
9. [测试体系](#9-测试体系)
10. [代码贡献规范](#10-代码贡献规范)
11. [常见开发任务](#11-常见开发任务)
12. [性能与体积优化](#12-性能与体积优化)

---

## 1. 项目概览

**本地记账 (LocalBill Recording)** 是一款纯本地离线的 Android 记账应用，采用 **MVVM + Jetpack Compose (Material 3) + Room + Kotlin Coroutines** 架构。

核心设计目标：

* **隐私优先**：零网络权限，所有财务数据仅存储于本地 SQLite 数据库。
* **极简美学**：高质感马卡龙配色 + 矢量图标，Canvas 原生绘制图表。
* **极致性能**：120Hz 跟手触感键盘、毫秒级触觉反馈、Release 版 R8 深度压缩至 6.8 MB。
* **数据自主**：支持 JSON 全量备份/还原、CSV 导出、手机桌面小组件。

---

## 2. 技术栈与版本

| 类别 | 技术 | 版本 |
| :--- | :--- | :--- |
| **语言** | Kotlin | 2.0.21 |
| **构建工具** | Android Gradle Plugin (AGP) | 8.7.3 |
| **UI 框架** | Jetpack Compose | BOM 2024.11.00 |
| **Material** | Material 3 + Icons Extended | 1.7.6 |
| **持久化** | Room (SQLite) + KSP | 2.6.1 |
| **并发/响应式** | Kotlin Coroutines | 1.9.0 |
| **导航** | Navigation Compose | 2.8.5 |
| **JSON** | Gson | 2.11.0 |
| **测试** | JUnit 4 / Robolectric / Mockito | 4.14.1 / 5.14.2 |
| **JVM 目标** | Java 21 | — |
| **代码压缩** | R8 (Release 全模式) | — |

**关键版本矩阵**：

* Min SDK: **26** (Android 8.0+)
* Target SDK / Compile SDK: **35**
* 最低支持 Android Studio：Ladybug / Koala 或更高版本

---

## 3. 工程目录结构

```
bill-recording/
├── build.gradle.kts              # 根构建脚本
├── settings.gradle.kts           # 模块声明 (include :app)
├── gradle.properties             # 全局 Gradle 配置
├── gradle/
│   ├── wrapper/                  # Gradle Wrapper
│   └── libs.versions.toml        # 版本目录 (version catalog)
├── app/
│   ├── build.gradle.kts          # 应用模块构建脚本
│   ├── proguard-rules.pro        # 混淆规则
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # 清单 (Activity / Widget / Service)
│       │   ├── java/com/localbill/recording/
│       │   │   ├── BillApplication.kt        # Application (初始化 DB / Repos)
│       │   │   ├── MainActivity.kt           # 入口 Activity (装配 ViewModels)
│       │   │   ├── data/                     # 数据层
│       │   │   │   ├── entity/              # Room 实体 (Record / Category)
│       │   │   │   ├── dao/                # 数据访问对象
│       │   │   │   ├── model/              # 纯函数计算模型
│       │   │   │   └── repository/          # 业务仓库 (Repository)
│       │   │   ├── ui/                     # 界面层
│       │   │   │   ├── components/         # 可复用 UI 组件 (图表 / 键盘)
│       │   │   │   ├── navigation/         # 导航图
│       │   │   │   ├── screens/           # 页面 (Home / Stats / Category / Backup)
│       │   │   │   ├── theme/             # 主题 (Color / Type / Theme)
│       │   │   │   └── viewmodel/         # ViewModel (状态流)
│       │   │   ├── util/                   # 工具类 (格式化 / CSV / 日期)
│       │   │   └── widget/                # 桌面小组件 (Provider / ViewsService)
│       │   └── res/                       # 资源 (drawable / mipmap / values / xml)
│       └── test/
│           └── java/com/localbill/recording/
│               ├── RoomDatabaseTest.kt
│               ├── CategoryAggregationTest.kt
│               ├── DateTimeUtilsTest.kt
│               ├── CalculatorEvaluationTest.kt
│               └── BackupRestoreTest.kt
```

**包名命名规范**：`com.localbill.recording.<layer>`，其中 `<layer>` 为 `data` / `ui` / `util` / `widget`。

---

## 4. 分层架构详解

应用采用经典三层架构，数据流向自下而上：

```
UI 层 (Compose Screen)
   │  StateFlow / Event
   ▼
ViewModel 层 (MVVM ViewModel + Factory)
   │  暴露 suspend fun / StateFlow
   ▼
Repository 层 (业务逻辑 / 纯函数计算)
   ├── Room DAO (SQL 访问)
   └── 本地文件 (JSON 备份 / CSV 导出)
   │
   ▼
Entity / Model (Room 实体 + 领域模型)
```

### 4.1 UI 层 (`ui/`)

* **`screens/`**：各页面实现（`HomeScreen`、`StatisticsScreen`、`CategoryManagementScreen`、`BackupScreen`）。
* **`components/`**：可复用 UI 组件：
  * `BezierTrendChart` — 贝塞尔平滑渐变曲线图（Canvas 原生绘制）。
  * `DonutPieChart` — 现代多层环形占比图（带中心孔洞统计）。
  * `CalculatorBottomSheet` — 120Hz 触感计算键盘（底部抽屉）。
  * `AnimatedAmountText` — 金额动画文本。
  * `CategoryIconBadge` — 马卡龙色彩分类徽章。
  * `MinimalCalendarSheet` — 极简日历选择器。
* **`navigation/`**：`MainAppNavigation` — 底部导航 (Home / 统计 / 分类管理)。
* **`theme/`**：`Color`（马卡龙配色）、`Type`（字体）、`Theme`（主题入口）。
* **`viewmodel/`**：各页面 ViewModel，使用 `@Immutable` data class 暴露 `StateFlow<HomeUiState>`。

### 4.2 ViewModel 层 (`ui/viewmodel/`)

* 继承 `ViewModel`，使用 `viewModelScope` + `launch` 处理副作用（写库、删除）。
* 使用 `combine` 组合多个 Repository 的 Flow，在 `flowOn(Dispatchers.Default)` 后聚合，`stateIn(SharingStarted.Eagerly)` 暴露统一状态。
* 提供内部 `Factory : ViewModelProvider.Factory`，在 `MainActivity` 中通过 `by viewModels { ... }` 注入 Repository。

### 4.3 Repository 层 (`data/repository/`)

* `RecordRepository`：记录增删改查、趋势点计算、分类聚合、恩格尔系数计算（均为纯函数）。
* `CategoryRepository`：两级分类的主从查询、聚合。
* `BackupRepository`：JSON 全量备份与还原。

### 4.4 Data 层 (`data/`)

* **`entity/`**：Room 实体（`@Entity` 注解）。`RecordEntity`、`CategoryEntity` 含外键约束、索引。
* **`dao/`**：`RecordDao`、`CategoryDao`，暴露 `Flow`（响应式查询）与 `suspend fun`。
* **`model/`**：纯函数计算模型（`BackupModels`、`StatisticsModels`、`PeriodSummary`、`EngelCoefficient` 等）。

### 4.5 util / widget

* **`util/`**：`AmountFormatter`（金额/百分比格式化）、`CsvExporter`（UTF-8 BOM CSV）、`DateTimeUtils`（日/周/月计算、格式化）。
* **`widget/`**：桌面小组件（`BillWidgetProvider` 4x2、`BillWidgetProviderLarge` 4x3、`BillWidgetViewsService` 远程视图、`BillWidgetRefreshReceiver`、`BillWidgetData`）。支持 `OPEN_APP` / `ADD_RECORD` 快捷指令。

---

## 5. 核心领域模型

### 5.1 两级分类体系 (Two-Level Categories)

`CategoryEntity` 通过 `parentId` 实现两级分类：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (自增 PK) | 主键 |
| `name` | String | 分类名称 |
| `iconName` | String | Material Icon 名 |
| `colorHex` | Long | ARGB 颜色数值，如 `0xFF22C55E` |
| `parentId` | Long? | `null` 为主分类，非 `null` 为子分类 |
| `isBuiltIn` | Boolean | 是否内置 |
| `sortOrder` | Int | 排序 |

**内置默认分类**：

* 📘 学习（主分类）
* 🍱 饮食（主分类，下辖子分类：🥣 食堂、🛵 外卖、🍽️ 外出、🍎 水果）
* 🚗 交通
* 👗 衣物

### 5.2 记账明细 (Record)

`RecordEntity`：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | Long (自增 PK) | 主键 |
| `amount` | Double | 金额 |
| `categoryId` | Long | 所属主分类 |
| `subCategoryId` | Long? | 所属子分类（可为空） |
| `note` | String | 备注 |
| `timestamp` | Long | 时间戳（毫秒） |
| `imagePath` | String? | 图片路径（可为空） |
| `createdAt` | Long | 创建时间 |

`RecordWithCategory`：关联分类信息的查询结果 DTO。

### 5.3 统计与聚合模型 (`data/model/`)

* `CategoryAggregation` — 主分类汇总（总额 / 笔数 / 占比 + 子分类拆解）。
* `SubCategoryAggregation` — 子分类明细（含"直接支出"伪分类）。
* `TrendPoint` — 趋势图数据点（按 日/周/月 维度）。
* `PeriodSummary` — 周期概览（总额 / 日均 / 笔数 / 最高单笔 / 恩格尔系数）。
* `EngelCoefficient` — 恩格尔系数（食品支出 / 总支出）与等级标签。
* `BackupModels` — JSON 备份反序列化模型。

---

## 6. 响应式架构 (Reactive UDF)

应用使用 **Combine + StateFlow** 的响应式数据流（Reactive UDF）：

1. **数据源**：`RecordDao` / `CategoryDao` 通过 Room 暴露 `Flow`。
2. **组合**：ViewModel 中用 `combine(records, categories, selectedDate)` 组合多个数据源。
3. **计算**：在 `flowOn(Dispatchers.Default)` 后执行聚合（日/周/月统计、分类聚合等纯函数）。
4. **共享**：`stateIn(SharingStarted.Eagerly, initialValue = ...)` 将计算结果缓存并暴露给 UI。
5. **副作用**：写库/删除等副作用在 `viewModelScope.launch(Dispatchers.IO)` 中执行。

关键示例（`HomeViewModel`）：

```kotlin
val uiState: StateFlow<HomeUiState> = combine(
    recordRepository.allRecordsFlow,
    categoryRepository.mainCategoriesFlow,
    categoryRepository.allCategoriesFlow,
    _selectedDate
) { records, mainCategories, allCategories, selectedDate ->
    // 在此计算今日/本周/本月总额、分组、选中日期详情等
    HomeUiState(...)
}.flowOn(Dispatchers.Default)
 .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = HomeUiState())
```

---

## 7. 开发环境搭建

### 7.1 硬件/软件要求

| 项目 | 要求 |
| :--- | :--- |
| OS | Windows / macOS / Linux |
| JDK | JDK 21 |
| Android Studio | Ladybug / Koala 或更高版本 |
| Android SDK | Platform 35 (Min SDK 26) |

### 7.2 安装步骤

1. 安装 **JDK 21**，并确保 `JAVA_HOME` 指向 JDK 21 安装目录。
2. 安装 **Android Studio**，打开后选择 *Empty Views* 项目或 *Import* 现有项目。
3. 在 *SDK Command Line Tools* 中安装 **Android SDK Platform 35**。
4. 打开项目，IDE 会自动配置 SDK 与 Gradle（若使用 Wrapper 则无需单独配置 Gradle）。
5. 连接模拟器（AVD）或选择真机进行调试。

> **`local.properties`**：包含 `sdk.dir`，通常由 IDE 自动生成，勿提交到版本库。

---

## 8. 构建与运行

### 8.1 命令行构建

```bash
# 执行单元测试
./gradlew testDebugUnitTest

# 编译 Debug APK（不混淆）
./gradlew assembleDebug

# 编译 Release APK（启用 R8 全模式 + 资源压缩）
./gradlew assembleRelease
```

### 8.2 在 Android Studio 中构建

* **Run** → 选择模拟器或真机设备 → 点击 ▶️ 运行。
* **Build → Build APK(s)**：生成 Debug APK。
* **Build → Generate Signed Bundle / APK**：生成带签名的发布版本。

### 8.3 Release 构建配置

`app/build.gradle.kts` 中 `release` buildType：

```kotlin
release {
    isMinifyEnabled = true       // 启用 R8 代码压缩
    isShrinkResources = true     // 压缩资源
    signingConfig = signingConfigs.getByName("debug")
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

---

## 9. 测试体系

测试遵循测试驱动与自动化质量保障流程，测试套件 **100% 通过**。

### 9.1 测试清单

| 测试套件 | 覆盖点 |
| :--- | :--- |
| `RoomDatabaseTest` | In-Memory SQLite、内置分类初始化、两级关联查询、外键级联安全 |
| `CategoryAggregationTest` | 两级分类自动聚合、子类拆解、百分比精确度 |
| `DateTimeUtilsTest` | 日/周/月跨度、自然周切分、中文日期格式化 |
| `CalculatorEvaluationTest` | 触感键盘连续加减运算、浮点精度（保留2位）、边界容错 |
| `BackupRestoreTest` | JSON 全量导出、反序列化恢复、数据一致性校验 |

### 9.2 测试运行

```bash
# 运行单元测试
./gradlew testDebugUnitTest

# 仅运行指定测试类
./gradlew testDebugUnitTest --tests "*CategoryAggregationTest*"
```

### 9.3 测试依赖

* JUnit 4、Robolectric（Android 环境模拟）、Mockito + Mockito Kotlin（依赖注入模拟）。
* `androidx.room.testing`（In-Memory SQLite）、`kotlinx-coroutines-test`（虚拟时间）。

---

## 10. 代码贡献规范

### 10.1 编码规范

* **语言**：Kotlin 2.0+，遵循 Kotlin 官方代码风格（`kotlin.code.style=official`）。
* **ViewModel**：UI 状态用 `@Immutable` data class 暴露 `StateFlow`，避免暴露可变状态。
* **副作用**：所有写库/删除操作使用 `viewModelScope.launch(Dispatchers.IO)`，禁止在 UI 线程执行耗时操作。
* **纯函数**：统计/聚合/格式化等计算逻辑应实现为纯函数，便于单测验证。
* **注释**：关键类与复杂算法添加 KDoc 说明（中文注释）。

### 10.2 提交规范

1. 在功能分支开发，遵循 Git Flow。
2. 提交信息采用 `type: subject` 格式（如 `feat: 新增月度视图切换`、`fix: 修复恩格尔系数边界`）。
3. 新增/修改功能需配套单元测试。
4. 更新 `README.md` / `RELEASE_NOTES.md` / `CHANGELOG.md`。
5. 提交前确认 `local.properties`、`.build_tmp/`、`build/` 等构建产物未被误提交。

### 10.3 开源协议

项目采用 [MIT License](LICENSE)。（Copyright 2026 yugusu704-lang）

---

## 11. 常见开发任务

### 11.1 新增一个自定义分类

1. 在 `CategoryRepository` 中新增 `suspend fun addCustomCategory(...)`。
2. 在 `CategoryManagementScreen` 中添加对应 UI 与 ViewModel 调用。
3. 在 `CategoryIconBadge` / 主题中处理新分类的图标与配色。

### 11.2 新增一个统计维度

1. 在 `StatisticsModels` 中新增领域模型。
2. 在 `RecordRepository` 中新增纯函数计算逻辑。
3. 在对应 ViewModel 中组合并暴露到 UI。
4. 在 `StatisticsScreen` 中渲染，并补充单元测试。

### 11.3 新增一个界面

1. 在 `ui/screens/` 下新建页面。
2. 在 `ui/viewmodel/` 下新建 ViewModel（含 Factory）。
3. 在 `ui/navigation/Navigation.kt` 中注册导航。
4. 在 `AndroidManifest.xml` / `MainActivity` 中装配（如需要新 Activity）。

---

## 12. 性能与体积优化

* **R8 全模式**：Release 构建启用 `isMinifyEnabled = true` + `isShrinkResources = true`，APK 体积仅 6.8 MB。
* **Canvas 原生图表**：趋势图与环形图使用 Canvas 原生绘制，避免引入第三方图表库。
* **Coroutines + Flow**：异步 I/O 与响应式数据流避免 UI 卡顿，120Hz 键盘提供跟手体验。
* **Haptic Feedback**：触觉反馈经毫秒级优化，无粘滞感。
* **In-Memory 数据库测试**：单元测试使用 In-Memory SQLite，确保测试快速可靠。

---

## 附：快速对照表

| 关注点 | 参考文档 |
| :--- | :--- |
| 面向用户的功能说明 | [README.md](README.md) |
| 版本发布说明 | [RELEASE_NOTES.md](RELEASE_NOTES.md) |
| 开发/构建/测试/贡献 | 本文档（DEVELOPER_GUIDE.md） |
| 开源协议 | [LICENSE](LICENSE) |
