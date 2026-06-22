package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import com.dasong.commerce.util.shuffle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameEngine(
    private val deckManager: DeckManager = DeckManager(),
    private val settlementEngine: SettlementEngine = SettlementEngine(),
    private val eventExecutor: EventExecutor = EventExecutor(),
    private val winChecker: WinConditionChecker = WinConditionChecker(),
    private val turnManager: TurnManager = TurnManager()
) {
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    fun initGame(playerCount: Int) {
        require(playerCount in 2..4) { "玩家数量必须在2-4之间" }

        val menuPool = deckManager.initMenuPool()
        val shopPool = deckManager.initShopPool()
        val combinedDeck = deckManager.createCombinedDeck()

        // Draw initial guest queue (4 guests, skip events)
        val queue = mutableListOf<GuestCard>()
        val deck = mutableListOf<GuestCard>()
        val pendingEvents = mutableListOf<EventCard>()

        for (item in combinedDeck) {
            when (item) {
                is GuestCard -> {
                    if (queue.size < 4) queue.add(item)
                    else deck.add(item)
                }
                is EventCard -> pendingEvents.add(item)
            }
        }

        // Create players
        val players = (1..playerCount).map { i ->
            val name = "玩家$i"
            val menuCards = deckManager.getInitialMenuForPlayer()
            PlayerState(
                id = i,
                name = name,
                seatOrder = i,
                funds = deckManager.getStartingFunds(i),
                refinedChamber = menuCards.toMutableList(),
                kitchen = mutableListOf()
            )
        }

        val state = GameState(
            menuPool = menuPool,
            shopPool = shopPool,
            guestDeck = deck,
            guestQueue = queue,
            activeEvent = null,
            players = players,
            currentPlayerIndex = 0,
            currentPhase = GamePhase.BUY,
            turnStep = TurnStep.PHASE_1_BUY_MENU_OR_SHOP
        )

        _gameState.value = state
    }

    // ========== 阶段1：购买阶段 ==========

    fun buyMenuCard(playerId: Int, card: MenuCard) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.BUY) { "当前不是购买阶段" }
        require(player.funds >= card.cost) { "资金不足" }

        player.funds -= card.cost
        player.kitchen.add(card)

        // Remove from menu pool
        state.menuPool.getPile(card.grade).remove(card)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun buyShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.BUY) { "当前不是购买阶段" }

        val foundation = player.foundations[foundationIndex]
        require(foundation.shopCard == null) { "该地基已被占用" }

        val clearCost = foundation.clearCost
        val totalCost = clearCost + shop.buildCost
        require(player.funds >= totalCost) { "资金不足：需要${totalCost}两（清理${clearCost}两 + 建造${shop.buildCost}两）" }

        player.funds -= totalCost
        foundation.shopCard = shop
        foundation.hasModel = true

        // Remove from shop pool and replenish
        state.shopPool.available.remove(shop)
        val newShop = deckManager.drawShopFromPool(state.shopPool)
        if (newShop != null) {
            state.shopPool.available.add(newShop)
        }
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun endBuyPhase(playerId: Int) {
        val state = requireState()
        require(state.currentPhase == GamePhase.BUY) { "当前不是购买阶段" }
        turnManager.advanceToNextPhase(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    // ========== 阶段2：备菜阶段 ==========

    fun removeMenu(playerId: Int, card: MenuCard) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.PREPARE) { "当前不是备菜阶段" }

        val totalCards = player.refinedChamber.size + player.kitchen.size
        require(totalCards > 6) { "菜品牌总数恰好为6张，不可再移除" }
        require(player.funds >= 3) { "资金不足：备菜需要3两" }

        player.funds -= 3
        player.kitchen.remove(card)

        // 舍弃一张菜单牌后，直接进入下一阶段
        turnManager.advanceToNextPhase(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun skipPreparePhase() {
        val state = requireState()
        require(state.currentPhase == GamePhase.PREPARE) { "当前不是备菜阶段" }
        turnManager.advanceToNextPhase(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    // ========== 阶段3：招待阶段 ==========

    fun selectGuest(playerId: Int, queuePosition: Int) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.SERVE) { "当前不是招待阶段" }

        val guestIndex = state.guestQueue.size - queuePosition
        require(guestIndex in state.guestQueue.indices) { "无效的客人位置" }

        // Calculate tip (小费): position 1 free, others pay 1 per skip
        val tip = (queuePosition - 1).coerceAtLeast(0)
        require(player.funds >= tip) { "资金不足：小费需要${tip}两" }

        player.funds -= tip
        state.settlementTip = tip

        val guest = state.guestQueue.removeAt(guestIndex)
        state.turnStep = TurnStep.PHASE_3_SETTLE_MENU
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun settleMenuIncome(playerId: Int): MenuSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.guestQueue.getOrNull(0) ?: error("未选择客人")

        // TODO: Store selected guest reference
        // For now, use the first guest in queue (simplified)
        val result = settlementEngine.calculateMenuIncome(
            player = player,
            guest = guest,
            event = state.activeEvent
        )

        state.settlementMenuIncome = result.totalIncome
        player.funds += result.totalIncome
        state.turnStep = TurnStep.PHASE_3_SETTLE_SHOP
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
        return result
    }

    fun settleShopIncome(playerId: Int): ShopSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.guestQueue.getOrNull(0) ?: error("未选择客人")

        val result = settlementEngine.calculateShopIncome(
            player = player,
            guest = guest,
            event = state.activeEvent
        )

        state.settlementShopIncome = result.totalIncome
        player.funds += result.totalIncome
        state.turnStep = TurnStep.PHASE_3_REFRESH_GUEST
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
        return result
    }

    fun refreshGuestQueue() {
        val state = requireState()

        // Refresh: draw from deck until queue has 4 guests (or 6 with event)
        val maxQueue = if (state.activeEvent?.effect == EventEffect.ZHANG_DENG_JIE_CAI) 6 else 4

        while (state.guestQueue.size < maxQueue && state.guestDeck.isNotEmpty()) {
            val nextCard = state.guestDeck.removeAt(0)
            state.guestQueue.add(nextCard)
        }

        // Check for events in deck
        // (Event cards are mixed in - handled during initial setup)

        state.turnStep = TurnStep.TURN_END_CHECK
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun endTurn() {
        val state = requireState()
        val player = state.currentPlayer

        if (winChecker.checkWin(player)) {
            // Winner!
            return
        }

        turnManager.advanceToNextPlayer(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    private fun requireState(): GameState =
        _gameState.value ?: error("游戏未初始化")
}
