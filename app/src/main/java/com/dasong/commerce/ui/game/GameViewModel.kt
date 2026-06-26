package com.dasong.commerce.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasong.commerce.engine.*
import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import com.dasong.commerce.online.OnlineManager
import com.dasong.commerce.online.RoomStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameEngine: GameEngine
) : ViewModel() {

    val gameState: StateFlow<GameState?> = gameEngine.gameState

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

    fun initGame(playerCount: Int, playerNames: List<String> = emptyList()) {
        viewModelScope.launch {
            // 联机模式：优先使用传入的玩家名称，否则从 OnlineManager 获取
            val names = if (playerNames.isNotEmpty()) {
                playerNames
            } else {
                val room = OnlineManager.roomFlow.value
                if (room != null && room.status == RoomStatus.PLAYING) {
                    room.playerIds.map { id ->
                        room.playerNames[id] ?: "玩家${room.playerIds.indexOf(id) + 1}"
                    }
                } else {
                    emptyList()
                }
            }
            gameEngine.initGame(playerCount, names)
        }
    }

    fun buyMenuCard(playerId: Int, card: MenuCard) {
        gameEngine.buyMenuCard(playerId, card)
    }

    fun placeShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) {
        gameEngine.placeShopCard(playerId, shop, foundationIndex)
    }

    fun buildShopHouse(playerId: Int, foundationIndex: Int) {
        gameEngine.buildShopHouse(playerId, foundationIndex)
    }

    fun endBuyPhase(playerId: Int) {
        gameEngine.endBuyPhase(playerId)
    }

    fun removeMenu(playerId: Int, card: MenuCard) {
        gameEngine.removeMenu(playerId, card)
    }

    fun skipPreparePhase() {
        gameEngine.skipPreparePhase()
    }

    fun selectGuest(playerId: Int, queuePosition: Int) {
        gameEngine.selectGuest(playerId, queuePosition)

        val menuResult = gameEngine.settleMenuIncome(playerId)

        val state = gameState.value
        // 门可罗雀：需要让玩家选择一个店铺结算
        if (state?.activeEvent?.effect == EventEffect.MEN_KE_LUO_QUE) {
            val shops = gameEngine.getMenKeLuoQueShops(playerId)
            if (shops.isEmpty()) {
                // 没有可结算的店铺，直接跳过
                val shopResult = gameEngine.settleShopIncome(playerId)
                completeSettlement(menuResult, shopResult)
                // refreshGuestQueue 推迟到 dismissSettlement 中执行
            } else {
                _menKeLuoQueShops.value = shops
                _showMenKeLuoQueDialog.value = true
                // 暂存 menuResult 用于后续完成结算
                _pendingMenuResult = menuResult
            }
        } else {
            val shopResult = gameEngine.settleShopIncome(playerId)
            completeSettlement(menuResult, shopResult)
            // refreshGuestQueue 推迟到 dismissSettlement 中执行
        }
    }

    private var _pendingMenuResult: MenuSettlementResult? = null

    fun onMenKeLuoQueShopSelected(foundationIndex: Int) {
        val state = gameState.value ?: return
        val playerId = state.currentPlayer.id
        val menuResult = _pendingMenuResult ?: return

        val shopResult = gameEngine.settleShopIncome(playerId, selectedShopIndex = foundationIndex)
        completeSettlement(menuResult, shopResult)
        // refreshGuestQueue 推迟到 dismissSettlement 中执行

        _pendingMenuResult = null
        _showMenKeLuoQueDialog.value = false
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
        proceedAfterRefresh()
    }

    fun confirmEventAnnouncement() {
        gameEngine.confirmEventAnnouncement()
        proceedAfterRefresh()
    }

    /**
     * 翻牌后的统一后续处理：
     * - 如果有事件待公告 → 等 UI 弹出 EventAnnouncementDialog
     * - 如果没有事件 → 检查胜负 → 显示回合切换弹窗
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
            return
        }
        _showTurnTransition.value = true
    }

    fun confirmTurnTransition() {
        _showTurnTransition.value = false
        _settlementResult.value = null
        gameEngine.endTurn()
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
