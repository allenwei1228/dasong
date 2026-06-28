package com.dasong.commerce.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasong.commerce.engine.*
import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import com.dasong.commerce.online.OnlineManager
import com.dasong.commerce.online.RoomStatus
import com.dasong.commerce.util.DiceRoller
import com.dasong.commerce.util.LogUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine
) : ViewModel() {

    val gameState: StateFlow<GameState?> = gameEngine.gameState

    /** 是否为联机模式 */
    private val _isOnlineMode = MutableStateFlow(false)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    /** 联机模式下，是否轮到本地玩家操作 */
    private val _isMyTurn = MutableStateFlow(true)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn.asStateFlow()

    /** 联机模式下，等待其他玩家操作的提示文本 */
    private val _waitingMessage = MutableStateFlow("")
    val waitingMessage: StateFlow<String> = _waitingMessage.asStateFlow()

    private val _showTurnTransition = MutableStateFlow(false)
    val showTurnTransition: StateFlow<Boolean> = _showTurnTransition.asStateFlow()

    private val _settlementResult = MutableStateFlow<SettlementDisplayData?>(null)
    val settlementResult: StateFlow<SettlementDisplayData?> = _settlementResult.asStateFlow()

    private val _winner = MutableStateFlow<String?>(null)
    val winner: StateFlow<String?> = _winner.asStateFlow()

    private val _showMenKeLuoQueDialog = MutableStateFlow(false)
    val showMenKeLuoQueDialog: StateFlow<Boolean> = _showMenKeLuoQueDialog.asStateFlow()

    private val _menKeLuoQueShops = MutableStateFlow<List<Foundation>>(emptyList())
    val menKeLuoQueShops: StateFlow<List<Foundation>> = _menKeLuoQueShops.asStateFlow()

    /** 回合切换后，弹窗提醒新玩家"该我操作了" */
    private val _showMyTurnReminder = MutableStateFlow(false)
    val showMyTurnReminder: StateFlow<Boolean> = _showMyTurnReminder.asStateFlow()

    /** 骰子投掷弹窗 */
    private val _showDiceRoll = MutableStateFlow(false)
    val showDiceRoll: StateFlow<Boolean> = _showDiceRoll.asStateFlow()

    /** 当前等待投掷的骰子数量 */
    private val _pendingDiceCount = MutableStateFlow(0)
    val pendingDiceCount: StateFlow<Int> = _pendingDiceCount.asStateFlow()

    /** 骰子来源说明（一品菜单/卦肆） */
    private val _diceSources = MutableStateFlow<List<String>>(emptyList())
    val diceSources: StateFlow<List<String>> = _diceSources.asStateFlow()

    /** 暂存当前玩家的操作上下文（用于骰子完成后继续结算） */
    private var _pendingPlayerId: Int = -1
    private var _pendingMenuResult: MenuSettlementResult? = null

    /** 是否应该退出到主页 */
    private val _shouldExitToHome = MutableStateFlow(false)
    val shouldExitToHome: StateFlow<Boolean> = _shouldExitToHome.asStateFlow()

    /** 游戏被解散的提示消息（联机模式下其他玩家退出时触发） */
    private val _disbandMessage = MutableStateFlow<String?>(null)
    val disbandMessage: StateFlow<String?> = _disbandMessage.asStateFlow()

    /** 标记是否是自己主动退出的（避免自己也弹出解散提示） */
    private var _isExitingSelf = false

    /** 游戏开始通知弹窗：提示玩家序号和初始资金 */
    private val _showGameStartNotification = MutableStateFlow(false)
    val showGameStartNotification: StateFlow<Boolean> = _showGameStartNotification.asStateFlow()

    private val _gameStartInfo = MutableStateFlow<GameStartInfo?>(null)
    val gameStartInfo: StateFlow<GameStartInfo?> = _gameStartInfo.asStateFlow()

    /** 是否已展示过游戏开始通知 */
    private var _hasShownGameStart = false

    /** 重连摘要弹窗 */
    private val _showReconnectSummary = MutableStateFlow(false)
    val showReconnectSummary: StateFlow<Boolean> = _showReconnectSummary.asStateFlow()

    private val _reconnectSummaryHistory = MutableStateFlow<List<String>>(emptyList())
    val reconnectSummaryHistory: StateFlow<List<String>> = _reconnectSummaryHistory.asStateFlow()

    /** 是否为重连模式 */
    private var _isReconnecting = false

    fun initGame(playerCount: Int, playerNames: List<String> = emptyList()) {
        viewModelScope.launch {
            // 检测是否为联机模式
            val room = OnlineManager.roomFlow.value
            val online = room != null && room.status == RoomStatus.PLAYING
            _isOnlineMode.value = online

            if (online) {
                // 重连判定：优先使用 OnlineManager 的确定性标记（joinRoom 时已知），
                // 避免依赖 syncedState 的竞态条件（sync 可能尚未拉取到服务端状态）
                val justReconnected = OnlineManager.consumeReconnectFlag()
                val syncedState = OnlineManager.syncedGameState.value
                val isReconnection = justReconnected ||
                        (room != null &&
                        OnlineManager.playerId.value in room.playerIds &&
                        gameEngine.gameState.value == null &&
                        syncedState != null &&
                        syncedState.stateVersion > 1)

                if (isReconnection) {
                    // 重连模式：不从零初始化，等待 sync 拉取服务端状态
                    LogUtil.d("GameViewModel", "重连模式：等待同步服务端游戏状态...")
                    _hasShownGameStart = true // 重连不弹开始通知
                    _isReconnecting = true
                    startOnlineSync()
                } else {
                    // 正常联机初始化
                    val names = if (playerNames.isNotEmpty()) {
                        playerNames
                    } else if (room != null) {
                        room.playerIds.map { id ->
                            room.playerNames[id] ?: "玩家${room.playerIds.indexOf(id) + 1}"
                        }
                    } else {
                        emptyList()
                    }
                    gameEngine.initGame(playerCount, names)

                    // 房主发布初始游戏状态到服务器
                    val isHost = room?.ownerId == OnlineManager.playerId.value
                    if (isHost) {
                        val state = gameEngine.gameState.value ?: return@launch
                        OnlineManager.publishInitialGameState(state)
                        // 房主本地立即弹窗通知
                        triggerGameStartNotification()
                    }

                    // 启动联机同步监听
                    startOnlineSync()
                }
            } else {
                // 单机模式：正常初始化
                gameEngine.initGame(playerCount, playerNames)
                // 单机热座模式：弹窗通知第一位玩家
                triggerGameStartNotification()
            }

            // 更新回合归属
            updateMyTurnState()
        }
    }

    /**
     * 弹出游戏开始通知：展示所有玩家的 seatOrder 分配和初始资金。
     * 联机模式下每位玩家在自己的设备上看到相同的内容（由房主的 shuffle 决定）。
     */
    private fun triggerGameStartNotification() {
        if (_hasShownGameStart) return
        _hasShownGameStart = true

        val state = gameEngine.gameState.value ?: return
        if (state.players.isEmpty()) return

        val assignments = state.players.sortedBy { it.seatOrder }.map { player ->
            PlayerAssignment(
                seatOrder = player.seatOrder,
                name = player.name,
                funds = player.funds
            )
        }
        _gameStartInfo.value = GameStartInfo(players = assignments)
        _showGameStartNotification.value = true
    }

    /** 关闭游戏开始通知弹窗 */
    fun dismissGameStartNotification() {
        _showGameStartNotification.value = false
    }

    /**
     * 启动联机同步：监听服务器推送的游戏状态更新。
     * 非当前回合玩家通过此机制接收其他玩家的操作并刷新 UI。
     */
    private fun startOnlineSync() {
        viewModelScope.launch {
            OnlineManager.syncedGameState.collect { remoteState ->
                if (remoteState == null) return@collect
                // 如果服务端版本比本地新，应用远程状态
                val localState = gameEngine.gameState.value
                if (localState == null || remoteState.stateVersion > localState.stateVersion) {
                    gameEngine.applyRemoteState(remoteState)
                    updateMyTurnState()
                    // 非房主玩家：首次收到房主发布的游戏状态后弹窗通知
                    if (!_hasShownGameStart) {
                        triggerGameStartNotification()
                    }
                    // 重连成功：展示离开期间的游戏摘要
                    if (_isReconnecting && remoteState.turnHistory.isNotEmpty()) {
                        _reconnectSummaryHistory.value = remoteState.turnHistory.toList()
                        _showReconnectSummary.value = true
                        _isReconnecting = false
                        LogUtil.d("GameViewModel", "重连成功，展示离开期间摘要: ${remoteState.turnHistory.size} 条记录")
                    }
                }
            }
        }
        // 监听房间状态变化：检测游戏解散
        viewModelScope.launch {
            OnlineManager.roomFlow.collect { room ->
                if (room?.status == RoomStatus.FINISHED && _isOnlineMode.value && !_isExitingSelf) {
                    _disbandMessage.value = "有玩家退出了游戏，本局已解散。"
                }
            }
        }
    }

    /**
     * 更新当前回合归属状态。
     */
    private fun updateMyTurnState() {
        val state = gameEngine.gameState.value ?: return
        val myTurn = OnlineManager.isMyTurn(state)
        val prevMyTurn = _isMyTurn.value
        _isMyTurn.value = myTurn

        // 联机模式：回合刚从其他玩家切换到我 → 弹窗提醒
        if (_isOnlineMode.value && !prevMyTurn && myTurn) {
            _showMyTurnReminder.value = true
        }

        if (!myTurn && _isOnlineMode.value) {
            val currentPlayer = state.currentPlayer
            _waitingMessage.value = "等待 ${currentPlayer.name} 操作中……"
        } else {
            _waitingMessage.value = ""
        }
    }

    /**
     * 联机模式下，操作后推送状态到服务器。
     */
    private fun syncToServerIfOnline() {
        if (!_isOnlineMode.value) return
        val state = gameEngine.gameState.value ?: return
        viewModelScope.launch {
            try {
                OnlineManager.publishGameState(state)
            } catch (e: Exception) {
                LogUtil.e("GameViewModel", "游戏状态同步失败: ${e.message}")
            }
        }
    }

    // ========== 阶段1：购买阶段 ==========

    fun buyMenuCard(playerId: Int, card: MenuCard) {
        gameEngine.buyMenuCard(playerId, card)
        updateMyTurnState()
        syncToServerIfOnline()
    }

    fun placeShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) {
        gameEngine.placeShopCard(playerId, shop, foundationIndex)
        updateMyTurnState()
        syncToServerIfOnline()
    }

    fun buildShopHouse(playerId: Int, foundationIndex: Int) {
        gameEngine.buildShopHouse(playerId, foundationIndex)
        updateMyTurnState()
        syncToServerIfOnline()
    }

    fun endBuyPhase(playerId: Int) {
        gameEngine.endBuyPhase(playerId)
        updateMyTurnState()
        syncToServerIfOnline()
    }

    // ========== 阶段2：备菜阶段 ==========

    fun removeMenu(playerId: Int, card: MenuCard) {
        gameEngine.removeMenu(playerId, card)
        updateMyTurnState()
        syncToServerIfOnline()
    }

    fun skipPreparePhase() {
        gameEngine.skipPreparePhase()
        updateMyTurnState()
        syncToServerIfOnline()
    }

    // ========== 阶段3：招待阶段 ==========

    fun selectGuest(playerId: Int, queuePosition: Int) {
        gameEngine.selectGuest(playerId, queuePosition)
        updateMyTurnState()
        syncToServerIfOnline()

        // 检查是否需要骰子交互
        val diceCount = gameEngine.estimateDiceCount(playerId)
        _pendingPlayerId = playerId

        if (diceCount > 0) {
            // 需要骰子交互：弹出骰子投掷弹窗，等玩家投完后继续
            _pendingDiceCount.value = diceCount
            _diceSources.value = gameEngine.getDiceSources(playerId)
            _showDiceRoll.value = true
        } else {
            // 无需骰子，直接结算
            proceedWithSettlement(playerId, emptyList())
        }
    }

    /**
     * 骰子投掷完成后调用，使用投掷结果继续结算。
     */
    fun onDiceRollComplete(diceValues: List<Int>) {
        _showDiceRoll.value = false
        val playerId = _pendingPlayerId
        _pendingPlayerId = -1
        if (playerId < 0) return

        proceedWithSettlement(playerId, diceValues)
    }

    /**
     * 继续结算流程：用骰子结果创建临时 roller，然后按顺序结算菜单和店铺。
     */
    private fun proceedWithSettlement(playerId: Int, diceValues: List<Int>) {
        // 创建骰子队列：按顺序消费值
        var diceIndex = 0
        val diceRoller: () -> Int = {
            if (diceIndex < diceValues.size) {
                diceValues[diceIndex++]
            } else {
                DiceRoller.roll() // fallback
            }
        }

        val menuResult = gameEngine.settleMenuIncome(playerId, diceRoller)
        updateMyTurnState()
        syncToServerIfOnline()

        val state = gameState.value
        // 门可罗雀：需要让玩家选择一个店铺结算
        if (state?.activeEvent?.effect == EventEffect.MEN_KE_LUO_QUE) {
            val shops = gameEngine.getMenKeLuoQueShops(playerId)
            if (shops.isEmpty()) {
                // 没有可结算的店铺，直接跳过
                val shopResult = gameEngine.settleShopIncome(playerId, diceRoller = diceRoller)
                completeSettlement(menuResult, shopResult)
                updateMyTurnState()
                syncToServerIfOnline()
            } else {
                _menKeLuoQueShops.value = shops
                _showMenKeLuoQueDialog.value = true
                // 暂存 menuResult 和 diceRoller 用于后续完成结算
                _pendingMenuResult = menuResult
                _pendingDiceRoller = diceRoller
            }
        } else {
            val shopResult = gameEngine.settleShopIncome(playerId, diceRoller = diceRoller)
            completeSettlement(menuResult, shopResult)
            updateMyTurnState()
            syncToServerIfOnline()
        }
    }

    /** 暂存骰子 roller（门可罗雀场景传递用） */
    private var _pendingDiceRoller: (() -> Int)? = null

    fun onMenKeLuoQueShopSelected(foundationIndex: Int) {
        val state = gameState.value ?: return
        val playerId = state.currentPlayer.id
        val menuResult = _pendingMenuResult ?: return
        val diceRoller = _pendingDiceRoller ?: { DiceRoller.roll() }

        val shopResult = gameEngine.settleShopIncome(
            playerId,
            selectedShopIndex = foundationIndex,
            diceRoller = diceRoller
        )
        completeSettlement(menuResult, shopResult)

        _pendingMenuResult = null
        _pendingDiceRoller = null
        _showMenKeLuoQueDialog.value = false

        updateMyTurnState()
        syncToServerIfOnline()
    }

    private fun completeSettlement(menuResult: MenuSettlementResult, shopResult: ShopSettlementResult) {
        _settlementResult.value = SettlementDisplayData(
            tip = gameState.value?.settlementTip ?: 0,
            menuIncome = menuResult.totalIncome,
            menuCards = menuResult.cardsDrawn,
            diceResults = menuResult.diceResults,
            shopIncome = shopResult.totalIncome,
            shopActivations = shopResult.activatedShops,
            totalIncome = (gameState.value?.settlementTip ?: 0) +
                    menuResult.totalIncome + shopResult.totalIncome
        )
    }

    // ========== 结算弹窗关闭 → 翻牌 → 事件公告/回合切换 ==========

    fun dismissSettlement() {
        _settlementResult.value = null
        gameEngine.refreshGuestQueue()
        updateMyTurnState()
        syncToServerIfOnline()
        proceedAfterRefresh()
    }

    fun confirmEventAnnouncement() {
        gameEngine.confirmEventAnnouncement()
        updateMyTurnState()
        syncToServerIfOnline()
        proceedAfterRefresh()
    }

    /**
     * 翻牌后的统一后续处理：
     * - 如果有事件待公告 → 等 UI 弹出 EventAnnouncementDialog
     * - 如果没有事件 → 检查胜负 → 切换回合
     */
    private fun proceedAfterRefresh() {
        val state = gameState.value ?: return
        if (state.announceEvent != null) {
            // UI 会检测到 announceEvent 并弹出事件公告弹窗，等待 confirmEventAnnouncement
            return
        }
        // 没有待公告的事件，进入回合结束流程
        val winChecker = WinConditionChecker()
        if (winChecker.checkWin(state.currentPlayer)) {
            _winner.value = state.currentPlayer.name
            syncToServerIfOnline()
            return
        }
        // 联机模式：直接切换回合并同步；单机模式：显示回合切换弹窗
        if (_isOnlineMode.value) {
            performOnlineTurnEnd()
        } else {
            _showTurnTransition.value = true
        }
    }

    /**
     * 联机模式：结算完成后自动切换回合并推送到服务器。
     */
    private fun performOnlineTurnEnd() {
        _settlementResult.value = null
        gameEngine.endTurn()
        updateMyTurnState()
        syncToServerIfOnline()
    }

    /**
     * 单机模式：回合切换确认。
     */
    fun confirmTurnTransition() {
        _showTurnTransition.value = false
        _settlementResult.value = null
        gameEngine.endTurn()
        updateMyTurnState()
        // 单机模式：回合切换后提醒新玩家
        _showMyTurnReminder.value = true
    }

    /** 关闭"该我操作了"提醒弹窗 */
    fun dismissMyTurnReminder() {
        _showMyTurnReminder.value = false
    }

    /** 退出游戏：单机模式直接退出到主页（联机模式退出在 GameScreen 层处理） */
    fun exitGame() {
        _shouldExitToHome.value = true
    }

    /** 关闭解散提示弹窗，导航回主页 */
    fun dismissDisbandMessage() {
        _disbandMessage.value = null
        _shouldExitToHome.value = true
    }

    /** 关闭重连摘要弹窗 */
    fun dismissReconnectSummary() {
        _showReconnectSummary.value = false
    }
}

data class SettlementDisplayData(
    val tip: Int,
    val menuIncome: Int,
    val menuCards: List<MenuCard>,
    val diceResults: List<DiceResult>,
    val shopIncome: Int,
    val shopActivations: List<ShopActivation>,
    val totalIncome: Int
)

/** 单个玩家的分配信息 */
data class PlayerAssignment(
    val seatOrder: Int,
    val name: String,
    val funds: Int
)

/** 游戏开始通知信息：包含所有玩家随机分配后的顺序和初始资金 */
data class GameStartInfo(
    val players: List<PlayerAssignment>
)

