# 大宋百商图 - Android 技术方案

---

## 一、项目概述与技术选型

### 1.1 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| 语言 | Kotlin 100% | 现代 Android 首选 |
| UI 框架 | Jetpack Compose | 声明式 UI，支持复杂嵌套滚动 |
| 架构 | MVVM + Repository | 数据驱动，单向数据流 |
| 导航 | Compose Navigation | 类型安全路由 |
| 持久化 | Room (SQLite) | 卡牌数据 |
| 依赖注入 | Hilt | 对象图管理 |
| 异步 | Kotlin Coroutines + Flow | 响应式数据流 |
| 最低 SDK | API 26 (Android 8.0) | 覆盖 95%+ 设备 |

### 1.2 核心理念

- **热座模式**：同一设备上 2-4 人轮流操作，当前玩家回合结束后切换
- **游戏引擎**：纯 Kotlin 状态机，与 UI 完全解耦，方便单元测试
- **Compose 声明式 UI**：根据游戏状态自动重组界面

---

## 二、项目目录结构

```
com.dasong.commerce/
├── app/
│   └── MainActivity.kt                    // 单一 Activity，Compose 宿主
├── navigation/
│   └── AppNavGraph.kt                     // 路由导航图
├── ui/
│   ├── theme/
│   │   ├── Theme.kt                       // 复古宋风主题配色
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── home/
│   │   ├── HomeScreen.kt                  // 主菜单（开始游戏、游戏说明）
│   │   └── HomeViewModel.kt
│   ├── guide/
│   │   ├── GuideScreen.kt                 // 游戏说明/规则浏览
│   │   └── components/
│   │       ├── CardCatalogSection.kt       // 卡牌目录（菜品/客人/商铺/事件）
│   │       └── RuleDetailSection.kt        // 规则详情
│   ├── setup/
│   │   ├── SetupScreen.kt                 // 开局设置（人数、顺位）
│   │   └── SetupViewModel.kt
│   ├── game/
│   │   ├── GameScreen.kt                  // 主游戏界面（根布局）
│   │   ├── GameViewModel.kt               // 游戏核心状态管理
│   │   └── components/
│   │       ├── public/
│   │       │   ├── PublicAreaPanel.kt      // 公共区域面板
│   │       │   ├── MenuCardPool.kt         // 菜单牌区域（4品级堆）
│   │       │   ├── ShopCardPool.kt         // 店铺公开选购区（4张）
│   │       │   ├── GuestQueue.kt           // 客人队列（4-6位）
│   │       │   └── EventCardDisplay.kt     // 当前事件展示
│   │       ├── player/
│   │       │   ├── PlayerPanel.kt          // 单个玩家面板
│   │       │   ├── PlayerFundsBar.kt       // 资金显示条
│   │       │   ├── FoundationSlots.kt      // 8块地基区域
│   │       │   ├── RefinedChamber.kt       // 雅阁（手牌库，牌背显示）
│   │       │   ├── KitchenPile.kt          // 后厨（弃牌堆，牌面显示）
│   │       │   └── ShopOnFoundation.kt     // 地基上的店铺
│   │       ├── phase/
│   │       │   ├── PhaseIndicator.kt       // 阶段指示器（1/2/3）
│   │       │   ├── BuyPhasePanel.kt        // 购买阶段 UI
│   │       │   ├── PreparePhasePanel.kt    // 备菜阶段 UI
│   │       │   └── ServePhasePanel.kt      // 招待阶段 UI
│   │       ├── settlement/
│   │       │   ├── SettlementDialog.kt     // 结算弹窗
│   │       │   └── SettlementAnimation.kt  // 收益动画
│   │       └── info/
│   │           ├── GameInfoPopup.kt        // 全局信息弹窗（各玩家资金/店铺）
│   │           └── InfoPanelButton.kt      // 信息面板入口按钮
│   └── result/
│       ├── ResultScreen.kt                 // 胜利/结束画面
│       └── ResultViewModel.kt
├── engine/
│   ├── GameEngine.kt                       // 游戏状态机核心
│   ├── GameState.kt                        // 游戏全局状态
│   ├── PlayerState.kt                      // 单玩家状态
│   ├── TurnManager.kt                      // 回合管理（阶段流转）
│   ├── SettlementEngine.kt                 // 结算引擎
│   ├── EventExecutor.kt                    // 事件牌执行器
│   ├── DeckManager.kt                      // 牌库管理
│   └── WinConditionChecker.kt              // 胜利条件检查
├── model/
│   ├── card/
│   │   ├── MenuCard.kt                     // 菜单牌数据类
│   │   ├── ShopCard.kt                     // 店铺牌数据类
│   │   ├── GuestCard.kt                    // 客人牌数据类
│   │   ├── EventCard.kt                    // 事件牌数据类
│   │   └── CardEnums.kt                    // 卡牌枚举（品级、店铺类型等）
│   ├── CardDataProvider.kt                 // 硬编码卡牌数据（76菜单+36店铺+72客人+9事件）
│   └── Player.kt                           // 玩家数据类
└── util/
    ├── DiceRoller.kt                       // 骰子工具
    ├── CurrencyFormatter.kt                // 货币格式化
    └── Extensions.kt                       // 通用扩展
```

