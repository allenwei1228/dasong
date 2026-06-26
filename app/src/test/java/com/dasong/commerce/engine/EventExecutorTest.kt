package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 事件执行器单元测试
 *
 * 测试覆盖:
 *  1. 持续事件的激活效果 (executeActivation)
 *  2. 持续事件的撤销效果 (executeDeactivation)
 *  3. 即时事件的效果 (executeImmediate)
 */
class EventExecutorTest {

    private lateinit var executor: EventExecutor

    // -- 测试用卡牌工厂 --
    private fun event(effect: EventEffect, duration: EventDuration = EventDuration.CONTINUOUS) =
        EventCard(1, "test", EventType.NEGATIVE, "desc", effect, duration)

    @Before
    fun setUp() {
        executor = EventExecutor()
    }

    // ==================== 激活测试 ====================

    @Test
    fun `activation - ZHANG_DENG_JIE_CAI fills queue from 4 to 6`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>(
            DeckCard.Guest(TestFactory.guest(10, "新客A")),
            DeckCard.Guest(TestFactory.guest(11, "新客B")),
            DeckCard.Event(event(EventEffect.JIAN_YI_YANG_DE))
        )

        executor.executeActivation(event(EventEffect.ZHANG_DENG_JIE_CAI), queue, deck)

        assertEquals("队列应扩展到6", 6, queue.size)
        // 新客人插到队尾 (index 0)
        assertEquals("第一位新客应在队首(index0)", "新客B", queue[0].name)
        assertEquals("第二位新客应在index1", "新客A", queue[1].name)
        // 事件牌应留在牌堆中（未被抽出）
        assertEquals("事件牌仍在牌堆", 1, deck.size)
        assertTrue("牌堆中应为事件牌", deck[0] is DeckCard.Event)
    }

    @Test
    fun `activation - ZHANG_DENG_JIE_CAI deck empty no crash`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>()

        executor.executeActivation(event(EventEffect.ZHANG_DENG_JIE_CAI), queue, deck)

        assertEquals("队列应保持4", 4, queue.size)
    }

    @Test
    fun `activation - YIN_ZHUANG_SU_GUO shrinks queue from 4 to 2`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>()

        executor.executeActivation(event(EventEffect.YIN_ZHUANG_SU_GUO), queue, deck)

        assertEquals("队列应缩减到2", 2, queue.size)
    }

    @Test
    fun `activation - YIN_ZHUANG_SU_GUO queue already 2 no change`() {
        val queue = TestFactory.guestQueue(2)
        val deck = mutableListOf<DeckCard>()

        executor.executeActivation(event(EventEffect.YIN_ZHUANG_SU_GUO), queue, deck)

        assertEquals("队列应保持2", 2, queue.size)
    }

    @Test
    fun `activation - settlement-only events do nothing on activation`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>()

        for (effect in listOf(
            EventEffect.JIAN_YI_YANG_DE,
            EventEffect.MEN_KE_LUO_QUE,
            EventEffect.WU_JIAN_BU_SHANG,
            EventEffect.SHI_HE_NIAN_FENG
        )) {
            executor.executeActivation(event(effect), queue, deck)
            assertEquals("$effect 不应改变队列大小", 4, queue.size)
        }
    }

    // ==================== 撤销测试 ====================

    @Test
    fun `deactivation - ZHANG_DENG_JIE_CAI shrinks queue from 6 to 4`() {
        val queue = TestFactory.guestQueue(6)

        executor.executeDeactivation(event(EventEffect.ZHANG_DENG_JIE_CAI), queue)

        assertEquals("队列应从6恢复到4", 4, queue.size)
    }

    @Test
    fun `deactivation - ZHANG_DENG_JIE_CAI removes rightmost guests`() {
        val queue = TestFactory.guestQueue(6)

        // 记录被保留的客人（前4个，index 0-3）
        val kept = queue.take(4).toList()

        executor.executeDeactivation(event(EventEffect.ZHANG_DENG_JIE_CAI), queue)

        assertEquals(4, queue.size)
        // 应保留左侧4个（index 0-3），移除最右侧2个
        assertEquals(kept, queue)
    }

    @Test
    fun `deactivation - ZHANG_DENG_JIE_CAI queue already 4 no change`() {
        val queue = TestFactory.guestQueue(4)

        executor.executeDeactivation(event(EventEffect.ZHANG_DENG_JIE_CAI), queue)

        assertEquals("队列应保持4", 4, queue.size)
    }

    @Test
    fun `deactivation - YIN_ZHUANG_SU_GUO does nothing`() {
        val queue = TestFactory.guestQueue(2)

        executor.executeDeactivation(event(EventEffect.YIN_ZHUANG_SU_GUO), queue)

        assertEquals("撤销银装素裹不改变队列", 2, queue.size)
    }

    @Test
    fun `deactivation - settlement-only events do nothing`() {
        val queue = TestFactory.guestQueue(4)

        for (effect in listOf(
            EventEffect.JIAN_YI_YANG_DE,
            EventEffect.MEN_KE_LUO_QUE,
            EventEffect.WU_JIAN_BU_SHANG,
            EventEffect.SHI_HE_NIAN_FENG
        )) {
            executor.executeDeactivation(event(effect), queue)
            assertEquals("$effect 撤销不应改变队列", 4, queue.size)
        }
    }

    // ==================== 即时事件测试 ====================

    @Test
    fun `immediate - CI_JIU_YING_XIN clears all guests in queue`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>()
        val menuPool = TestFactory.menuPoolWithFourGrade()

        executor.executeImmediate(
            event(EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE),
            listOf(TestFactory.player(1)),
            queue, deck, menuPool
        )

        assertEquals("辞旧迎新应清空队列", 0, queue.size)
    }

    @Test
    fun `immediate - CI_JIU_YING_XIN empty queue no crash`() {
        val queue = mutableListOf<GuestCard>()
        val deck = mutableListOf<DeckCard>()
        val menuPool = TestFactory.menuPoolWithFourGrade()

        executor.executeImmediate(
            event(EventEffect.CI_JIU_YING_XIN, EventDuration.IMMEDIATE),
            listOf(TestFactory.player(1)),
            queue, deck, menuPool
        )

        assertEquals(0, queue.size)
    }

    @Test
    fun `immediate - KE_JUAN_ZA_SHUI taxes 2 per model per player`() {
        val player1 = TestFactory.playerWithBuiltShops(
            id = 1, funds = 20,
            shops = listOf(0 to TestFactory.shopCard(), 1 to TestFactory.shopCard())
        )
        val player2 = TestFactory.playerWithBuiltShops(
            id = 2, funds = 20,
            shops = listOf(0 to TestFactory.shopCard())
        )
        // player3 没有模型
        val player3 = TestFactory.player(id = 3, funds = 20)

        executor.executeImmediate(
            event(EventEffect.KE_JUAN_ZA_SHUI, EventDuration.IMMEDIATE),
            listOf(player1, player2, player3),
            mutableListOf(), mutableListOf(), TestFactory.menuPoolWithFourGrade()
        )

        assertEquals("玩家1有2个模型，扣4两", 16, player1.funds)
        assertEquals("玩家2有1个模型，扣2两", 18, player2.funds)
        assertEquals("玩家3无模型，不扣钱", 20, player3.funds)
    }

    @Test
    fun `immediate - KE_JUAN_ZA_SHUI fund cannot go below 0`() {
        val player1 = TestFactory.playerWithBuiltShops(
            id = 1, funds = 3, // 有2个模型，应扣4两
            shops = listOf(0 to TestFactory.shopCard(), 1 to TestFactory.shopCard())
        )

        executor.executeImmediate(
            event(EventEffect.KE_JUAN_ZA_SHUI, EventDuration.IMMEDIATE),
            listOf(player1),
            mutableListOf(), mutableListOf(), TestFactory.menuPoolWithFourGrade()
        )

        assertEquals("资金不应低于0", 0, player1.funds)
    }

    @Test
    fun `immediate - SHUO_GUO_LEI_LEI each player gets 2 four-grade menus`() {
        val menuPool = TestFactory.menuPoolWithFourGrade(10)
        val player1 = TestFactory.player(id = 1)
        val player2 = TestFactory.player(id = 2)

        val initialPoolSize = menuPool.gradeFour.size

        executor.executeImmediate(
            event(EventEffect.SHUO_GUO_LEI_LEI, EventDuration.IMMEDIATE),
            listOf(player1, player2),
            mutableListOf(), mutableListOf(), menuPool
        )

        assertEquals("玩家1厨房应有2张", 2, player1.kitchen.size)
        assertEquals("玩家2厨房应有2张", 2, player2.kitchen.size)
        assertEquals("公共池减少4张", initialPoolSize - 4, menuPool.gradeFour.size)
    }

    @Test
    fun `immediate - SHUO_GUO_LEI_LEI not enough cards gives partial`() {
        val menuPool = TestFactory.menuPoolWithFourGrade(3) // 只有3张
        val player1 = TestFactory.player(id = 1)
        val player2 = TestFactory.player(id = 2)

        executor.executeImmediate(
            event(EventEffect.SHUO_GUO_LEI_LEI, EventDuration.IMMEDIATE),
            listOf(player1, player2),
            mutableListOf(), mutableListOf(), menuPool
        )

        // 玩家1: toList()得到3张 → take(2) → 拿走2张 → gradeFour剩1张
        // 玩家2: toList()得到1张 → size=1 < 2 → 不拿 → 玩家2得0张
        assertEquals("玩家1拿2张", 2, player1.kitchen.size)
        assertEquals("玩家2得0张(池不足2张)", 0, player2.kitchen.size)
        assertEquals("公共池剩1张", 1, menuPool.gradeFour.size)
    }

    @Test
    fun `immediate - settlement-only events do nothing when immediate`() {
        val queue = TestFactory.guestQueue(4)
        val deck = mutableListOf<DeckCard>()
        val menuPool = TestFactory.menuPoolWithFourGrade()

        for (effect in listOf(
            EventEffect.JIAN_YI_YANG_DE,
            EventEffect.MEN_KE_LUO_QUE,
            EventEffect.WU_JIAN_BU_SHANG,
            EventEffect.SHI_HE_NIAN_FENG
        )) {
            executor.executeImmediate(
                event(effect, EventDuration.IMMEDIATE), // 虽然这些不应是 IM
                listOf(TestFactory.player(1)),
                queue, deck, menuPool
            )
            assertEquals("$effect 即时执行应no-op", 4, queue.size)
        }
    }
}
