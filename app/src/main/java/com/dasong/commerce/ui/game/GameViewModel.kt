package com.dasong.commerce.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dasong.commerce.engine.*
import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.model.card.ShopCard
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

    fun initGame(playerCount: Int) {
        viewModelScope.launch {
            gameEngine.initGame(playerCount)
        }
    }

    fun buyMenuCard(playerId: Int, card: MenuCard) {
        gameEngine.buyMenuCard(playerId, card)
    }

    fun buyShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) {
        gameEngine.buyShopCard(playerId, shop, foundationIndex)
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
        val shopResult = gameEngine.settleShopIncome(playerId)

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

        gameEngine.refreshGuestQueue()
    }

    fun endTurn() {
        val state = gameState.value ?: return
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

    fun dismissSettlement() {
        _settlementResult.value = null
        endTurn()
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