---

## 三、数据模型定义

### 3.1 核心枚举

```kotlin
// ===== 卡牌枚举 =====

enum class MenuGrade { ONE, TWO, THREE, FOUR }  // 一品~四品

enum class ShopType {
    GUA_SI,        // 卦肆
    SHU_FANG,      // 书坊
    JIU_SI,        // 酒肆
    CHOU_DUAN,     // 绸缎庄
    GOU_LAN,       // 勾栏瓦肆
    CI_QI,         // 瓷器铺
    CU_JU,         // 蹴鞠场
    YIN_ZI,        // 饮子铺
    GUAN_PU,       // 关扑铺
    CHA_GUAN,      // 茶馆
    SHUO_SHU,      // 说书场
    SHOU_SHI       // 首饰铺
}

enum class EventType { POSITIVE, NEGATIVE, RESHUFFLE }

enum class GamePhase { BUY, PREPARE, SERVE }  // 购买→备菜→招待

enum class TurnStep {
    PHASE_1_BUY_MENU_OR_SHOP,  // 阶段1：二选一购买
    PHASE_2_PREPARE_OPTIONAL,  // 阶段2：可选备菜
    PHASE_3_SELECT_GUEST,      // 阶段3：选客人
    PHASE_3_SETTLE_MENU,       // 阶段3：菜单结算
    PHASE_3_SETTLE_SHOP,       // 阶段3：店铺结算
    PHASE_3_REFRESH_GUEST,     // 阶段3：刷新客人
    TURN_END_CHECK              // 回合结束/胜利检查
}
```

### 3.2 卡牌数据类

```kotlin
// ===== 菜单牌 =====
data class MenuCard(
    val id: Int,                    // 唯一ID
    val name: String,               // 菜名
    val grade: MenuGrade,           // 品级
    val cost: Int                   // 购买花费（铜钱）
) {
    val baseIncome: Int get() = when (grade) {
        MenuGrade.FOUR -> 2
        MenuGrade.THREE -> 2
        MenuGrade.TWO -> 3
        MenuGrade.ONE -> 2          // + 骰子额外
    }
}

// ===== 店铺牌 =====
data class ShopCard(
    val id: Int,
    val name: String,               // 店名
    val type: ShopType,
    val buildCost: Int,             // 建造费用
    val baseIncome: Int,            // 基础收入（卦肆/酒肆/蹴鞠场/勾栏瓦肆为0，运行时计算）
    val hasDiceMechanic: Boolean,   // 是否骰子机制（卦肆）
    val hasMenuBonus: Boolean,      // 是否菜单加成（酒肆+1、蹴鞠场+2）
    val menuBonus: Int,             // 菜单加成值
    val hasHousingBonus: Boolean,   // 是否基于房屋数（勾栏瓦肆）
    val incomeType: IncomeType      // 收入计算类型
)

enum class IncomeType {
    FIXED,        // 固定收入
    DICE,         // 骰子决定
    MENU_BONUS,   // 菜单数量加成
    HOUSING_COUNT // 房屋数量相关
}

// ===== 客人牌 =====
data class GuestCard(
    val id: Int,
    val name: String,               // 客人名称
    val menuConsumption: Int,       // 消耗菜单张数（2/3/4）
    val shopTypes: List<ShopType>   // 光顾的店铺类型组合
)

// ===== 事件牌 =====
data class EventCard(
    val id: Int,
    val name: String,               // 事件名
    val type: EventType,
    val description: String,        // 效果描述
    val effect: EventEffect         // 效果枚举
)

enum class EventEffect {
    JIAN_YI_YANG_DE,    // 俭以养德：菜单-1
    MEN_KE_LUO_QUE,     // 门可罗雀：仅选1店铺结算
    WU_JIAN_BU_SHANG,   // 无尖不商：店铺收入-1
    SHI_HE_NIAN_FENG,   // 时和年丰：菜单+1
    ZHANG_DENG_JIE_CAI, // 张灯结彩：队列扩为6张
    YIN_ZHUANG_SU_GUO,  // 银装素裹：弃置最右2张
    CI_JIU_YING_XIN,    // 辞旧迎新：重洗队列
    KE_JUAN_ZA_SHUI,    // 苛捐杂税：每店铺模型付2铜钱
    SHUO_GUO_LEI_LEI    // 硕果累累：每人拿2张四品菜单
}
```

