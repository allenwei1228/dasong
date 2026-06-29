package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import com.dasong.commerce.util.DiceRoller
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

    fun initGame(playerCount: Int, playerNames: List<String> = emptyList()) {
        require(playerCount in 2..4) { "玩家数量必须在2-4之间" }

        val menuPool = deckManager.initMenuPool()
        val shopPool = deckManager.initShopPool()
        val combinedDeck = deckManager.createCombinedDeck()

        // Draw initial guest queue (first 4 guests, skip events)
        val queue = mutableListOf<GuestCard>()
        val deck = mutableListOf<DeckCard>()

        for (item in combinedDeck) {
            when (item) {
                is DeckCard.Guest -> {
                    if (queue.size < 4) queue.add(item.card)
                    else deck.add(item)
                }
                is DeckCard.Event -> deck.add(item)
            }
        }

        // Create players with randomly assigned seat orders
        // 玩家序号随机抽取：打乱名字索引，使 seatOrder 随机分配给各玩家
        // 行动顺序按 seatOrder 固定排列：玩家1(5铜钱) → 玩家2(6铜钱) → 玩家3(7铜钱) → 玩家4(8铜钱)
        val names = if (playerNames.size == playerCount) playerNames else emptyList()
        val nameIndices = (0 until playerCount).toMutableList()
        nameIndices.shuffle()
        val players = (1..playerCount).map { i ->
            val nameIndex = nameIndices[i - 1]
            val name = names.getOrElse(nameIndex) { "玩家${nameIndex + 1}" }
            val menuCards = deckManager.getInitialMenuForPlayer()
            PlayerState(
                id = i,
                name = name,
                seatOrder = i,
                funds = deckManager.getStartingFunds(i),
                refinedChamber = menuCards.toMutableList(),
                kitchen = mutableListOf()
            )
        }.sortedBy { it.seatOrder }  // 按seatOrder排序：玩家1先行动，玩家4最后

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
        state.currentTurnRecord.buyPhase.add("购买菜单牌[${card.name}]")

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
        state.currentTurnRecord.buyPhase.add("放置店铺[${shop.name}]于#${foundationIndex + 1}号地")

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
        state.currentTurnRecord.buyPhase.add("建房[${foundation.shopCard!!.name}](#${foundationIndex + 1}号地)")

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
        state.currentTurnRecord.preparePhase.add("备菜：舍弃[${card.name}]")

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

    fun estimateDiceCount(playerId: Int): Int {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")
        return settlementEngine.estimateDiceCount(player, guest, state.activeEvent)
    }

    fun getDiceSources(playerId: Int): List<String> {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")
        return settlementEngine.getDiceSources(player, guest, state.activeEvent)
    }

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
        state.currentTurnRecord.servePhase.add("招待客人[${guest.name}]")
        state.turnStep = TurnStep.PHASE_3_SETTLE_MENU
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    fun settleMenuIncome(playerId: Int, diceRoller: () -> Int = { DiceRoller.roll() }): MenuSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")

        val result = settlementEngine.calculateMenuIncome(
            player = player,
            guest = guest,
            event = state.activeEvent,
            diceRoller = diceRoller
        )

        state.settlementMenuIncome = result.totalIncome
        player.funds += result.totalIncome
        state.turnStep = TurnStep.PHASE_3_SETTLE_SHOP
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
        return result
    }

    fun settleShopIncome(playerId: Int, selectedShopIndex: Int? = null, diceRoller: () -> Int = { DiceRoller.roll() }): ShopSettlementResult {
        val state = requireState()
        val player = state.players.find { it.id == playerId } ?: error("玩家不存在")
        val guest = state.selectedGuest ?: error("未选择客人")

        val result = settlementEngine.calculateShopIncome(
            player = player,
            guest = guest,
            event = state.activeEvent,
            selectedShopIndex = selectedShopIndex,
            diceRoller = diceRoller
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
        drawNextFromDeck(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    /**
     * Draw cards from deck one by one:
     * - GuestCard: add to queue, continue until queue is full
     * - EventCard: set announceEvent and pause for UI to confirm
     * - When deck is empty, reshuffle all guests and events back into the deck
     */
    private fun drawNextFromDeck(state: GameState) {
        val maxQueue = when (state.activeEvent?.effect) {
            EventEffect.ZHANG_DENG_JIE_CAI -> 6
            EventEffect.YIN_ZHUANG_SU_GUO -> 2
            else -> 4
        }

        // 当牌堆为空且队列需要补满时，重新放回所有客人、事件，洗牌重新放置
        if (state.guestDeck.isEmpty() && state.guestQueue.size < maxQueue) {
            state.guestDeck.addAll(deckManager.createCombinedDeck())
        }

        while (state.guestQueue.size < maxQueue && state.guestDeck.isNotEmpty()) {
            val deckCard = state.guestDeck.removeAt(0)
            when (deckCard) {
                is DeckCard.Guest -> {
                    // 新客人排队到队尾（index 0 = 展示 position 最大 = 队尾）
                    state.guestQueue.add(0, deckCard.card)
                }
                is DeckCard.Event -> {
                    // Event card found: pause for UI announcement
                    state.announceEvent = deckCard.card
                    state.turnStep = TurnStep.PHASE_3_EVENT_ANNOUNCE
                    return
                }
            }
        }

        // No more events (or deck empty), proceed to end turn check
        state.announceEvent = null
        state.turnStep = TurnStep.TURN_END_CHECK
    }

    /**
     * Called when the UI confirms the event announcement.
     * Processes the event and continues drawing from the deck.
     */
    fun confirmEventAnnouncement() {
        val state = requireState()
        val event = state.announceEvent ?: error("没有待确认的事件")

        when (event.duration) {
            EventDuration.CONTINUOUS -> {
                // 先撤销上一个持续事件的效果
                val prevEvent = state.activeEvent
                if (prevEvent != null) {
                    eventExecutor.executeDeactivation(
                        event = prevEvent,
                        guestQueue = state.guestQueue
                    )
                }

                // 再应用新事件
                state.activeEvent = event

                // Execute one-time activation effect (e.g., expand queue, discard guests)
                eventExecutor.executeActivation(
                    event = event,
                    guestQueue = state.guestQueue,
                    guestDeck = state.guestDeck
                )
            }
            EventDuration.IMMEDIATE -> {
                // Execute immediately and discard
                eventExecutor.executeImmediate(
                    event = event,
                    players = state.players,
                    guestQueue = state.guestQueue,
                    guestDeck = state.guestDeck,
                    menuPool = state.menuPool
                )
                // Immediate events do NOT become activeEvent
            }
        }

        state.announceEvent = null

        // Continue drawing from deck
        drawNextFromDeck(state)
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

        // 记录本回合操作摘要到历史（按阶段分组）
        val summary = buildTurnSummary(player.name, state.currentTurnRecord)
        if (summary.isNotBlank()) {
            state.turnHistory.add(summary)
        }
        state.currentTurnRecord = TurnActionRecord()

        turnManager.advanceToNextPlayer(state)
        _gameState.value = state.copy(stateVersion = state.stateVersion + 1)
    }

    private fun buildTurnSummary(playerName: String, record: TurnActionRecord): String {
        val parts = mutableListOf<String>()
        if (record.buyPhase.isNotEmpty()) parts.add("购买:${record.buyPhase.joinToString("、")}")
        if (record.preparePhase.isNotEmpty()) parts.add("备菜:${record.preparePhase.joinToString("、")}")
        if (record.servePhase.isNotEmpty()) parts.add("招待:${record.servePhase.joinToString("、")}")
        if (parts.isEmpty()) return ""
        return "$playerName: ${parts.joinToString("；")}"
    }

    // ========== 联机模式支持 ==========

    /**
     * 从远程状态初始化游戏（非房主玩家使用）。
     * 直接设置 GameState 来自服务器下发的初始状态。
     */
    fun initGameFromRemote(state: GameState) {
        _gameState.value = state
    }

    /**
     * 应用远程同步下来的游戏状态（非当前回合玩家使用）。
     * 当服务器推送的 version 比本地新时调用。
     */
    fun applyRemoteState(state: GameState) {
        val local = _gameState.value
        if (local == null || state.stateVersion > local.stateVersion) {
            _gameState.value = state
        }
    }

    private fun requireState(): GameState =
        _gameState.value ?: error("游戏未初始化")
}
