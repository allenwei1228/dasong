package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * 结算引擎事件测试
 *
 * 验证持续事件效果是否正确影响菜单和店铺收入计算。
 *
 * 测试覆盖:
 *  1. 俭以养德: 菜单消耗 -1
 *  2. 时和年丰: 菜单消耗 +1
 *  3. 无尖不商: 店铺收入 -1
 *  4. 门可罗雀: 仅选 1 个店铺结算
 *  5. 无事件时的正常结算（基线对比）
 */
class SettlementEngineEventTest {

    private lateinit var engine: SettlementEngine
    private val fixedDice = { 3 } // 固定骰子值，保证可重复性

    @Before
    fun setUp() {
        engine = SettlementEngine()
    }

    // --- 辅助方法 ---

    /** 创建一个有一品卡的精炼房的玩家 */
    private fun playerWithMenuCards(
        id: Int = 1,
        refinedCards: List<MenuCard> = listOf(MenuCard(1, "东坡肉", MenuGrade.ONE, 9)),
        funds: Int = 10
    ): PlayerState {
        val p = TestFactory.player(id, funds = funds)
        p.refinedChamber.clear()
        p.refinedChamber.addAll(refinedCards)
        return p
    }

    private fun event(effect: EventEffect) =
        EventCard(1, "test", EventType.NEGATIVE, "", effect, EventDuration.CONTINUOUS)

    // ==================== 俭以养德: 菜单消耗 -1 ====================

    @Test
    fun `JIAN_YI_YANG_DE reduces menu consumption by 1`() {
        val player = playerWithMenuCards(refinedCards = listOf(
            MenuCard(1, "炊饼", MenuGrade.FOUR, 0),
            MenuCard(2, "炒青菜", MenuGrade.THREE, 3),
            MenuCard(3, "酱牛肉", MenuGrade.THREE, 3)
        ))
        val guest = TestFactory.guest(menuConsumption = 3) // 需要3张

        val result = engine.calculateMenuIncome(
            player, guest,
            event = event(EventEffect.JIAN_YI_YANG_DE),
            diceRoller = fixedDice
        )

        // 俭以养德: 3 - 1 = 2 张
        assertEquals("应抽2张卡", 2, result.cardsDrawn.size)
    }

    @Test
    fun `JIAN_YI_YANG_DE menu consumption cannot go below 0`() {
        val player = playerWithMenuCards(refinedCards = listOf(
            MenuCard(1, "炊饼", MenuGrade.FOUR, 0)
        ))
        val guest = TestFactory.guest(menuConsumption = 1) // 需要1张

        val result = engine.calculateMenuIncome(
            player, guest,
            event = event(EventEffect.JIAN_YI_YANG_DE),
            diceRoller = fixedDice
        )

        // 俭以养德: 1 - 1 = 0，不能为负
        assertEquals("应抽0张卡", 0, result.cardsDrawn.size)
    }

    // ==================== 时和年丰: 菜单消耗 +1 ====================

    @Test
    fun `SHI_HE_NIAN_FENG increases menu consumption by 1`() {
        val player = playerWithMenuCards(refinedCards = listOf(
            MenuCard(1, "炊饼", MenuGrade.FOUR, 0),
            MenuCard(2, "炊饼", MenuGrade.FOUR, 0),
            MenuCard(3, "炒青菜", MenuGrade.THREE, 3)
        ))
        val guest = TestFactory.guest(menuConsumption = 2)

        val result = engine.calculateMenuIncome(
            player, guest,
            event = event(EventEffect.SHI_HE_NIAN_FENG),
            diceRoller = fixedDice
        )

        // 时和年丰: 2 + 1 = 3 张
        assertEquals("应抽3张卡", 3, result.cardsDrawn.size)
    }

    // ==================== 无尖不商: 店铺收入 -1 ====================

