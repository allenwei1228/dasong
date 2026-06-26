package com.dasong.commerce.online

import com.dasong.commerce.BuildConfig
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

object OnlineManager {

    enum class ConnectionStatus {
        DISCONNECTED,
        CONNECTED,
        SYNCING,
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

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

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var syncJob: kotlinx.coroutines.Job? = null

    fun getMySeatOrder(): Int {
        val room = _roomFlow.value ?: return 1
        val myId = _playerId.value
        val index = room.playerIds.indexOf(myId)
        return if (index >= 0) index + 1 else 1
    }

    fun getMyName(): String = _playerName.value

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

        // 如果已在房间中更新昵称，否则加入房间
        val joinedRoom = if (_playerId.value in room.playerIds) {
            room.copy(playerNames = room.playerNames + (_playerId.value to _playerName.value))
        } else {
            room.copy(
                playerIds = room.playerIds + _playerId.value,
                playerNames = room.playerNames + (_playerId.value to _playerName.value),
            )
        }

        // 更新 Supabase
        supabase.from("rooms").update({
            set("player_ids", joinedRoom.playerIds)
            set("player_names", joinedRoom.playerNames)
        }) {
            filter { eq("room_code", normalizedCode) }
        }

        _roomFlow.value = joinedRoom
        startSync(joinedRoom.roomCode)
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

    fun resetLocalNavigation() {
        syncJob?.cancel()
        syncJob = null
        _roomFlow.value = null
    }

    fun leaveRoom() {
        syncJob?.cancel()
        syncJob = null
        _connectionState.value = ConnectionStatus.DISCONNECTED
        _roomFlow.value = null
        _playerId.value = ""
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

    // ============ 辅助方法 ============

    private fun generateRoomCode(): String = buildString {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        repeat(6) { append(chars.random()) }
    }

    private fun ensureConnected() {
        require(_playerId.value.isNotBlank()) { "请先连接服务器" }
    }

    /**
     * 每秒轮询 Supabase 获取房间最新状态。
     */
    private fun startSync(roomCode: String) {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                _connectionState.value = ConnectionStatus.SYNCING
                runCatching {
                    supabase.from("rooms").select {
                        filter { eq("room_code", roomCode) }
                    }.decodeSingleOrNull<RoomDto>()
                        ?.toRoomData()
                }.onSuccess { room ->
                    if (room != null) {
                        _roomFlow.value = room
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
}
