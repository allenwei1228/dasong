# 联机游戏登录与房间管理模板

本文档基于 ACQUIRE 项目的实际代码，总结了联机游戏的登录、房间创建与加入的完整流程，可作为后续开发的模板参考。

---

## 目录

1. [数据层](#1-数据层)
2. [业务逻辑层](#2-业务逻辑层)
3. [UI 层](#3-ui-层)
4. [房间创建流程](#4-房间创建流程)
5. [加入房间流程](#5-加入房间流程)
6. [开始游戏流程](#6-开始游戏流程)
7. [同步机制](#7-同步机制)
8. [Supabase 数据库设计](#8-supabase-数据库设计)

---

## 1. 数据层

### 1.1 房间状态枚举

```kotlin
// RoomStatus.kt
package com.acquireonline.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RoomStatus {
    WAITING,     // 等待中（房间创建后）
    PLAYING,     // 游戏进行中
    FINISHED,    // 游戏已结束
}
```

### 1.2 房间数据模型

```kotlin
// RoomData.kt
package com.acquireonline.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomData(
    @SerialName("room_code")
    val roomCode: String,              // 房间码（6位唯一标识）
    @SerialName("owner_id")
    val ownerId: String,               // 房主 ID
    @SerialName("player_ids")
    val playerIds: List<String> = emptyList(),   // 玩家 ID 列表
    @SerialName("player_names")
    val playerNames: Map<String, String> = emptyMap(),  // 玩家 ID -> 昵称
    @SerialName("max_players")
    val maxPlayers: Int = 2,            // 最大人数
    val status: RoomStatus = RoomStatus.WAITING,
    @SerialName("created_at")
    val createdAt: String? = null,
    // ========== 游戏通用配置参数（替换为实际游戏参数） ==========
    // var1 / var2 / var3 / var4 /...等 ：供不同游戏自定义使用的通用参数
    @SerialName("var1")
    val var1: Int = 0,
    @SerialName("var2")
    val var2: Int = 0,
    @SerialName("var3")
    val var3: Int = 0,
    @SerialName("var4")
    val var4: Boolean = false,
) {
    val isFull: Boolean get() = playerIds.size >= maxPlayers
}
```

### 1.3 玩家状态模型

```kotlin
// PlayerState.kt
package com.acquireonline.app.data

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val id: String,
    val name: String,
    val cash: Int,
    val hand: List<String> = emptyList(),
    val stocks: Map<String, Int> = emptyMap(),
    val items: List<ItemType> = emptyList(),
)
```

### 1.4 在线游戏状态包装

```kotlin
// OnlineGameState.kt
package com.acquireonline.app.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnlineGameState(
    @SerialName("room_code")
    val roomCode: String,
    val state: GameState,
    val version: Int = 0,
)
```

---

## 2. 业务逻辑层

### 2.1 OnlineManager（单例模式）

核心管理器，负责所有网络通信和数据同步。

```kotlin
// OnlineManager.kt
package com.acquireonline.app.online

object OnlineManager {

    // ============ 状态定义 ============

    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTED,
        SYNCING,
    }

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()

    private val _playerId = MutableStateFlow("")
    val playerId: StateFlow<String> = _playerId.asStateFlow()

    private val _playerName = MutableStateFlow("玩家")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _roomFlow = MutableStateFlow<RoomData?>(null)
    val roomFlow: StateFlow<RoomData?> = _roomFlow.asStateFlow()

    private val _gameStateFlow = MutableStateFlow<GameState?>(null)
    val gameStateFlow: StateFlow<GameState?> = _gameStateFlow.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    // ============ 连接与认证 ============

    suspend fun connect(displayName: String): Boolean = runCatching {
        _playerName.value = displayName.ifBlank { "玩家" }
        if (_playerId.value.isBlank()) {
            _playerId.value = generatePlayerId()
        }
        _connectionState.value = ConnectionStatus.CONNECTED
        _events.tryEmit("已连接为 ${_playerName.value}")
        true
    }.getOrElse {
        _connectionState.value = ConnectionStatus.DISCONNECTED
        _events.tryEmit(it.message ?: "连接失败")
        false
    }

    private fun generatePlayerId(): String =
        "player_${System.currentTimeMillis()}_${(1000..9999).random()}"

    // ============ 房间操作 ============

    suspend fun createRoom(
        maxPlayers: Int,
        var1: Int = 0,   // 游戏自定义参数1
        var2: Int = 0,   // 游戏自定义参数2
        var3: Int = 0,   // 游戏自定义参数3
        var4: Boolean = false,  // 游戏自定义参数4（布尔开关）
    ): RoomData {
        // 参数校验
        require(maxPlayers in 2..6) { "房间人数必须在 2-6 之间" }
        ensureConnected()

        // 构建房间数据
        val room = RoomData(
            roomCode = generateRoomCode(),     // 生成 6 位房间码
            ownerId = _playerId.value,         // 创建者成为房主
            playerIds = listOf(_playerId.value),
            playerNames = mapOf(_playerId.value to _playerName.value),
            maxPlayers = maxPlayers,
            status = RoomStatus.WAITING,
            var1 = var1,
            var2 = var2,
            var3 = var3,
            var4 = var4,
        )

        _roomFlow.value = room
        _gameStateFlow.value = null

        return try {
            val saved = when (syncMode()) {
                SyncMode.DEMO -> DemoServer.createRoom(room)
                SyncMode.SUPABASE -> createRoomRemote(room)
            }
            _roomFlow.value = saved
            startSync(saved.roomCode)
            saved
        } catch (throwable: Throwable) {
            _roomFlow.value = null
            throw throwable
        }
    }

    suspend fun joinRoom(roomCode: String): RoomData {
        ensureConnected()
        val normalizedCode = roomCode.trim().uppercase()
        require(normalizedCode.length == 6) { "请输入 6 位房间码" }

        val room = loadRoom(normalizedCode) ?: error("房间不存在")
        require(!room.isFull || _playerId.value in room.playerIds) { "房间已满" }

        // 如果已在房间中更新昵称，否则加入房间
        val joinedRoom = if (_playerId.value in room.playerIds) {
            room.copy(playerNames = room.playerNames + (_playerId.value to _playerName.value))
        } else {
            room.copy(
                playerIds = room.playerIds + _playerId.value,
                playerNames = room.playerNames + (_playerId.value to _playerName.value),
            )
        }

        val saved = when (syncMode()) {
            SyncMode.DEMO -> DemoServer.updateRoom(joinedRoom)
            SyncMode.SUPABASE -> updateRoomRemote(joinedRoom)
        }

        _roomFlow.value = saved
        if (saved.status == RoomStatus.PLAYING) {
            _gameStateFlow.value = loadGameState(saved.roomCode)
        }
        startSync(saved.roomCode)
        return saved
    }

    suspend fun startGame(): GameState {
        val room = _roomFlow.value ?: error("当前未加入任何房间")
        require(room.ownerId == _playerId.value) { "只有房主可以开始游戏" }
        require(room.playerIds.size >= 2) { "至少需要 2 名玩家才能开始" }

        // 更新房间状态为 PLAYING
        val startedRoom = room.copy(status = RoomStatus.PLAYING)

        // 初始化游戏状态
        val state = GameEngine.createInitialState(
            roomCode = room.roomCode,
            players = room.playerIds.map { id ->
                id to (room.playerNames[id] ?: id.takeLast(4))
            },
            var1 = room.var1,
            var2 = room.var2,
            var3 = room.var3,
            var4 = room.var4,
        )

        saveRoom(startedRoom)
        publishGameState(state)
        _roomFlow.value = startedRoom
        return state
    }

    fun resetLocalNavigation() {
        _roomFlow.value = null
        _gameStateFlow.value = null
        syncJob?.cancel()
        syncJob = null
    }

    // ============ 辅助方法 ============

    private fun generateRoomCode(): String = buildString {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  // 排除易混淆字符
        repeat(6) { append(chars.random()) }
    }

    private fun ensureConnected() {
        require(_playerId.value.isNotBlank()) { "请先连接服务器" }
    }

    private fun syncMode(): SyncMode =
        if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
            SyncMode.SUPABASE
        } else {
            SyncMode.DEMO
        }
}
```

### 2.2 OnlineViewModel

ViewModel 层，处理 UI 状态和用户交互。

```kotlin
// OnlineViewModel.kt
package com.acquireonline.app.online

class OnlineViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineUiState())
    val uiState: StateFlow<OnlineUiState> = _uiState.asStateFlow()

    val connectionState = OnlineManager.connectionState
    val currentRoom = OnlineManager.roomFlow
    val gameState = OnlineManager.gameStateFlow
    val events = OnlineManager.events

    fun updatePlayerName(name: String) {
        _uiState.value = _uiState.value.copy(playerName = name)
    }

    fun updateRoomCodeInput(code: String) {
        _uiState.value = _uiState.value.copy(roomCodeInput = code.uppercase())
    }

    fun connect() {
        viewModelScope.launch {
            val name = _uiState.value.playerName.ifBlank { "玩家" }
            OnlineManager.connect(name)
        }
    }

    fun createRoom(maxPlayers: Int, var1: Int, var2: Int, var3: Int, var4: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val room = OnlineManager.createRoom(
                    maxPlayers = maxPlayers,
                    var1 = var1,
                    var2 = var2,
                    var3 = var3,
                    var4 = var4,
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentRoom = room,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(e.message ?: "创建房间失败")
            }
        }
    }

    fun joinRoom() {
        viewModelScope.launch {
            val code = _uiState.value.roomCodeInput
            if (code.length != 6) {
                _events.emit("请输入 6 位房间码")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val room = OnlineManager.joinRoom(code)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentRoom = room,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(e.message ?: "加入房间失败")
            }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val state = OnlineManager.startGame()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentRoom = _currentRoom.value?.copy(status = RoomStatus.PLAYING),
                    gameState = state,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _events.emit(e.message ?: "开始游戏失败")
            }
        }
    }

    fun leaveRoom() {
        OnlineManager.resetLocalNavigation()
        _uiState.value = _uiState.value.copy(
            currentRoom = null,
            gameState = null,
        )
    }

    val isRoomOwner: Boolean
        get() = currentRoom.value?.ownerId == OnlineManager.playerId.value
}
```

### 2.3 OnlineUiState

UI 状态数据类。

```kotlin
// OnlineUiState.kt
package com.acquireonline.app.online

data class OnlineUiState(
    val playerName: String = "",
    val roomCodeInput: String = "",
    val currentRoom: RoomData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
```

---

## 3. UI 层

### 3.1 RoomScreen（房间界面）

```kotlin
// RoomScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    uiState: OnlineUiState,
    onPlayerNameChange: (String) -> Unit,
    onRoomCodeChange: (String) -> Unit,
    onCreateRoom: (Int, Int, Int, Int, Boolean) -> Unit,  // maxPlayers, var1, var2, var3, var4
    onJoinRoom: () -> Unit,
    onStartGame: () -> Unit,
    onLeaveRoom: () -> Unit,
) {
    var maxPlayers by remember { mutableIntStateOf(2) }
    var var1 by remember { mutableIntStateOf(0) }  // 游戏自定义参数1
    var var2 by remember { mutableIntStateOf(0) }  // 游戏自定义参数2
    var var3 by remember { mutableIntStateOf(0) }  // 游戏自定义参数3
    var var4 by remember { mutableStateOf(false) }  // 游戏自定义参数4
    val room = uiState.currentRoom

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("游戏大厅") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ============ 昵称输入 ============
            OutlinedTextField(
                value = uiState.playerName,
                onValueChange = onPlayerNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("你的昵称") },
                singleLine = true,
            )

            if (room == null) {
                // ============ 创建房间区域 ============
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("创建房间", style = MaterialTheme.typography.titleMedium)

                        // 人数选择
                        Text("选择本局人数上限")
                        FlowRow {
                            (2..6).forEach { count ->
                                FilterChip(
                                    selected = maxPlayers == count,
                                    onClick = { maxPlayers = count },
                                    label = { Text("$count 人") },
                                )
                            }
                        }

                        // 游戏参数1（如棋盘大小）
                        Text("参数1（游戏自定义）")
                        FlowRow {
                            (1..5).forEach { v ->
                                FilterChip(
                                    selected = var1 == v,
                                    onClick = { var1 = v },
                                    label = { Text("选项$v") },
                                )
                            }
                        }

                        // 游戏参数2
                        Text("参数2（游戏自定义）")
                        FlowRow {
                            (1..5).forEach { v ->
                                FilterChip(
                                    selected = var2 == v,
                                    onClick = { var2 = v },
                                    label = { Text("选项$v") },
                                )
                            }
                        }

                        // 游戏参数3
                        Text("参数3（游戏自定义）")
                        FlowRow {
                            (1..5).forEach { v ->
                                FilterChip(
                                    selected = var3 == v,
                                    onClick = { var3 = v },
                                    label = { Text("选项$v") },
                                )
                            }
                        }

                        // 游戏参数4（布尔开关）
                        FilterChip(
                            selected = var4,
                            onClick = { var4 = !var4 },
                            label = { Text(if (var4) "已开启特殊模式" else "开启特殊模式") },
                        )

                        Button(
                            onClick = { onCreateRoom(maxPlayers, var1, var2, var3, var4) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("创建联机房间")
                        }
                    }
                }

                // ============ 加入房间区域 ============
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("加入房间", style = MaterialTheme.typography.titleMedium)

                        OutlinedTextField(
                            value = uiState.roomCodeInput,
                            onValueChange = onRoomCodeChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("输入 6 位房间码") },
                            singleLine = true,
                        )

                        OutlinedButton(
                            onClick = onJoinRoom,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("加入现有房间")
                        }
                    }
                }
            } else {
                // ============ 房间信息区域 ============
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("房间信息", style = MaterialTheme.typography.titleMedium)
                        Text("房间码：${room.roomCode}")
                        Text("当前人数：${room.playerIds.size}/${room.maxPlayers}")
                        Text("var1=${room.var1}, var2=${room.var2}, var3=${room.var3}, var4=${room.var4}")
                        Text("房间状态：${room.status.name}")

                        // 玩家列表
                        room.playerIds.forEachIndexed { index, playerId ->
                            val playerName = room.playerNames[playerId] ?: playerId.takeLast(4)
                            Text("${index + 1}. $playerName${if (playerId == room.ownerId) "（房主）" else ""}")
                        }

                        // 房主操作区域
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // 房主：修改人数上限
                            if (uiState.isRoomOwner && room.status == RoomStatus.WAITING) {
                                Text("人数上限：")
                                (2..6).forEach { count ->
                                    FilterChip(
                                        selected = room.maxPlayers == count,
                                        onClick = { /* TODO */ },
                                        label = { Text("$count") },
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // 房主：开始游戏按钮
                            if (uiState.isRoomOwner && room.status == RoomStatus.WAITING) {
                                Button(
                                    onClick = onStartGame,
                                    enabled = room.playerIds.size >= 2,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text("开始游戏")
                                }
                            }

                            // 退出房间按钮
                            OutlinedButton(
                                onClick = onLeaveRoom,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("退出房间")
                            }
                        }
                    }
                }

                // ============ 等待提示（非房主） ============
                if (!uiState.isRoomOwner && room.status == RoomStatus.WAITING) {
                    Text("等待房主开始游戏……")
                }
            }
        }
    }
}
```

### 3.2 AcquireApp（路由整合）

```kotlin
// AcquireApp.kt
@Composable
fun AcquireApp(viewModel: OnlineViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    val navController = rememberNavController()

    NavHost(navController, startDestination = "room") {
        composable("room") {
            RoomScreen(
                uiState = uiState,
                onPlayerNameChange = viewModel::updatePlayerName,
                onRoomCodeChange = viewModel::updateRoomCodeInput,
                onCreateRoom = { maxPlayers, var1, var2, var3, var4 ->
                    viewModel.createRoom(maxPlayers, var1, var2, var3, var4)
                },
                onJoinRoom = viewModel::joinRoom,
                onStartGame = viewModel::startGame,
                onLeaveRoom = viewModel::leaveRoom,
            )
        }

        composable("game") {
            gameState?.let { state ->
                GameScreen(
                    uiState = uiState,
                    gameState = state,
                    onLeaveRoom = viewModel::leaveRoom,
                    // ... 其他游戏回调
                )
            }
        }
    }

    // 监听导航
    LaunchedEffect(gameState) {
        if (gameState != null) {
            navController.navigate("game")
        } else {
            navController.navigate("room") {
                popUpTo("room") { inclusive = true }
            }
        }
    }
}
```

---

## 4. 房间创建流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        房间创建流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. [UI] 用户输入昵称 → updatePlayerName()                       │
│           ↓                                                      │
│  2. [UI] 用户选择房间配置（人数、棋盘大小、道具模式）                   │
│           ↓                                                      │
│  3. [UI] 点击"创建联机房间" → createRoom(maxPlayers, boardSize)   │
│           ↓                                                      │
│  4. [VM] OnlineViewModel.createRoom()                            │
│           ↓                                                      │
│  5. [Manager] OnlineManager.createRoom()                         │
│           ├── 生成 6 位房间码 generateRoomCode()                  │
│           ├── 构建 RoomData 对象                                  │
│           │   ├── roomCode: 6位随机码                             │
│           │   ├── ownerId: 当前玩家ID（房主）                      │
│           │   ├── playerIds: [房主ID]                             │
│           │   ├── playerNames: {房主ID -> 昵称}                   │
│           │   ├── maxPlayers: 最大人数                            │
│           │   ├── status: WAITING                                 │
│           │   └── 其他配置...                                     │
│           ↓                                                      │
│  6. [同步] 保存到服务器                                           │
│           ├── DEMO 模式 → DemoServer.createRoom()                │
│           └── SUPABASE 模式 → createRoomRemote()                 │
│           ↓                                                      │
│  7. [同步] 启动轮询同步 startSync(roomCode)                       │
│           └── 每 1 秒拉取一次房间和游戏状态                        │
│           ↓                                                      │
│  8. [State] 更新 uiState.currentRoom                             │
│           ↓                                                      │
│  9. [UI] 显示房间信息（房间码、玩家列表）                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 房间码生成规则

```kotlin
private fun generateRoomCode(): String = buildString {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"  // 排除易混淆字符
    repeat(6) { append(chars.random()) }
}
```

**特点**：
- 使用 30 个不含易混淆字符的字母数字（去掉 I、O、0、1 等）
- 6 位长度，共 30^6 ≈ 7.29 亿种组合
- 全大写显示

---

## 5. 加入房间流程

```
┌─────────────────────────────────────────────────────────────────┐
│                        加入房间流程                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. [UI] 用户输入 6 位房间码 → updateRoomCodeInput()              │
│           ↓                                                      │
│  2. [UI] 点击"加入现有房间" → joinRoom()                          │
│           ↓                                                      │
│  3. [VM] OnlineViewModel.joinRoom()                             │
│           ├── 校验房间码长度（必须 6 位）                          │
│           ↓                                                      │
│  4. [Manager] OnlineManager.joinRoom(roomCode)                  │
│           ├── 规范化房间码（大写 + 去空格）                        │
│           ├── 加载房间数据 loadRoom(roomCode)                    │
│           │   ├── 房间不存在 → 报错                              │
│           │   └── 房间已满 → 报错（除非已在房间）                  │
│           ↓                                                      │
│  6. [判断] 当前玩家是否已在房间中？                                │
│           ├── 是 → 只更新昵称                                     │
│           └── 否 → 添加到 playerIds 和 playerNames               │
│           ↓                                                      │
│  7. [同步] 保存更新后的房间数据                                    │
│           ↓                                                      │
│  8. [判断] 房间状态是否为 PLAYING？                               │
│           ├── 是 → 加载游戏状态 loadGameState()                  │
│           └── 否 → 显示等待界面                                   │
│           ↓                                                      │
│  9. [同步] 启动轮询同步 startSync(roomCode)                       │
│           ↓                                                      │
│  10. [State] 更新 uiState.currentRoom                            │
│           ↓                                                      │
│  11. [UI] 跳转到房间等待界面                                      │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 关键校验逻辑

```kotlin
suspend fun joinRoom(roomCode: String): RoomData {
    ensureConnected()
    val normalizedCode = roomCode.trim().uppercase()
    require(normalizedCode.length == 6) { "请输入 6 位房间码" }

    val room = loadRoom(normalizedCode) ?: error("房间不存在")
    require(!room.isFull || _playerId.value in room.playerIds) { "房间已满" }
    // ...
}
```

---

## 6. 开始游戏流程

```
┌─────────────────────────────────────────────────────────────────┐
│                       开始游戏流程                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. [UI] 房主点击"开始游戏" → startGame()                        │
│           ↓                                                      │
│  2. [VM] OnlineViewModel.startGame()                             │
│           ↓                                                      │
│  3. [Manager] OnlineManager.startGame()                          │
│           ├── 校验：是房主吗？                                    │
│           ├── 校验：玩家数 >= 2？                                 │
│           ↓                                                      │
│  4. [Engine] GameEngine.createInitialState()                    │
│           ├── 生成随机种子 seed                                  │
│           ├── 洗牌分发给各玩家                                    │
│           ├── 初始化玩家 PlayerState 列表                        │
│           └── 构建初始 GameState                                 │
│           ↓                                                      │
│  5. [同步] 保存房间状态（status = PLAYING）                       │
│           ↓                                                      │
│  6. [同步] 发布游戏状态 publishGameState(state)                  │
│           ↓                                                      │
│  7. [State] 更新 uiState.gameState                               │
│           ↓                                                      │
│  8. [UI] 导航到游戏界面                                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 游戏初始化（GameEngine）

```kotlin
object GameEngine {

    fun createInitialState(
        roomCode: String,
        players: List<Pair<String, String>>,  // (playerId, playerName)
        var1: Int = 0,   // 游戏自定义参数1
        var2: Int = 0,   // 游戏自定义参数2
        var3: Int = 0,   // 游戏自定义参数3
        var4: Boolean = false,  // 游戏自定义参数4
        seed: Long = System.currentTimeMillis(),
    ): GameState {
        require(players.size in 2..6) { "需要 2-6 名玩家" }
        // 游戏参数校验由具体游戏自行实现

        // 构建玩家状态（示例：使用 var3 作为起始资金）
        val playerStates = players.map { (id, name) ->
            PlayerState(
                id = id,
                name = name.ifBlank { "玩家${id.takeLast(4)}" },
                cash = var3,  // var3 用作起始资金
                hand = /* 从牌堆分发 */,
            )
        }

        return GameState(
            roomCode = roomCode,
            phase = GamePhase.PLAYING,
            turnStep = TurnStep.PLACE_TILE,
            players = playerStates,
            currentPlayerIndex = 0,
            // var1, var2, var4 可根据游戏需求使用
            // ...
        )
    }
}
```

---

## 7. 同步机制

### 7.1 轮询同步

```kotlin
private fun startSync(roomCode: String) {
    syncJob?.cancel()
    syncJob = scope.launch {
        while (isActive) {
            _connectionState.value = ConnectionStatus.SYNCING
            runCatching {
                val remoteRoom = loadRoom(roomCode)
                val remoteGame = loadGameState(roomCode)
                remoteRoom to remoteGame
            }.onSuccess { (room, gameState) ->
                if (room != null) {
                    _roomFlow.value = room
                }
                if (gameState != null) {
                    val localVersion = _gameStateFlow.value?.version ?: -1
                    if (gameState.version >= localVersion) {
                        _gameStateFlow.value = gameState
                    }
                }
                _connectionState.value = ConnectionStatus.CONNECTED
            }.onFailure { throwable ->
                _connectionState.value = ConnectionStatus.CONNECTED
                _events.tryEmit(throwable.message ?: "同步失败")
            }
            delay(1_000)  // 每秒同步一次
        }
    }
}
```

### 7.2 双模式支持

```kotlin
private enum class SyncMode {
    DEMO,       // 本地内存（开发测试用）
    SUPABASE,   // Supabase 远程数据库
}

private fun syncMode(): SyncMode =
    if (BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()) {
        SyncMode.SUPABASE
    } else {
        SyncMode.DEMO
    }
```

### 7.3 DemoServer（本地内存实现）

```kotlin
private object DemoServer {
    private val rooms = mutableMapOf<String, RoomData>()
    private val gameStates = mutableMapOf<String, GameState>()

    suspend fun createRoom(room: RoomData): RoomData {
        rooms[room.roomCode] = room
        return room
    }

    suspend fun updateRoom(room: RoomData): RoomData {
        rooms[room.roomCode] = room
        return room
    }

    suspend fun getRoom(roomCode: String): RoomData? = rooms[roomCode]

    suspend fun saveGameState(state: GameState): GameState {
        gameStates[state.roomCode] = state
        return state
    }

    suspend fun getGameState(roomCode: String): GameState? = gameStates[roomCode]
}
```

---

## 8. Supabase 数据库设计

### 8.1 rooms 表

```sql
CREATE TABLE rooms (
    room_code VARCHAR(6) PRIMARY KEY,
    owner_id VARCHAR(64) NOT NULL,
    player_ids TEXT[] NOT NULL DEFAULT '{}',
    player_names JSONB NOT NULL DEFAULT '{}',
    max_players INTEGER NOT NULL DEFAULT 2 CHECK (max_players BETWEEN 2 AND 6),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    -- 游戏通用配置参数
    var1 INTEGER NOT NULL DEFAULT 0,
    var2 INTEGER NOT NULL DEFAULT 0,
    var3 INTEGER NOT NULL DEFAULT 0,
    var4 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 索引
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_rooms_owner ON rooms(owner_id);
```

### 8.2 game_states 表

```sql
CREATE TABLE game_states (
    room_code VARCHAR(6) PRIMARY KEY REFERENCES rooms(room_code),
    state JSONB NOT NULL,
    version INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_game_states_room ON game_states(room_code);
```

### 8.3 行级安全策略（RLS）

```sql
-- rooms 表：任何已登录用户可读取，房间成员可更新
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow read" ON rooms FOR SELECT USING (true);
CREATE POLICY "Allow update" ON rooms FOR UPDATE USING (true);

-- game_states 表：同 rooms
ALTER TABLE game_states ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow read" ON game_states FOR SELECT USING (true);
CREATE POLICY "Allow update" ON game_states FOR UPDATE USING (true);
```

---

## 9. 完整流程时序图

```
┌────────┐     ┌──────────┐     ┌────────────┐     ┌──────────────┐     ┌─────────┐
│ Player │     │   UI     │     │ ViewModel  │     │OnlineManager │     │ Server  │
└───┬────┘     └────┬─────┘     └─────┬──────┘     └──────┬───────┘     └────┬────┘
    │               │                │                   │                 │
    │ 输入昵称       │                │                   │                 │
    │───────────────>│                │                   │                 │
    │               │                │                   │                 │
    │ 点击创建房间    │                │                   │                 │
    │───────────────>│                │                   │                 │
    │               │ createRoom()    │                   │                 │
    │               │───────────────>│                   │                 │
    │               │                │ connect(name)     │                 │
    │               │                │───────────────────>│                 │
    │               │                │                   │                 │
    │               │                │ generateRoomCode()│                 │
    │               │                │ createRoomData()  │                 │
    │               │                │                   │ saveRoom()      │
    │               │                │                   │────────────────>│
    │               │                │                   │<────────────────│
    │               │                │ startSync()       │                 │
    │               │                │                   │                 │
    │               │ RoomData       │                   │                 │
    │               │<───────────────│                   │                 │
    │  显示房间信息   │                │                   │                 │
    │<───────────────│                │                   │                 │
    │               │                │                   │                 │
    │ ... 等待其他玩家加入 ...         │                   │                 │
    │               │                │                   │                 │
    │ 房主点击开始    │                │                   │                 │
    │───────────────>│                │                   │                 │
    │               │ startGame()    │                   │                 │
    │               │───────────────>│                   │                 │
    │               │                │ startGame()       │                 │
    │               │                │───────────────────>│                 │
    │               │                │                   │                 │
    │               │                │ createInitialState()              │
    │               │                │ publishGameState()                  │
    │               │                │                   │────────────────>│
    │               │                │                   │<────────────────│
    │               │                │ GameState         │                 │
    │               │                │<───────────────────│                 │
    │               │ GameState      │                   │                 │
    │               │<───────────────│                   │                 │
    │  跳转到游戏界面 │                │                   │                 │
    │<───────────────│                │                   │                 │
    │               │                │                   │                 │
    │ ... 游戏进行中（每秒同步一次）...│                   │                 │
    │               │                │                   │                 │
```

---

## 10. 快速参考 Checklist

### 新项目集成清单

- [ ] 创建 `RoomStatus` 枚举
- [ ] 创建 `RoomData` 数据类（含 `@SerialName`）
- [ ] 创建 `OnlineUiState` 数据类
- [ ] 创建 `OnlineManager` 单例（状态 + 方法）
- [ ] 创建 `OnlineViewModel`
- [ ] 创建 `RoomScreen` Compose 界面
- [ ] 配置 Supabase 数据库表
- [ ] 实现轮询同步逻辑
- [ ] 实现 DemoServer 供本地测试

### 关键约束条件

| 参数 | 最小值 | 最大值 | 默认值 |
|------|--------|--------|--------|
| 房间人数 | 2 | 6 | 2 |
| var1-var3（游戏自定义） | INT 范围 | INT 范围 | 0 |
| var4（游戏自定义） | false | true | false |
| 房间码长度 | 6 | 6 | - |
