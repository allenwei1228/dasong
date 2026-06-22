package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*

data class GameState(
    val menuPool: MenuPool = MenuPool(),
    val shopPool: ShopPool = ShopPool(),
    val guestDeck: MutableList<GuestCard> = mutableListOf(),
    val guestQueue: MutableList<GuestCard> = mutableListOf(),
    var activeEvent: EventCard? = null,
    val players: List<PlayerState> = emptyList(),
    var currentPlayerIndex: Int = 0,
    var currentPhase: GamePhase = GamePhase.BUY,
    var turnStep: TurnStep = TurnStep.PHASE_1_BUY_MENU_OR_SHOP,
    var settlementTip: Int = 0,
    var settlementMenuIncome: Int = 0,
    var settlementShopIncome: Int = 0,
    var stateVersion: Long = 0
) {
    val currentPlayer: PlayerState get() = players[currentPlayerIndex]
}
