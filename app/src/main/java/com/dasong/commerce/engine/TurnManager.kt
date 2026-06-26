package com.dasong.commerce.engine

import com.dasong.commerce.model.card.GamePhase
import com.dasong.commerce.util.LogUtil
import com.dasong.commerce.model.card.TurnStep

class TurnManager {

    companion object {
        private const val TAG = "TurnManager"
    }

    fun advanceToNextPhase(state: GameState) {
        val prevPhase = state.currentPhase
        val nextPhase = when (prevPhase) {
            GamePhase.BUY -> GamePhase.PREPARE
            GamePhase.PREPARE -> GamePhase.SERVE
            GamePhase.SERVE -> GamePhase.SERVE
        }
        state.currentPhase = nextPhase
        state.turnStep = when (state.currentPhase) {
            GamePhase.BUY -> TurnStep.PHASE_1_BUY_MENU_OR_SHOP
            GamePhase.PREPARE -> TurnStep.PHASE_2_PREPARE_OPTIONAL
            GamePhase.SERVE -> TurnStep.PHASE_3_SELECT_GUEST
        }
        LogUtil.d(TAG, "阶段转换: $prevPhase -> $nextPhase | turnStep=${state.turnStep} | 当前玩家=${state.currentPlayer.name}")
    }

    fun advanceToNextPlayer(state: GameState) {
        val prevPlayer = state.currentPlayer.name
        val prevPhase = state.currentPhase
        state.currentPlayerIndex = (state.currentPlayerIndex + 1) % state.players.size
        state.currentPhase = GamePhase.BUY
        state.turnStep = TurnStep.PHASE_1_BUY_MENU_OR_SHOP
        state.settlementTip = 0
        state.settlementMenuIncome = 0
        state.settlementShopIncome = 0
        state.menuBoughtThisTurn = false // 重置菜单购买标记
        state.shopPlacedThisTurn = false // 重置店铺放置标记
        LogUtil.d(TAG, "玩家切换: $prevPlayer -> ${state.currentPlayer.name} | 阶段重置: $prevPhase -> BUY")
    }
}
