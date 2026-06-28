package com.dasong.commerce.online

import android.content.Context
import android.content.SharedPreferences
import com.dasong.commerce.BuildConfig
import com.dasong.commerce.engine.GameState
import com.dasong.commerce.model.card.GamePhase
import com.dasong.commerce.model.card.TurnStep
import com.dasong.commerce.util.LogUtil
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

object OnlineManager {

    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTED,
        SYNCING,
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // JSON 序列化器（忽略未知字段，兼容未来扩展）
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    // SharedPreferences 键名
    private const val PREFS_NAME = "online_manager"
    private const val KEY_PLAYER_ID = "player_id"

    // 持久化存储（进程重启后恢复 playerId）
    private var prefs: SharedPreferences? = null

    /**
     * 初始化 OnlineManager（在 Application.onCreate 中调用）。
     * 从 SharedPreferences 恢复 playerId，确保进程重启后可重连。
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedId = prefs?.getString(KEY_PLAYER_ID, null)
        if (!savedId.isNullOrBlank()) {
            _playerId.value = savedId
            LogUtil.d("OnlineManager", "从本地恢复 playerId: ${savedId.take(8)}...")
        }
    }

    // Supabase 客户端（懒初始化）
    private val supabase by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Postgrest)
        }
    }

    private val _connectionState = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionState: StateFlow<ConnectionStatus> = _connectionState.asStateFlow()

    private val _playerId = MutableStateFlow("")
    val playerId: StateFlow<String> = _playerId.asStateFlow()

    private val _playerName = MutableStateFlow("玩家")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _roomFlow = MutableStateFlow<RoomData?>(null)
    val roomFlow: StateFlow<RoomData?> = _roomFlow.asStateFlow()

    /** 从服务器同步下来的最新游戏状态（非当前玩家回合时，通过此 Flow 接收更新） */
    private val _syncedGameState = MutableStateFlow<GameState?>(null)
    val syncedGameState: StateFlow<GameState?> = _syncedGameState.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var syncJob: kotlinx.coroutines.Job? = null

    /** 本地上次推送的版本号，用于避免自己推送的状态触发重复刷新 */
    private var lastPublishedVersion: Long = -1

    /** 标记最近一次 joinRoom 是否为重连（由 GameViewModel 消费后重置） */
    private var _justReconnected = false

    /** 消费重连标记（一次性读取后重置） */
    fun consumeReconnectFlag(): Boolean {
        val result = _justReconnected
        _justReconnected = false
        return result
    }

    /** 发布锁：防止多个协程并发推送到 Supabase 导致旧状态覆盖新状态 */
    private val publishMutex = Mutex()

    fun getMySeatOrder(): Int {
        val room = _roomFlow.value ?: return 1
        val myId = _playerId.value
        val index = room.playerIds.indexOf(myId)
        return if (index >= 0) index + 1 else 1
    }

    fun getMyName(): String = _playerName.value

    /**
     * 判断当前是否轮到本地玩家操作。
     * 通过比较 GameState 中当前玩家的名字与本地玩家名字来识别回合归属。
     * 注意：不能依赖 seatOrder 比较，因为 GameEngine.initGame() 中 seatOrder 已被随机分配给不同名字。
     */
    fun isMyTurn(gameState: GameState?): Boolean {
        val state = gameState ?: return false
        if (state.players.isEmpty()) return false
        val currentPlayer = state.players[state.currentPlayerIndex]
        val myName = _playerName.value
        return currentPlayer.name == myName
    }

    // ============ 连接与认证 ============