### 3.3 玩家状态

```kotlin
data class PlayerState(
    val id: Int,
    val name: String,
    val seatOrder: Int,              // 顺位 1-4
    var funds: Int,                  // 当前资金（铜钱）

    // 牌库
    val refinedChamber: MutableList<MenuCard>,  // 雅阁（抽牌堆，倒序=牌顶在末尾）
    val kitchen: MutableList<MenuCard>,         // 后厨（弃牌堆）

    // 地基与店铺
    val foundations: MutableList<Foundation>    // 8块地基
) {
    /** 菜品牌总数（雅阁+后厨），不可低于6张 */
    val totalMenuCards: Int get() = refinedChamber.size + kitchen.size

    /** 是否可以执行备菜（总牌数 > 6） */
    val canPrepare: Boolean get() = totalMenuCards > 6
}

data class Foundation(
    val index: Int,                  // 0-7，第1~8块
    var shopCard: ShopCard? = null,  // 占据的店铺牌
    var hasModel: Boolean = false    // 是否已建房屋模型
) {
    val clearCost: Int get() = when (index) {
        0 -> 0
        1 -> 2
        2 -> 3
        3 -> 4
        4 -> 5
        5 -> 6
        6 -> 7
        7 -> 9
        else -> error("最多8块地基")
    }
}
```

### 3.4 游戏全局状态

```kotlin
data class GameState(
    // 公共区域
    val menuPool: MenuPool,           // 四品堆（按品级分4堆）
    val shopPool: ShopPool,           // 公开选购区（4张可见）
    val guestDeck: MutableList<GuestCard>,  // 客人+事件混合牌堆
    val guestQueue: MutableList<GuestCard>, // 客人队列（左→右 1/2/3/4号位）
    var activeEvent: EventCard?,      // 当前生效事件

    // 玩家
    val players: List<PlayerState>,
    var currentPlayerIndex: Int,      // 当前操作玩家索引

    // 回合
    var currentPhase: GamePhase,
    var turnStep: TurnStep,

    // 结算临时数据
    var settlementTip: Int = 0,       // 小费
    var settlementMenuIncome: Int = 0,// 菜单收入
    var settlementShopIncome: Int = 0 // 店铺收入
)

data class MenuPool(
    val gradeOne: MutableList<MenuCard>,   // 一品堆
    val gradeTwo: MutableList<MenuCard>,   // 二品堆
    val gradeThree: MutableList<MenuCard>, // 三品堆
    val gradeFour: MutableList<MenuCard>   // 四品堆
)

data class ShopPool(
    val available: MutableList<ShopCard>   // 始终4张公开
)
```

---

## 四、游戏引擎设计（engine/）

### 4.1 GameEngine - 状态机核心

```kotlin
@Singleton
class GameEngine @Inject constructor(
    private val deckManager: DeckManager,
    private val settlementEngine: SettlementEngine,
    private val eventExecutor: EventExecutor,
    private val winChecker: WinConditionChecker
) {
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    /** 初始化新游戏 */
    fun initGame(playerCount: Int) { /* ... */ }

    /** ========== 阶段1：购买阶段 ========== */
    fun buyMenuCard(playerId: Int, card: MenuCard) { /* 支付右上角价格，放入后厨 */ }
    fun buyShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) { /* 付地基清理费+建造费 */ }
    fun endBuyPhase(playerId: Int) { /* 进入备菜阶段 */ }

    /** ========== 阶段2：备菜阶段 ========== */
    /**
     * 花3两从后厨永久弃置1张菜单牌
     * 前提：玩家总菜品牌数（雅阁+后厨）> 6，等于6时不可移除
     */
    fun removeMenu(playerId: Int, card: MenuCard) {
        val player = getPlayer(playerId)
        val totalCards = player.refinedChamber.size + player.kitchen.size
        require(totalCards > 6) { "菜品牌总数恰好为6张，不可再移除" }
        // 支付3两，从后厨移除
    }

    /** 跳过备菜阶段；若总牌数=6则自动跳过进入招待阶段 */
    fun skipPreparePhase() { /* 跳过，进入招待阶段 */ }

    /** ========== 阶段3：招待阶段 ========== */
    fun selectGuest(playerId: Int, queuePosition: Int) { /* 选客人+付小费 */ }
    fun settleMenuIncome(playerId: Int) { /* settlementEngine 计算 */ }
    fun settleShopIncome(playerId: Int) { /* settlementEngine 计算 */ }
    fun refreshGuestQueue() { /* 补客人+事件触发 */ }
    fun endTurn() { /* 检查胜利→切换玩家 */ }
}
```

