package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*

data class GameState(
    val menuPool: MenuPool = MenuPool(),
    val shopPool: ShopPool = ShopPool(),
    val guestDeck: MutableList<DeckCard> = mutableListOf(),
    val guestQueue: MutableList<GuestCard> = mutableListOf(),
    var activeEvent: EventCard? = null, // 当前生效的持续事件牌
    var announceEvent: EventCard? = null, // 当前需要公告的事件牌（UI展示用）
    val players: List<PlayerState> = emptyList(),
    var currentPlayerIndex: Int = 0,
    var currentPhase: GamePhase = GamePhase.BUY,
    var turnStep: TurnStep = TurnStep.PHASE_1_BUY_MENU_OR_SHOP,
    var settlementTip: Int = 0,
    var settlementMenuIncome: Int = 0,
    var settlementShopIncome: Int = 0,
    var selectedGuest: GuestCard? = null, // 当前正在结算的客人
    var winner: String? = null, // 胜者名字
    var menuBoughtThisTurn: Boolean = false, // 本回合是否已购买菜单牌
    var shopPlacedThisTurn: Boolean = false, // 本回合是否已放置店铺牌
    var pendingMenKeLuoQue: Boolean = false, // 门可罗雀：等待玩家选择店铺
    var stateVersion: Long = 0
) {
    val currentPlayer: PlayerState get() = players[currentPlayerIndex]
}
