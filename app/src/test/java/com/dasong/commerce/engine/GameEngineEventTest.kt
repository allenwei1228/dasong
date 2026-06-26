package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import org.junit.Assert.*
import org.junit.Test

/**
 * 游戏引擎事件集成测试
 *
 * 通过自定义牌堆控制事件出现顺序，验证完整的事件流程：
 *  1. 卡牌本身效果是否生效（持续事件激活 + 即时事件触发）
 *  2. 上一个生效的持续事件效果是否被正确移除
 *  3. 连续翻出事件牌时是否能正常运行
 *
 * 注意：事件牌只在队列有空位（招待了客人）时才会被翻出。
 */
class GameEngineEventTest {

    // --- 测试用卡牌工厂 ---
    private fun guest(
        id: Int, menuConsumption: Int = 2,
        shopTypes: List<ShopType> = listOf(ShopType.YIN_ZI)
    ) = GuestCard(id, "客$id", menuConsumption, shopTypes)

    private fun event(
        id: Int, effect: EventEffect,
        duration: EventDuration = EventDuration.CONTINUOUS,
        type: EventType = EventType.NEGATIVE
    ) = EventCard(id, effect.name, type, "", effect, duration)

    // ==================== 1. 卡牌效果是否生效 ====================

    @Test
    fun `single continuous event activates correctly`() {
        // 队列3人(空1位) → 翻牌触发张灯结彩 → 列队扩到6 → 用牌堆剩下的2客人补齐 → 队列=5
        val deck = listOf(
            DeckCard.Event(event(1, EventEffect.ZHANG_DENG_JIE_CAI)),
            DeckCard.Guest(guest(10)),
            DeckCard.Guest(guest(11))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()

        val state = engine.gameState.value!!
        assertEquals("activeEvent = 张灯结彩", EventEffect.ZHANG_DENG_JIE_CAI, state.activeEvent?.effect)
        // 3基础 + 2补齐 = 5（牌堆只有2张客牌）
        assertEquals("队列应=5(3+2补齐)", 5, state.guestQueue.size)
    }

    @Test
    fun `single immediate event executes correctly`() {
        val deck = listOf(
            DeckCard.Event(event(7, EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE, EventType.RESHUFFLE))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()

        val state = engine.gameState.value!!
        assertNull("即时事件不成为activeEvent", state.activeEvent)
        assertEquals("辞旧迎新清空队列", 0, state.guestQueue.size)
    }

    @Test
    fun `KE_JUAN_ZA_SHUI immediate event taxes correctly`() {
        val deck = listOf(
            DeckCard.Event(event(8, EventEffect.KE_JUAN_ZA_SHUI, EventDuration.IMMEDIATE))
        )
        val shops1 = listOf(
            0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI),
            1 to TestFactory.shopCard(2, "茶馆", ShopType.CHA_GUAN)
        )
        val shops2 = listOf(0 to TestFactory.shopCard(3, "瓷器铺", ShopType.CI_QI))
        val engine = createEngine(deck, initialQueueSize = 3, playerShops = listOf(shops1, shops2))

        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()

        val state = engine.gameState.value!!
        assertEquals("玩家1有2模型，扣4两", 6, state.players[0].funds)
        assertEquals("玩家2有1模型，扣2两", 8, state.players[1].funds)
    }

    @Test
    fun `SHUO_GUO_LEI_LEI immediate event distributes menus`() {
        val deck = listOf(
            DeckCard.Event(event(9, EventEffect.SHUO_GUO_LEI_LEI, EventDuration.IMMEDIATE, EventType.POSITIVE))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()

        val state = engine.gameState.value!!
        assertTrue("玩家1获得菜单", state.players[0].kitchen.isNotEmpty())
        assertTrue("玩家2获得菜单", state.players[1].kitchen.isNotEmpty())
    }

    // ==================== 2. 上一个持续事件效果被正确移除 ====================

    @Test
    fun `new continuous event replaces old one with deactivation`() {
        // 牌堆: [张灯结彩, 客人×3, 银装素裹, 客人]
        // 第一轮: 张灯结彩激活(3→6, 用掉3个客人) → queue=6
        // 招待后: queue=5 → 翻牌银装素裹 → 撤销(→4)→激活(→2)
        val deck = listOf(
            DeckCard.Event(event(1, EventEffect.ZHANG_DENG_JIE_CAI)),
            DeckCard.Guest(guest(50)), DeckCard.Guest(guest(51)), DeckCard.Guest(guest(52)),
            DeckCard.Event(event(2, EventEffect.YIN_ZHUANG_SU_GUO)),
            DeckCard.Guest(guest(53))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        // 张灯结彩激活
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()
        val state1 = engine.gameState.value!!
        assertEquals("第一轮 activeEvent=张灯结彩", EventEffect.ZHANG_DENG_JIE_CAI, state1.activeEvent?.effect)
        assertEquals("队列=6", 6, state1.guestQueue.size)

        // 招待一位客人 → 队列5 → 翻出银装素裹
        serveGuest(engine)
        val state2 = engine.gameState.value!!
        assertEquals("待公告=银装素裹", EventEffect.YIN_ZHUANG_SU_GUO, state2.announceEvent!!.effect)

        // 确认银装素裹 → 张灯结彩撤销(→4)，银装素裹激活(→2 →补→4)
        engine.confirmEventAnnouncement()
        val state3 = engine.gameState.value!!

        assertEquals("activeEvent=银装素裹", EventEffect.YIN_ZHUANG_SU_GUO, state3.activeEvent?.effect)
        assertEquals("队列=2(银装素裹)", 2, state3.guestQueue.size)
    }

    @Test
    fun `new continuous event deactivates ZHANG_DENG_JIE_CAI properly`() {
        val engine = createEngineWithActiveEvent(EventEffect.ZHANG_DENG_JIE_CAI, queueSize = 6)

        // 招待一位客人 → 队列5 → 翻出无尖不商
        val state = engine.gameState.value!!
        state.guestDeck.add(0, DeckCard.Event(event(2, EventEffect.WU_JIAN_BU_SHANG)))
        serveGuest(engine)

        assertEquals("待公告=无尖不商", EventEffect.WU_JIAN_BU_SHANG, state.announceEvent!!.effect)

        engine.confirmEventAnnouncement()

        // 张灯结彩撤销 → 队列从5恢复到≤4 → 无尖不商不改变队列
        val after = engine.gameState.value!!
        assertTrue("队列应≤4(张灯结彩已撤销)", after.guestQueue.size <= 4)
        assertEquals("新事件=无尖不商", EventEffect.WU_JIAN_BU_SHANG, after.activeEvent?.effect)
    }

    @Test
    fun `deactivation handles small queue gracefully`() {
        val engine = createEngineWithActiveEvent(EventEffect.ZHANG_DENG_JIE_CAI, queueSize = 2)

        val state = engine.gameState.value!!
        state.guestDeck.add(0, DeckCard.Event(event(3, EventEffect.WU_JIAN_BU_SHANG)))
        serveGuest(engine)

        engine.confirmEventAnnouncement()

        val after = engine.gameState.value!!
        assertTrue("队列应≤4", after.guestQueue.size <= 4)
        assertEquals("新事件生效", EventEffect.WU_JIAN_BU_SHANG, after.activeEvent?.effect)
    }

    // ==================== 3. 连续翻出事件牌 ====================

    @Test
    fun `consecutive event cards processed in sequence`() {
        // 时和年丰(settelement only) → 银装素裹(队列→2) → 招待后翻出后续
        val deck = listOf(
            DeckCard.Event(event(1, EventEffect.SHI_HE_NIAN_FENG, type = EventType.POSITIVE)),
            DeckCard.Event(event(2, EventEffect.YIN_ZHUANG_SU_GUO)),
            DeckCard.Guest(guest(60)), DeckCard.Guest(guest(61))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        // 时和年丰激活（不改变队列大小，只是结算影响）
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()
        val state1 = engine.gameState.value!!
        assertEquals("事件A: 时和年丰", EventEffect.SHI_HE_NIAN_FENG, state1.activeEvent?.effect)

        // drawNextFromDeck 继续 → 银装素裹
        assertNotNull("应有待公告事件B", state1.announceEvent)
        assertEquals("事件B: 银装素裹", EventEffect.YIN_ZHUANG_SU_GUO, state1.announceEvent!!.effect)

        // 确认银装素裹 → 队列→2
        engine.confirmEventAnnouncement()
        val state2 = engine.gameState.value!!
        assertEquals("activeEvent=银装素裹", EventEffect.YIN_ZHUANG_SU_GUO, state2.activeEvent?.effect)
        assertEquals("队列=2", 2, state2.guestQueue.size)
    }

    @Test
    fun `consecutive immediate event then continuous event`() {
        // 辞旧迎新清空队列 → drawNextFromDeck 补到4 → 遇到张灯结彩
        val deck = listOf(
            DeckCard.Event(event(7, EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE, EventType.RESHUFFLE)),
            DeckCard.Event(event(5, EventEffect.ZHANG_DENG_JIE_CAI)),
            DeckCard.Guest(guest(70)), DeckCard.Guest(guest(71)),
            DeckCard.Guest(guest(72)), DeckCard.Guest(guest(73)),
            DeckCard.Guest(guest(74)), DeckCard.Guest(guest(75))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        // 辞旧迎新清空队列
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()
        val state1 = engine.gameState.value!!
        assertEquals("队列清空", 0, state1.guestQueue.size)
        assertNull("即时事件不设为activeEvent", state1.activeEvent)

        // drawNextFromDeck → 补到4人 → 期间翻出张灯结彩(作为第2张)
        assertNotNull("待公告=张灯结彩", state1.announceEvent)
        assertEquals("张灯结彩", EventEffect.ZHANG_DENG_JIE_CAI, state1.announceEvent!!.effect)

        // 确认张灯结彩 → 队列到6
        engine.confirmEventAnnouncement()
        val state2 = engine.gameState.value!!
        assertEquals("activeEvent=张灯结彩", EventEffect.ZHANG_DENG_JIE_CAI, state2.activeEvent?.effect)
        assertEquals("队列=6", 6, state2.guestQueue.size)
    }

    @Test
    fun `continuous event then immediate event in same round`() {
        // 无尖不商 + 苛捐杂税连续翻出
        val deck = listOf(
            DeckCard.Event(event(3, EventEffect.WU_JIAN_BU_SHANG)),
            DeckCard.Event(event(8, EventEffect.KE_JUAN_ZA_SHUI, EventDuration.IMMEDIATE)),
            DeckCard.Guest(guest(80)), DeckCard.Guest(guest(81))
        )
        val shops1 = listOf(0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI))
        val engine = createEngine(deck, initialQueueSize = 3,
            playerShops = listOf(shops1, emptyList()))

        // 无尖不商
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()
        val state1 = engine.gameState.value!!
        assertEquals("activeEvent=无尖不商", EventEffect.WU_JIAN_BU_SHANG, state1.activeEvent?.effect)

        // 苛捐杂税 (连续翻出)
        assertEquals("待公告=苛捐杂税", EventEffect.KE_JUAN_ZA_SHUI, state1.announceEvent!!.effect)

        // 确认苛捐杂税 → 即时扣税，无尖不商不变
        engine.confirmEventAnnouncement()
        val state2 = engine.gameState.value!!
        assertEquals("无尖不商仍在生效", EventEffect.WU_JIAN_BU_SHANG, state2.activeEvent?.effect)
        assertEquals("玩家1有1模型，扣2两", 8, state2.players[0].funds)
    }

    @Test
    fun `three events with serving between them`() {
        // 三个连续事件（无客人穿插），时和年丰→银装素裹→无尖不商
        // drawNextFromDeck 会依次翻出
        val deck = listOf(
            DeckCard.Event(event(1, EventEffect.SHI_HE_NIAN_FENG, type = EventType.POSITIVE)),
            DeckCard.Event(event(2, EventEffect.YIN_ZHUANG_SU_GUO)),
            DeckCard.Event(event(3, EventEffect.WU_JIAN_BU_SHANG)),
            DeckCard.Guest(guest(90))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        // 事件A: 时和年丰
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()
        assertEquals("A: 时和年丰", EventEffect.SHI_HE_NIAN_FENG,
            engine.gameState.value!!.activeEvent?.effect)

        // drawNextFromDeck 继续 → 翻出银装素裹
        assertNotNull("待公告B", engine.gameState.value!!.announceEvent)
        assertEquals("B待公告: 银装素裹", EventEffect.YIN_ZHUANG_SU_GUO,
            engine.gameState.value!!.announceEvent!!.effect)

        // 事件B: 银装素裹（替换时和年丰, 队列→2）
        engine.confirmEventAnnouncement()
        assertEquals("B: 银装素裹", EventEffect.YIN_ZHUANG_SU_GUO,
            engine.gameState.value!!.activeEvent?.effect)
        assertEquals("队列→2", 2, engine.gameState.value!!.guestQueue.size)

        // drawNextFromDeck 继续 → 无尖不商
        // 银装素裹后max=2, queue=2满 → 不继续翻 → 需要招待后再翻
        // 招待客人 → 队列空1位 → 翻出无尖不商
        serveGuest(engine)
        assertEquals("C待公告: 无尖不商", EventEffect.WU_JIAN_BU_SHANG,
            engine.gameState.value!!.announceEvent!!.effect)

        // 事件C: 无尖不商（替换银装素裹）
        engine.confirmEventAnnouncement()
        assertEquals("C: 无尖不商", EventEffect.WU_JIAN_BU_SHANG,
            engine.gameState.value!!.activeEvent?.effect)
    }

    @Test
    fun `event sequence with no guests remaining`() {
        // 只有事件，无客人 → 连续处理
        val deck = listOf(
            DeckCard.Event(event(7, EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE, EventType.RESHUFFLE)),
            DeckCard.Event(event(1, EventEffect.JIAN_YI_YANG_DE))
        )
        val engine = createEngine(deck, initialQueueSize = 3)

        // 辞旧迎新清空队列 → drawNextFromDeck补人 → 遇到俭以养德
        engine.refreshGuestQueue()
        engine.confirmEventAnnouncement()

        // 俭以养德
        engine.confirmEventAnnouncement()

        val state = engine.gameState.value!!
        assertEquals("俭以养德", EventEffect.JIAN_YI_YANG_DE, state.activeEvent?.effect)
        assertEquals("队列为空(无客人可补)", 0, state.guestQueue.size)
    }

    // ==================== 辅助方法 ====================

    /**
     * 模拟招待一位客人（移除队列最右端的客人）
     */
    private fun serveGuest(engine: GameEngine) {
        val state = engine.gameState.value!!
        if (state.guestQueue.isNotEmpty()) {
            state.guestQueue.removeAt(state.guestQueue.lastIndex)
        }
        engine.refreshGuestQueue()
    }

    private fun createEngine(
        deckContent: List<DeckCard>,
        initialQueueSize: Int = 3,
        playerShops: List<List<Pair<Int, ShopCard>>> = listOf(emptyList(), emptyList())
    ): GameEngine {
        val deckManager = object : DeckManager() {
            override fun createCombinedDeck(): MutableList<DeckCard> = mutableListOf()
        }

        val engine = GameEngine(deckManager = deckManager)
        engine.initGame(playerCount = playerShops.size.coerceAtLeast(2))

        val state = engine.gameState.value!!

        // 替换玩家属性（funds, foundations）
        playerShops.forEachIndexed { idx, shops ->
            if (idx < state.players.size && shops.isNotEmpty()) {
                val p = state.players[idx]
                p.funds = 10
                p.foundations.clear()
                p.foundations.addAll(TestFactory.playerWithBuiltShops(id = idx + 1, funds = 10, shops = shops).foundations)
            }
        }

        state.guestQueue.clear()
        state.guestQueue.addAll(TestFactory.guestQueue(initialQueueSize))

        state.guestDeck.clear()
        state.guestDeck.addAll(deckContent.toMutableList())

        return engine
    }

    private fun createEngineWithActiveEvent(
        effect: EventEffect,
        queueSize: Int
    ): GameEngine {
        val deckManager = object : DeckManager() {
            override fun createCombinedDeck(): MutableList<DeckCard> = mutableListOf()
        }
        val engine = GameEngine(deckManager = deckManager)
        engine.initGame(playerCount = 2)

        val state = engine.gameState.value!!
        state.activeEvent = event(99, effect)
        state.guestQueue.clear()
        state.guestQueue.addAll(TestFactory.guestQueue(queueSize))
        state.guestDeck.clear()

        return engine
    }
}
