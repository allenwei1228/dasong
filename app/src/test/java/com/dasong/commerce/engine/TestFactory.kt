package com.dasong.commerce.engine

import com.dasong.commerce.model.*
import com.dasong.commerce.model.card.*

/**
 * 测试辅助工厂：快速构建测试所需的游戏状态对象。
 */
object TestFactory {

    /** 创建基础 MenuPool（含四品菜单卡） */
    fun menuPoolWithFourGrade(count: Int = 10): MenuPool {
        val pool = MenuPool()
        repeat(count) {
            pool.gradeFour.add(MenuCard(400 + it, "炊饼", MenuGrade.FOUR, 0))
        }
        return pool
    }

    /** 创建带资金的玩家 */
    fun player(id: Int, name: String = "玩家$id", funds: Int = 10): PlayerState {
        return PlayerState(
            id = id,
            name = name,
            seatOrder = id,
            funds = funds
        )
    }

    /** 创建已建造店铺的玩家（用于测试事件） */
    fun playerWithBuiltShops(
        id: Int,
        name: String = "玩家$id",
        funds: Int = 10,
        shops: List<Pair<Int, ShopCard>> = emptyList() // foundationIndex to ShopCard
    ): PlayerState {
        val player = PlayerState(
            id = id,
            name = name,
            seatOrder = id,
            funds = funds
        )
        shops.forEach { (idx, shop) ->
            val f = player.foundations[idx]
            f.shopCard = shop
            f.hasModel = true
            f.isBuilt = true
        }
        return player
    }

    /** 创建客人队列 */
    fun guestQueue(vararg guests: GuestCard): MutableList<GuestCard> =
        guests.toMutableList()

    /** 创建客人队列（用模板方法快速生成） */
    fun guestQueue(
        size: Int,
        shopTypes: List<ShopType> = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN),
        menuConsumption: Int = 2
    ): MutableList<GuestCard> {
        return (1..size).map {
            GuestCard(it, "客人$it", menuConsumption, shopTypes)
        }.toMutableList()
    }

    /** 创建测试用 GuestCard */
    fun guest(
        id: Int = 1,
        name: String = "测试客人",
        menuConsumption: Int = 2,
        shopTypes: List<ShopType> = listOf(ShopType.JIU_SI, ShopType.CHA_GUAN)
    ): GuestCard = GuestCard(id, name, menuConsumption, shopTypes)

    /** 创建测试用 ShopCard */
    fun shopCard(
        id: Int = 1,
        name: String = "酒肆",
        type: ShopType = ShopType.JIU_SI,
        buildCost: Int = 6,
        baseIncome: Int = 3,
        incomeType: IncomeType = IncomeType.FIXED
    ): ShopCard = ShopCard(id, name, type, buildCost, baseIncome, incomeType = incomeType)

    /** 创建基础 GameState */
    fun gameState(players: List<PlayerState>, guestQueue: MutableList<GuestCard>): GameState {
        return GameState(
            players = players,
            guestQueue = guestQueue,
            guestDeck = mutableListOf()
        )
    }
}