    suspend fun connect(displayName: String): Boolean = runCatching {
        _playerName.value = displayName.ifBlank { "玩家" }
        if (_playerId.value.isBlank()) {
            _playerId.value = generatePlayerId()
            // 持久化 playerId，确保进程重启后可恢复
            prefs?.edit()?.putString(KEY_PLAYER_ID, _playerId.value)?.apply()
            LogUtil.d("OnlineManager", "生成并持久化 playerId: ${_playerId.value.take(8)}...")
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

    // ============ 房间操作（Supabase） ============

    suspend fun createRoom(maxPlayers: Int): RoomData {
        require(maxPlayers in 2..4) { "房间人数必须在 2-4 之间" }
        ensureConnected()

        val room = RoomData(
            roomCode = generateRoomCode(),
            ownerId = _playerId.value,
            playerIds = listOf(_playerId.value),
            playerNames = mapOf(_playerId.value to _playerName.value),
            maxPlayers = maxPlayers,
            status = RoomStatus.WAITING,
        )

        return try {
            val dto = RoomDto.fromRoomData(room)
            supabase.from("rooms").insert(dto)
            _roomFlow.value = room
            startSync(room.roomCode)
            room
        } catch (throwable: Throwable) {
            _roomFlow.value = null
            throw throwable
        }
    }

    suspend fun joinRoom(roomCode: String): RoomData {
        ensureConnected()
        val normalizedCode = roomCode.trim().uppercase()
        require(normalizedCode.length == 6) { "请输入 6 位房间码" }

        val dto = supabase.from("rooms").select {
            filter { eq("room_code", normalizedCode) }
        }.decodeSingleOrNull<RoomDto>()
            ?: error("房间不存在")

        val room = dto.toRoomData()
        require(!room.isFull || _playerId.value in room.playerIds) { "房间已满" }
        require(room.status != RoomStatus.PLAYING || _playerId.value in room.playerIds) { "游戏已开始，无法加入" }

        val isReconnecting = _playerId.value in room.playerIds

        // 如果已在房间中更新昵称，否则加入房间
        val joinedRoom = if (isReconnecting) {
            // 重连：从断开列表中移除自己
            val newDisconnected = room.disconnectedPlayerIds - _playerId.value
            room.copy(
                playerNames = room.playerNames + (_playerId.value to _playerName.value),
                disconnectedPlayerIds = newDisconnected,
            )
        } else {
            room.copy(
                playerIds = room.playerIds + _playerId.value,
                playerNames = room.playerNames + (_playerId.value to _playerName.value),
            )
        }

        // 更新 Supabase（区分重连和新加入）
        supabase.from("rooms").update({
            set("player_ids", joinedRoom.playerIds)
            set("player_names", joinedRoom.playerNames)
            if (isReconnecting) {
                set("disconnected_player_ids", joinedRoom.disconnectedPlayerIds)
            }
        }) {
            filter { eq("room_code", normalizedCode) }
        }

        _roomFlow.value = joinedRoom
        startSync(joinedRoom.roomCode)
        if (isReconnecting && room.status == RoomStatus.PLAYING) {
            _justReconnected = true
            _events.tryEmit("已重新连接游戏")
        }
        return joinedRoom
    }

    suspend fun startGame() {
        val room = _roomFlow.value ?: error("当前未加入任何房间")
        require(room.ownerId == _playerId.value) { "只有房主可以开始游戏" }
        require(room.playerIds.size >= 2) { "至少需要 2 名玩家才能开始" }

        val startedRoom = room.copy(status = RoomStatus.PLAYING)
        supabase.from("rooms").update({
            set("status", "PLAYING")
        }) {
            filter { eq("room_code", room.roomCode) }
        }

        _roomFlow.value = startedRoom
        _events.tryEmit("游戏开始！")
    }

    /**
     * 解散游戏：将房间状态设为 FINISHED，通知其他玩家。
     */
    suspend fun disbandGame() {
        val room = _roomFlow.value ?: return
        supabase.from("rooms").update({
            set("status", "FINISHED")
        }) {
            filter { eq("room_code", room.roomCode) }
        }
        _roomFlow.value = room.copy(status = RoomStatus.FINISHED)
        _events.tryEmit("游戏已解散")
    }

    fun resetLocalNavigation() {
        syncJob?.cancel()
        syncJob = null
        _roomFlow.value = null
        _syncedGameState.value = null
        lastPublishedVersion = -1
    }

    fun leaveRoom() {
        syncJob?.cancel()
        syncJob = null
        _connectionState.value = ConnectionStatus.DISCONNECTED
        _roomFlow.value = null
        _syncedGameState.value = null
        _playerId.value = ""
        lastPublishedVersion = -1
    }

    /**
     * 游戏中断开连接（保留 playerId 以便后续重连）。
     * 在 Supabase 上标记当前玩家为已断开，其他客户端会自动跳过该玩家的回合。
     */
    fun disconnectFromRoom() {
        val room = _roomFlow.value ?: return
        val myId = _playerId.value
        if (myId.isBlank()) return

        // 标记自己为已断开
        val updatedDisconnected = (room.disconnectedPlayerIds + myId).distinct()
        _roomFlow.value = room.copy(disconnectedPlayerIds = updatedDisconnected)

        // 异步通知服务器
        scope.launch {
            runCatching {
                supabase.from("rooms").update({
                    set("disconnected_player_ids", updatedDisconnected)
                }) {
                    filter { eq("room_code", room.roomCode) }
                }
            }
        }

        syncJob?.cancel()
        syncJob = null
        _connectionState.value = ConnectionStatus.DISCONNECTED
        _syncedGameState.value = null
        _events.tryEmit("已断开连接，可重新加入游戏")
    }

    suspend fun addLocalPlayer(playerName: String) {
        val room = _roomFlow.value ?: error("当前未加入任何房间")
        require(room.ownerId == _playerId.value) { "只有房主可以添加玩家" }
        require(room.status == RoomStatus.WAITING) { "游戏已开始，无法添加玩家" }
        require(!room.isFull) { "房间已满" }

        val newPlayerId = "local_${room.playerIds.size + 1}_${(1000..9999).random()}"
        val updatedRoom = room.copy(
            playerIds = room.playerIds + newPlayerId,
            playerNames = room.playerNames + (newPlayerId to playerName.ifBlank { "玩家${room.playerIds.size + 1}" }),
        )

        supabase.from("rooms").update({
            set("player_ids", updatedRoom.playerIds)
            set("player_names", updatedRoom.playerNames)
        }) {
            filter { eq("room_code", room.roomCode) }
        }

        _roomFlow.value = updatedRoom
    }

    // ============ 游戏状态同步 ============

    /**
     * 房主发布初始游戏状态到 Supabase。
     * 将 stateVersion 设为 1，确保高于所有客户端的默认值 (0)，
     * 这样非房主玩家通过 sync 轮询时能正确应用房主状态。
     */
    suspend fun publishInitialGameState(gameState: GameState) {
        publishMutex.withLock {
            gameState.stateVersion = 1
            doPublishGameState(gameState)
        }
    }

    /**
     * 当前玩家操作后推送游戏状态到 Supabase。
     * 只有当前回合玩家调用此方法。
     */
    suspend fun publishGameState(gameState: GameState) {
        publishMutex.withLock {
            // 版本守卫：如果已发布过更新的版本，跳过旧版本推送，防止竞态覆盖
            if (gameState.stateVersion <= lastPublishedVersion) return
            doPublishGameState(gameState)
        }
    }

    /**
     * 实际执行推送（需在 publishMutex 锁内调用）。
     */
    private suspend fun doPublishGameState(gameState: GameState) {
        val room = _roomFlow.value ?: error("当前未加入任何房间")
        val dto = GameStateDto(
            roomCode = room.roomCode,
            state = gameState,
            version = gameState.stateVersion,
        )
        supabase.from("game_states").upsert(dto)
        lastPublishedVersion = gameState.stateVersion
    }

    /**
     * 从 Supabase 加载游戏状态。
     */
    private suspend fun loadGameState(roomCode: String): GameState? {
        return try {
            supabase.from("game_states").select {
                filter { eq("room_code", roomCode) }
            }.decodeSingleOrNull<GameStateDto>()
                ?.state
        } catch (_: Exception) {
            null
        }
    }

    // ============ 辅助方法 ============

    private fun generateRoomCode(): String = buildString {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        repeat(6) { append(chars.random()) }
    }

    private fun ensureConnected() {
        require(_playerId.value.isNotBlank()) { "请先连接服务器" }
    }

    /**
     * 每秒轮询 Supabase 获取房间状态和游戏状态。
     * 游戏状态同步：当 version > 本地已处理版本时，推送给 UI。
     * 自动跳过：检测到当前回合玩家已断开时，自动推进回合。
     */
    private fun startSync(roomCode: String) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                _connectionState.value = ConnectionStatus.SYNCING
                runCatching {
                    val roomDto = supabase.from("rooms").select {
                        filter { eq("room_code", roomCode) }
                    }.decodeSingleOrNull<RoomDto>()
                    val gameStateDto = supabase.from("game_states").select {
                        filter { eq("room_code", roomCode) }
                    }.decodeSingleOrNull<GameStateDto>()
                    roomDto?.toRoomData() to gameStateDto
                }.onSuccess { (room, gameStateDto) ->
                    if (room != null) {
                        _roomFlow.value = room
                    }
                    // 游戏状态同步：仅当服务端版本比本地新时才更新
                    if (gameStateDto != null && gameStateDto.version > lastPublishedVersion) {
                        _syncedGameState.value = gameStateDto.state
                        lastPublishedVersion = gameStateDto.version
                    }

                    // 自动跳过断开玩家的回合（仅本客户端已连接时执行）
                    if (room != null && gameStateDto != null && _playerId.value !in room.disconnectedPlayerIds) {
                        autoSkipDisconnectedPlayerIfNeeded(room, gameStateDto)
                    }

                    _connectionState.value = ConnectionStatus.CONNECTED
                }.onFailure {
                    _connectionState.value = ConnectionStatus.CONNECTED
                    _events.tryEmit("同步失败: ${it.message}")
                }
                delay(1_000) // 每秒同步一次
            }
        }
    }

    /**
     * 如果当前回合玩家已断开连接且本客户端是已连接的玩家，自动跳过该回合。
     */
    private suspend fun autoSkipDisconnectedPlayerIfNeeded(room: RoomData, gameStateDto: GameStateDto) {
        val gameState = gameStateDto.state
        if (gameState.players.isEmpty()) return

        // 通过玩家名字找到对应的 playerId（因为 seatOrder 已被随机分配，不能再用 seatOrder 映射到 playerIds）
        val currentPlayerName = gameState.currentPlayer.name
        val currentPlayerId = room.playerNames.entries.find { it.value == currentPlayerName }?.key ?: return

        // 当前玩家未断开，无需跳过
        if (currentPlayerId !in room.disconnectedPlayerIds) return

        // 所有玩家都断开了（所有人退出），不跳过
        val hasConnectedPlayer = room.playerIds.any { it !in room.disconnectedPlayerIds }
        if (!hasConnectedPlayer) return

        // 跳过此玩家：推进到下一个玩家
        val newState = gameState.copy()
        newState.currentPlayerIndex = (gameState.currentPlayerIndex + 1) % gameState.players.size
        newState.currentPhase = GamePhase.BUY
        newState.turnStep = TurnStep.PHASE_1_BUY_MENU_OR_SHOP
        newState.settlementTip = 0
        newState.settlementMenuIncome = 0
        newState.settlementShopIncome = 0
        newState.menuBoughtThisTurn = false
        newState.shopPlacedThisTurn = false
        newState.stateVersion = gameState.stateVersion + 1

        LogUtil.d("OnlineManager", "自动跳过断开玩家: ${gameState.currentPlayer.name} -> ${newState.currentPlayer.name}")

        // 临时设置 roomFlow 用于 publishGameState
        val savedRoom = _roomFlow.value
        _roomFlow.value = room
        try {
            publishGameState(newState)
            _events.tryEmit("${gameState.currentPlayer.name} 已断开，自动跳过回合")
        } catch (_: Exception) {
            // 其他客户端可能已经跳过了，忽略
        } finally {
            _roomFlow.value = savedRoom
        }
    }
}