### 4.2 SettlementEngine - 结算引擎

```kotlin
class SettlementEngine {
    /**
     * 计算菜单收入
     * 1. 客人消耗N张菜单 + 酒肆(+1) + 蹴鞠场(+2)
     * 2. 从雅阁翻牌，累加每张菜单收益
     * 3. 一品额外投骰子
     * 4. 扣除事件效果（俭以养德 -1 / 时和年丰 +1）
     */
    fun calculateMenuIncome(
        player: PlayerState,
        guest: GuestCard,
        event: EventCard?,
        diceRoller: () -> Int  // 骰子函数注入
    ): MenuSettlementResult

    /**
     * 计算店铺收入
     * 1. 客人光顾的店铺类型 → 匹配玩家地基上的已建店铺
     * 2. 每店铺基础收入 + 联动加成
     * 3. 扣减事件效果（无尖不商 -1）
     */
    fun calculateShopIncome(
        player: PlayerState,
        guest: GuestCard,
        event: EventCard?
    ): ShopSettlementResult
}

data class MenuSettlementResult(
    val cardsDrawn: List<MenuCard>,
    val totalIncome: Int,
    val diceResults: List<DiceResult>,  // 一品骰子结果
    val menuCountBonus: Int             // 酒肆/蹴鞠场加成
)

data class ShopSettlementResult(
    val activatedShops: List<ShopActivation>,
    val linkageDetails: List<LinkageDetail>,
    val totalIncome: Int
)
```

### 4.3 WinConditionChecker

```kotlin
class WinConditionChecker {
    fun checkWin(player: PlayerState): Boolean {
        val shopsWithModel = player.foundations.count { it.hasModel && it.shopCard != null }
        return shopsWithModel >= 8 && player.funds >= 50
    }
}
```

---

## 五、UI 布局设计（Jetpack Compose）

### 5.1 整体布局结构

```
┌──────────────────────────────────────────────┐
│  GameScreen (Column, verticalScroll)          │
│                                                │
│  ┌──────────────────────────────────────────┐ │
│  │  顶栏：当前玩家指示 + 回合信息 + ☰信息按钮│ │
│  ├──────────────────────────────────────────┤ │
│  │  PhaseIndicator (阶段指示器)              │ │
│  ├──────────────────────────────────────────┤ │
│  │  PublicAreaPanel (公共区域)               │ │
│  │  ┌────────┐ ┌────────┐ ┌──────────────┐ │ │
│  │  │菜单牌堆│ │店铺区  │ │  客人队列     │ │ │
│  │  │4品级 │ │4张公开 │ │ [1][2][3][4] │ │ │
│  │  │可选堆 │ │选购    │ │              │ │ │
│  │  └────────┘ └────────┘ └──────────────┘ │ │
│  │  ┌──────────────────────────────────────┐ │ │
│  │  │ 当前生效事件: [事件牌名称+效果]       │ │ │
│  │  └──────────────────────────────────────┘ │ │
│  ├──────────────────────────────────────────┤ │
│  │  PlayerList (横向可滑动 Row)              │ │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ │ │
│  │  │玩家1面板 │ │玩家2面板 │ │玩家3面板 │ │ │
│  │  │(当前高亮)│ │          │ │          │ │ │
│  │  └──────────┘ └──────────┘ └──────────┘ │ │
│  ├──────────────────────────────────────────┤ │
│  │  当前阶段操作面板 (phase/)                │ │
│  └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
```

### 5.2 关键 UI 组件详设

#### 5.2.1 HomeScreen（主菜单）

