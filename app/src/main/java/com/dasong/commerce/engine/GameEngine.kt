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
        require(!state.shopPlacedThisTurn) { "本回合已放置店铺牌，不可再购买菜单牌" }
        require(!state.menuBoughtThisTurn) { "本回合已购买过菜单牌，每回合只能购买一张" }
        require(player.funds >= card.cost) { "资金不足" }

        player.funds -= card.cost
        player.kitchen.add(card)
        state.menuBoughtThisTurn = true

        // Remove from menu pool
        state.menuPool.getPile(card.grade).remove(card)

        // 如果无可建造的房屋，自动跳过建房步骤
        val hasUnbuiltShops = player.foundations.any { it.shopCard != null && !it.isBuilt }
        if (!hasUnbuiltShops) {
            turnManager.advanceToNextPhase(state)
        }
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    // Step 1: 放置店铺牌（只支付清理地基费用，一回合一次，店铺效果不生效）
    fun placeShopCard(playerId: Int, shop: ShopCard, foundationIndex: Int) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.BUY) { "当前不是购买阶段" }
        require(!state.menuBoughtThisTurn) { "本回合已购买菜单牌，不可再放置店铺牌" }
        require(!state.shopPlacedThisTurn) { "本回合已放置过店铺牌，每回合只能放置一次" }

        val foundation = player.foundations[foundationIndex]
        require(foundation.shopCard == null) { "该地基已被占用" }

        val clearCost = foundation.clearCost
        require(player.funds >= clearCost) { "资金不足：清理地基需要${clearCost}两" }

        player.funds -= clearCost
        foundation.shopCard = shop
        foundation.hasModel = true
        foundation.isBuilt = false // 店铺效果不生效

        state.shopPlacedThisTurn = true

        // Remove from shop pool and replenish
        state.shopPool.available.remove(shop)
        val newShop = deckManager.drawShopFromPool(state.shopPool)
        if (newShop != null) {
            state.shopPool.available.add(newShop)
        }
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    // Step 2: 购买店铺房屋（支付店铺价格，不限次数，购买后效果生效）
    fun buildShopHouse(playerId: Int, foundationIndex: Int) {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        require(state.currentPhase == GamePhase.BUY) { "当前不是购买阶段" }

        val foundation = player.foundations[foundationIndex]
        require(foundation.shopCard != null) { "该地基没有放置店铺牌" }
        require(!foundation.isBuilt) { "该店铺房屋已购买" }

        val buildCost = foundation.shopCard!!.buildCost
        require(player.funds >= buildCost) { "资金不足：购买房屋需要${buildCost}两" }

        player.funds -= buildCost
        foundation.isBuilt = true // 店铺效果生效

        // 如果建造后无可再建造的房屋，自动跳过建房步骤进入下一阶段
        val hasUnbuiltShops = player.foundations.any { it.shopCard != null && !it.isBuilt }
        if (!hasUnbuiltShops) {
            turnManager.advanceToNextPhase(state)
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

        // 小费逻辑：选第n位客人，支付n-1两，分配给第1~n-1位客人各+1
        // 例：队列 [pos4, pos3, pos2, pos1]，选pos3 → 支付2两 → pos1和pos2各+1小费
        val tipCost = (queuePosition - 1).coerceAtLeast(0)
        require(player.funds >= tipCost) { "资金不足：小费需要${tipCost}两" }

        player.funds -= tipCost

        // 将小费分配给被跳过的客人（位置1~n-1，对应数组索引更大的元素）
        for (i in (guestIndex + 1) until state.guestQueue.size) {
            state.guestQueue[i].tip += 1
        }

        val guest = state.guestQueue.removeAt(guestIndex)
        // 选中客人身上已积累的小费（之前被跳过时积累的），归招待者所有
        val accumulatedTip = guest.tip
        state.settlementTip = accumulatedTip
        guest.tip = 0
        player.funds += accumulatedTip

        state.selectedGuest = guest
        state.turnStep = TurnStep.PHASE_3_SETTLE_MENU
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun settleMenuIncome(playerId: Int): MenuSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")

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

    fun settleShopIncome(playerId: Int, selectedShopIndex: Int? = null): ShopSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")

        val result = settlementEngine.calculateShopIncome(
            player = player,
            guest = guest,
            event = state.activeEvent,
            selectedShopIndex = selectedShopIndex
        )

        state.settlementShopIncome = result.totalIncome
        player.funds += result.totalIncome
        state.turnStep = TurnStep.PHASE_3_REFRESH_GUEST
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
        return result
    }

    // 门可罗雀：获取可选的店铺列表
    fun getMenKeLuoQueShops(playerId: Int): List<Foundation> {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: return emptyList()

        val builtShops = player.foundations.filter { it.hasModel && it.shopCard != null && it.isBuilt }
        return builtShops.filter { it.shopCard!!.type in guest.shopTypes }
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
            state.winner = player.name
            _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
            return
        }

        turnManager.advanceToNextPlayer(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    private fun requireState(): GameState =
        _gameState.value ?: error("游戏未初始化")
}