    @Test
    fun `WU_JIAN_BU_SHANG reduces each shop income by 1`() {
        val player = TestFactory.playerWithBuiltShops(
            id = 1, funds = 50,
            shops = listOf(
                0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI, 6, 5),
                2 to TestFactory.shopCard(3, "茶馆", ShopType.CHA_GUAN, 5, 3)
            )
        )
        val guest = TestFactory.guest(shopTypes = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN))

        val result = engine.calculateShopIncome(
            player, guest,
            event = event(EventEffect.WU_JIAN_BU_SHANG)
        )

        val shopIncomes = result.activatedShops.map { it.baseIncome }
        // 酒肆: 5 -> 4, 茶馆: 3 -> 2
        assertTrue("酒肆收入应为4", shopIncomes.any { it == 4 })
        assertTrue("茶馆收入应为2", shopIncomes.any { it == 2 })
    }

    @Test
    fun `WU_JIAN_BU_SHANG income cannot go below 0`() {
        val player = TestFactory.playerWithBuiltShops(
            id = 1, funds = 50,
            shops = listOf(
                0 to TestFactory.shopCard(1, "饮子铺", ShopType.YIN_ZI, 5, 0)
            )
        )
        val guest = TestFactory.guest(shopTypes = listOf(ShopType.YIN_ZI))

        val result = engine.calculateShopIncome(
            player, guest,
            event = event(EventEffect.WU_JIAN_BU_SHANG)
        )

        // 无尖不商: 0 - 1 = 0 (不低于0)
        assertEquals("无尖不商下收入最低为0", 0, result.activatedShops[0].baseIncome)
    }

    // ==================== 门可罗雀: 仅1店铺 ====================

    @Test
    fun `MEN_KE_LUO_QUE only activates 1 shop`() {
        val player = TestFactory.playerWithBuiltShops(
            id = 1, funds = 50,
            shops = listOf(
                0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI, 6, 5),
                2 to TestFactory.shopCard(3, "茶馆", ShopType.CHA_GUAN, 5, 3)
            )
        )
        val guest = TestFactory.guest(shopTypes = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN))

        val result = engine.calculateShopIncome(
            player, guest,
            event = event(EventEffect.MEN_KE_LUO_QUE)
        )

        assertEquals("门可罗雀只激活1个店铺", 1, result.activatedShops.size)
    }

    @Test
    fun `MEN_KE_LUO_QUE can select specific shop`() {
        val player = TestFactory.playerWithBuiltShops(
            id = 1, funds = 50,
            shops = listOf(
                0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI, 6, 5),
                2 to TestFactory.shopCard(3, "茶馆", ShopType.CHA_GUAN, 5, 3)
            )
        )
        val guest = TestFactory.guest(shopTypes = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN))

        val result = engine.calculateShopIncome(
            player, guest,
            event = event(EventEffect.MEN_KE_LUO_QUE),
            selectedShopIndex = 2 // 选择茶馆
        )

        assertEquals("门可罗雀指定店铺", 1, result.activatedShops.size)
        assertEquals("应激活茶馆", ShopType.CHA_GUAN, result.activatedShops[0].shop.type)
    }

    // ==================== 基线: 无事件 ====================

    @Test
    fun `no event should not modify menu consumption`() {
        val player = playerWithMenuCards(refinedCards = listOf(
            MenuCard(1, "炊饼", MenuGrade.FOUR, 0),
            MenuCard(2, "炒青菜", MenuGrade.THREE, 3)
        ))
        val guest = TestFactory.guest(menuConsumption = 2)

        val result = engine.calculateMenuIncome(
            player, guest,
            event = null, diceRoller = fixedDice
        )

        assertEquals("无事件抽2张卡", 2, result.cardsDrawn.size)
    }

    @Test
    fun `no event activates all matching shops`() {
        val player = TestFactory.playerWithBuiltShops(
            id = 1, funds = 50,
            shops = listOf(
                0 to TestFactory.shopCard(1, "酒肆", ShopType.JIU_SI, 6, 5),
                2 to TestFactory.shopCard(3, "茶馆", ShopType.CHA_GUAN, 5, 3)
            )
        )
        val guest = TestFactory.guest(shopTypes = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN))

        val result = engine.calculateShopIncome(player, guest, event = null)

        assertEquals("无事件激活所有匹配店铺", 2, result.activatedShops.size)
    }
}