```
┌───────────────────────────┐
│                           │
│     【大宋百商图】         │
│      (宋风Logo/插画)       │
│                           │
│   ┌─────────────────┐     │
│   │   📖 游戏说明    │     │
│   └─────────────────┘     │
│   ┌─────────────────┐     │
│   │   🎮 单机游戏    │     │
│   └─────────────────┘     │
│   ┌─────────────────┐     │
│   │ 🌐 创建房间(todo) │    │
│   └─────────────────┘     │
│   ┌─────────────────┐     │
│   │  🔗 加入房间(todo) │   │
│   └─────────────────┘     │
│                           │
└───────────────────────────┘
```

#### 5.2.2 GuideScreen（游戏说明 + 卡牌目录）

```
┌──────────────────────────────────────┐
│  ← 返回       游戏说明                │
├──────────────────────────────────────┤
│  Tab: [菜品] [客人] [商铺] [事件]    │ ← TabRow 切换
├──────────────────────────────────────┤
│  Tab内容区 (LazyColumn)              │
│                                      │
│  ┌────────────────────────────────┐  │
│  │ 一品 (9铜钱, 2+🎲)              │  │
│  │ 东坡肉 · 御膳坊 · 蟹酿橙        │  │
│  ├────────────────────────────────┤  │
│  │ 二品 (6铜钱, 3固定)             │  │
│  │ 葱爆羊肉 · 糖醋里脊 · ...       │  │
│  ├────────────────────────────────┤  │
│  │ 三品 (3铜钱, 2固定)             │  │
│  │ ...                            │  │
│  ├────────────────────────────────┤  │
│  │ 四品 (免费, 2固定)              │  │
│  │ ...                            │  │
│  └────────────────────────────────┘  │
│                                      │
└──────────────────────────────────────┘
```

#### 5.2.3 GameScreen（主游戏界面）

**整体为可纵向滚动的 Column**，内容超出屏幕时可向下滑动查看所有玩家面板。

```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { GameTopBar(gameState) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
        ) {
            PhaseIndicator(currentPhase = gameState.currentPhase)
            PublicAreaPanel(gameState)
            EventBanner(gameState.activeEvent)
            PlayerPanelsRow(gameState)  // 横向滚动
            CurrentPhasePanel(gameState, viewModel)
        }
    }
}
```

#### 5.2.4 PublicAreaPanel（公共区域）

```kotlin
@Composable
fun PublicAreaPanel(state: GameState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text("【公共区域】", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 菜单牌区域 - 4品级堆叠
            MenuCardPool(state.menuPool)
            // 店铺选购区 - 4张并排
            ShopCardPool(state.shopPool)
            // 客人队列 - 横向排列
            GuestQueue(state.guestQueue)
        }
    }
}

@Composable
fun MenuCardPool(pool: MenuPool) {
    Column {
        Text("菜单牌", fontWeight = FontWeight.Bold)
        Row {
            MenuPile("一品", color1, pool.gradeOne.size, 9)
            MenuPile("二品", color2, pool.gradeTwo.size, 6)
            MenuPile("三品", color3, pool.gradeThree.size, 3)
            MenuPile("四品", color4, pool.gradeFour.size, 0)
        }
    }
}

@Composable
fun GuestQueue(queue: List<GuestCard>) {
    Column {
        Text("客人队列", fontWeight = FontWeight.Bold)
        Row {
            // 队列从左到右展示，最右是1号（首位客人）
            queue.forEachIndexed { index, guest ->
                val posLabel = queue.size - index  // 1号 = 最右
                GuestCardView(
                    guest = guest,
                    position = posLabel,
                    cost = posLabel - 1  // 位置费用：1号免费，2号1两...
                )
            }
        }
    }
}
```

#### 5.2.5 PlayerPanel（单个玩家面板）

```kotlin
@Composable
fun PlayerPanel(
    player: PlayerState,
    isCurrentPlayer: Boolean,
    modifier: Modifier = Modifier
) {
    // 固定宽度 ≈ 屏幕宽度的 85%（4玩家时显示80%），超出部分可横向滑动
    Card(
        modifier = modifier
            .width((LocalConfiguration.current.screenWidthDp * 0.85).dp)
            .border(
                width = if (isCurrentPlayer) 3.dp else 1.dp,
                color = if (isCurrentPlayer) Color(0xFFFFD700) else Color.Gray
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ===== 第一行：玩家信息 + 资金 =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "玩家${player.seatOrder} - ${player.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                // ★ 资金显示 ★
                PlayerFundsBar(funds = player.funds)
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // ===== 第二行：雅阁 + 后厨 =====
            Row(Modifier.fillMaxWidth()) {
                RefinedChamber(player.refinedChamber)  // 牌背堆叠
                Spacer(Modifier.width(16.dp))
                KitchenPile(player.kitchen)              // 牌面展示
            }

            // ===== 第三行：8块地基（2行×4列网格） =====
            Text("地基", style = MaterialTheme.typography.labelLarge)
            FoundationSlots(
                foundations = player.foundations,
                onSlotClick = { /* 购买阶段选择地基 */ }
            )
        }
    }
}

@Composable
fun PlayerFundsBar(funds: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFD700).copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MonetizationOn, "铜钱", tint = Color(0xFFFFD700))
            Spacer(Modifier.width(4.dp))
            Text(
                text = "${funds}两",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB8860B)
            )
        }
    }
}
```

