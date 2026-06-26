package com.dasong.commerce.online

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnlineUiState())
    val uiState: StateFlow<OnlineUiState> = _uiState.asStateFlow()

    val connectionState = OnlineManager.connectionState
    val currentRoom = OnlineManager.roomFlow
    val events = OnlineManager.events

    private val _viewModelEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val viewModelEvents: SharedFlow<String> = _viewModelEvents.asSharedFlow()

    private val _shouldNavigateToGame = MutableStateFlow(false)
    val shouldNavigateToGame: StateFlow<Boolean> = _shouldNavigateToGame.asStateFlow()

    private val _playerNames = MutableStateFlow<List<String>>(emptyList())
    val playerNames: StateFlow<List<String>> = _playerNames.asStateFlow()

    private val _playerCount = MutableStateFlow(2)
    val playerCount: StateFlow<Int> = _playerCount.asStateFlow()

    fun updatePlayerName(name: String) {
        _uiState.value = _uiState.value.copy(playerName = name)
    }

    fun updateRoomCodeInput(code: String) {
        _uiState.value = _uiState.value.copy(
            roomCodeInput = code.take(6).uppercase()
        )
    }

    fun connect() {
        viewModelScope.launch {
            val name = _uiState.value.playerName.ifBlank { "玩家" }
            OnlineManager.connect(name)
        }
    }

    fun createRoom(maxPlayers: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 先连接
                OnlineManager.connect(_uiState.value.playerName.ifBlank { "玩家" })
                val room = OnlineManager.createRoom(maxPlayers)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentRoom = room,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "创建房间失败",
                )
            }
        }
    }

    fun joinRoom() {
        viewModelScope.launch {
            val code = _uiState.value.roomCodeInput
            if (code.length != 6) {
                _viewModelEvents.emit("请输入 6 位房间码")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                OnlineManager.connect(_uiState.value.playerName.ifBlank { "玩家" })
                val room = OnlineManager.joinRoom(code)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentRoom = room,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加入房间失败",
                )
            }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            try {
                OnlineManager.startGame()
                // 收集玩家名称和人数，用于传递给 GameScreen
                val room = OnlineManager.roomFlow.value
                if (room != null) {
                    val names = room.playerIds.map { id ->
                        room.playerNames[id] ?: "玩家${room.playerIds.indexOf(id) + 1}"
                    }
                    _playerNames.value = names
                    _playerCount.value = room.playerIds.size
                    _shouldNavigateToGame.value = true
                }
            } catch (e: Exception) {
                _viewModelEvents.emit(e.message ?: "开始游戏失败")
            }
        }
    }

    fun leaveRoom() {
        OnlineManager.leaveRoom()
        _uiState.value = OnlineUiState()
        _shouldNavigateToGame.value = false
        _playerNames.value = emptyList()
    }

    /**
     * 房主在同一设备上直接添加玩家（Demo 模式）
     */
    fun addLocalPlayer(name: String) {
        viewModelScope.launch {
            try {
                OnlineManager.addLocalPlayer(name)
            } catch (e: Exception) {
                _viewModelEvents.emit(e.message ?: "添加玩家失败")
            }
        }
    }

    fun resetNavigationFlag() {
        _shouldNavigateToGame.value = false
    }

    val isRoomOwner: Boolean
        get() = currentRoom.value?.ownerId == OnlineManager.playerId.value
}