#### 5.2.6 PlayerPanelsRow（横向滑动容器）

```kotlin
@Composable
fun PlayerPanelsRow(state: GameState) {
    // ★ 横向可滑动 ★
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.players) { player ->
            PlayerPanel(
                player = player,
                isCurrentPlayer = player.id == state.players[state.currentPlayerIndex].id,
                modifier = Modifier.animateItemPlacement()
            )
        }
    }
}
```

#### 5.2.7 FoundationSlots（地基区域）

```kotlin
@Composable
fun FoundationSlots(
    foundations: List<Foundation>,
    onSlotClick: (Int) -> Unit
) {
    // 2行 × 4列
    Column {
        for (row in 0..1) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (col in 0..3) {
                    val index = row * 4 + col
                    val foundation = foundations[index]

                    FoundationSlot(
                        foundation = foundation,
                        index = index + 1, // 显示1-8
                        onClick = { onSlotClick(index) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
fun FoundationSlot(
    foundation: Foundation,
    index: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                when {
                    foundation.hasModel -> Color(0xFF8B4513) // 已建模型：棕色
                    foundation.shopCard != null -> Color(0xFFD2B48C) // 有牌无模型：浅棕
                    else -> Color(0xFFF5F5DC) // 空地：米色
                },
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (foundation.shopCard != null) {
                Text(
                    foundation.shopCard!!.name.take(2),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                if (foundation.hasModel) {
                    Icon(
                        Icons.Default.Home,
                        "已建",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            } else {
                Text(
                    "#$index",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "清理:${foundation.clearCost}两",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}
```

#### 5.2.8 GameInfoPopup（全局信息弹窗）

```kotlin
@Composable
fun GameInfoPopup(
    players: List<PlayerState>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 玩家信息总览") },
        text = {
            LazyColumn {
                items(players) { player ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text("玩家${player.seatOrder} - ${player.name}",
                                fontWeight = FontWeight.Bold)
                            Divider()
                            // 资金
                            Row {
                                Text("💰 资金: ")
                                Text("${player.funds}两", color = Color(0xFFB8860B))
                            }
                            // 店铺类型和数量
                            Text("🏠 已建店铺:")
                            val builtShops = player.foundations
                                .filter { it.hasModel && it.shopCard != null }
                                .groupBy { it.shopCard!!.type }
                                .mapValues { it.value.size }

                            if (builtShops.isEmpty()) {
                                Text("  暂无", color = Color.Gray)
                            } else {
                                builtShops.forEach { (type, count) ->
                                    Text("  · ${type.displayName}: ${count}间")
                                }
                            }
                            Text("  总模型数: ${player.foundations.count { it.hasModel }}/8")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
```

#### 5.2.9 InfoPanelButton（信息面板入口）

```kotlin
@Composable
fun InfoPanelButton(onClick: () -> Unit) {
    var showPopup by remember { mutableStateOf(false) }

    IconButton(onClick = { showPopup = true }) {
        Icon(Icons.Default.Info, "玩家信息")
    }

    if (showPopup) {
        GameInfoPopup(
            players = currentPlayers,
            onDismiss = { showPopup = false }
        )
    }
}
```

该按钮放置在 TopBar 右侧，点击后弹出 `GameInfoPopup` 展示所有玩家资金、店铺类型和数量。

#### 5.2.10 阶段操作面板

```kotlin
@Composable
fun CurrentPhasePanel(state: GameState, viewModel: GameViewModel) {
    val currentPlayer = state.players[state.currentPlayerIndex]

    when (state.currentPhase) {
        GamePhase.BUY -> BuyPhasePanel(
            player = currentPlayer,
            menuPool = state.menuPool,
            shopPool = state.shopPool,
            showSelectShop  // 是否展示店铺选择（点地基触发）
        )
        GamePhase.PREPARE -> {
            val totalCards = currentPlayer.refinedChamber.size + currentPlayer.kitchen.size
            val canPrepare = totalCards > 6  // 总牌数>6才能移除
            PreparePhasePanel(
                player = currentPlayer,
                canPrepare = canPrepare,
                onRemove = { card -> viewModel.removeMenu(currentPlayer.id, card) },
                onSkip = { viewModel.skipPreparePhase() }
            )
        }
        GamePhase.SERVE -> ServePhasePanel(
            guestQueue = state.guestQueue,
            currentPlayer = currentPlayer,
            activeEvent = state.activeEvent,
            onSelectGuest = { pos -> viewModel.selectGuest(currentPlayer.id, pos) },
            showSettlement  // 结算动画状态
        )
    }
}
```

### 5.3 Screen 宽度适配策略

| 情况 | 处理方式 |
|------|----------|
| 玩家面板内容多 | 单个面板固定宽度 = 屏幕85%，横向 `LazyRow` 滑动切换查看 |
| 地基网格 + 店铺信息 | 面板内部纵向可滚动 (`verticalScroll`) |
| 公共区域卡牌多 | 菜单堆、店铺区、客人队列用 `horizontalScroll` Row 包裹 |
| 4 位客人 + 6 位（张灯结彩） | 客人队列 `LazyRow` + 自动折叠显示（超出时缩小卡片） |
| 阶段操作面板按钮多 | `FlowRow` 自动换行 |
| 整体布局 | **最外层 `verticalScroll`**，确保内容超出一屏时可下滑 |

---

## 六、导航路由

```kotlin
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("setup") },
                onGuide = { navController.navigate("guide") }
            )
        }

        composable("guide") {
            GuideScreen(onBack = { navController.popBackStack() })
        }

        composable("setup") {
            SetupScreen(
                onGameStart = { playerCount ->
                    navController.navigate("game") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("game") {
            GameScreen(
                onGameEnd = { winnerName ->
                    navController.navigate("result/$winnerName") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("result/{winnerName}") { backStackEntry ->
            val winnerName = backStackEntry.arguments?.getString("winnerName") ?: ""
            ResultScreen(
                winnerName = winnerName,
                onBackHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
```

---

## 七、主题设计（复古宋风）

```kotlin
// Color.kt
val SongGold = Color(0xFFC9A96E)          // 宋风金色
val SongRed = Color(0xFFC43D3D)           // 朱红
val SongInk = Color(0xFF2C2C2C)          // 墨色
val SongPaper = Color(0xFFF5F0E8)        // 宣纸色
val SongTeal = Color(0xFF4A7B6B)         // 青绿
val SongBrown = Color(0xFF8B6914)        // 赭石

// Theme.kt
private val SongLightColorScheme = lightColorScheme(
    primary = SongGold,
    onPrimary = Color.White,
    primaryContainer = SongGold.copy(alpha = 0.15f),
    secondary = SongRed,
    background = SongPaper,
    surface = Color.White,
    onBackground = SongInk,
    onSurface = SongInk,
    // ...
)

// Type.kt - 使用支持中文的衬线字体
val SongTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        // ...
    )
)
```

---

## 八、卡牌数据硬编码

```kotlin
// CardDataProvider.kt
object CardDataProvider {

    // ===== 菜单牌 76张 =====
    val menuCards: List<MenuCard> = buildList {
        // 四品 (低价小吃) - 约28张
        addAll(List(7) { MenuCard(400 + it, "炊饼", MenuGrade.FOUR, 0) })
        addAll(List(7) { MenuCard(410 + it, "馒头", MenuGrade.FOUR, 0) })
        addAll(List(7) { MenuCard(420 + it, "馄饨", MenuGrade.FOUR, 0) })
        addAll(List(7) { MenuCard(430 + it, "汤面", MenuGrade.FOUR, 0) })
        // 三品 (家常菜) - 约25张
        // ...
        // 二品 (硬菜) - 约19张
        // ...
        // 一品 (御宴) - 约4张
        // ...
    }

    // ===== 店铺牌 36张 (12种 × 3张) =====
    val shopCards: List<ShopCard> = listOf(
        ShopCard(1, "卦肆", ShopType.GUA_SI, 5, 0, hasDiceMechanic = true, incomeType = IncomeType.DICE),
        // ... 重复3种 × 每种3张
    )

    // ===== 客人牌 81张 =====
    val guestCards: List<GuestCard> = listOf(
        GuestCard(1, "货郎", menuConsumption = 2, shopTypes = listOf(ShopType.YIN_ZI, ShopType.GUAN_PU)),
        // ...
    ).flatMap { List(3) { it } }  // 每种3张

    // ===== 事件牌 9张 =====
    val eventCards: List<EventCard> = listOf(
        EventCard(1, "俭以养德", EventType.NEGATIVE, "菜单消耗 -1", EventEffect.JIAN_YI_YANG_DE),
        // ...
    )
}
```

---

## 十、热座切换流程

```
┌─────────────────────────────────────────────────┐
│  当前玩家完成回合 (endTurn)                       │
│       ↓                                          │
│  回合间过渡弹窗："请将设备交给玩家N"               │
│       ↓                                          │
│  点击确认 → currentPlayerIndex++                  │
│       ↓                                          │
│  如果最后一个玩家 → currentPlayerIndex = 0 (新一轮)  │
│       ↓                                          │
│  重置 currentPhase = BUY, turnStep = PHASE_1     │
└─────────────────────────────────────────────────┘
```

过渡弹窗实现：

```kotlin
@Composable
fun TurnTransitionDialog(
    nextPlayer: PlayerState,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("回合结束") },
        text = {
            Column {
                Text("当前回合已完成，请将设备交给：")
                Text(
                    "玩家${nextPlayer.seatOrder} - ${nextPlayer.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("确认") }
        }
    )
}
```

---

## 十一、关键交互流程时序图

```
玩家操作                  ViewModel                  GameEngine               UI更新
   │                        │                          │                       │
   │── buyShop(地基3) ──────→│                          │                       │
   │                        │── buyShopCard(p1,sh,3) ──→│                       │
   │                        │                          │  扣减资金             │
   │                        │                          │  更新foundation[3]    │
   │                        │                          │  补shopPool           │
   │                        │                          │  gameState.emit() ───→│ 重组UI
   │                        │←── state:Flow ─────────  │                       │
   │                        │                          │                       │
   │── endBuyPhase() ──────→│── endBuyPhase(p1) ─────→│                       │
   │                        │                          │  phase = PREPARE      │
   │                        │                          │  gameState.emit() ───→│ 重组UI
   │                        │                          │                       │
   │── skipPrepare() ──────→│── skipPreparePhase() ──→│                       │
   │                        │                          │  phase = SERVE        │
   │                        │                          │  gameState.emit() ───→│ 重组UI
   │                        │                          │                       │
   │── selectGuest(1号) ───→│── selectGuest(p1, 1) ──→│                       │
   │                        │                          │  移除客人，记录小费    │
   │                        │                          │  结算菜单收入          │
   │                        │                          │  结算店铺收入          │
   │                        │                          │  刷新客人/事件          │
   │                        │                          │  检查胜利条件          │
   │                        │                          │  gameState.emit() ───→│ 重组UI
   │←── 结算弹窗 ───────────│                          │                       │
```

---

## 十二、依赖清单（app/build.gradle.kts）

```kotlin
dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Hilt (DI)
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

---

## 十三、开发阶段规划

| 阶段 | 内容 | 估时 |
|------|------|------|
| **Phase 1** | 数据模型 + 卡牌数据硬编码 + GameEngine 核心状态机 | 3天 |
| **Phase 2** | HomeScreen + SetupScreen + GuideScreen（基础目录+卡牌目录） | 2天 |
| **Phase 3** | GameScreen 骨架 + PublicAreaPanel + PlayerPanelsRow | 3天 |
| **Phase 4** | 阶段操作面板（购买/备菜/招待）+ SettlementEngine 结算 | 4天 |
| **Phase 5** | 结算动画 + GameInfoPopup + 事件牌效果 | 2天 |
| **Phase 6** | 过渡弹窗 + ResultScreen | 1天 |
| **Phase 7** | 主题润色 + 宋风视觉 + 测试修 bug | 2天 |

**总计约 17 人天**

---

## 十四、风险与注意事项

1. **结算复杂度**：联动加成多层嵌套，`SettlementEngine` 需要充足的单元测试覆盖
2. **客人队列**：事件牌 "张灯结彩" 使队列从4扩为6，UI 需动态适配宽度
3. **一品骰子**：骰子结果需在 UI 中播放动画，增加沉浸感
4. **热座隐私**：回合过渡弹窗可有效防止误操作，但可考虑加入确认机制
5. **卡牌数量多**：菜单76+店铺36+客人81+事件9 = 202 张不同卡牌，需做好硬编码数据管理
6. **屏幕适配**：小屏手机（约360dp）需确保地基网格不溢出，必要时缩小单格尺寸
