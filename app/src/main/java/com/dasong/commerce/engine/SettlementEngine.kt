package com.dasong.commerce.engine

import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.*
import com.dasong.commerce.util.DiceRoller

data class DiceResult(
    val cardName: String,
    val diceValue: Int
)

data class MenuSettlementResult(
    val cardsDrawn: List<MenuCard>,
    val totalIncome: Int,
    val diceResults: List<DiceResult>,
    val menuCountBonus: Int
)

data class ShopActivation(
    val shop: ShopCard,
    val baseIncome: Int,
    val linkageBonus: Int,
    val totalIncome: Int
)

data class LinkageDetail(
    val fromShopType: ShopType,
    val toShopType: ShopType,
    val bonusPerShop: Int,
    val shopCount: Int,
    val totalBonus: Int
)

data class ShopSettlementResult(
    val activatedShops: List<ShopActivation>,
    val linkageDetails: List<LinkageDetail>,
    val totalIncome: Int
)

class SettlementEngine {

    /**
     * Calculate menu income for a player serving a guest
     */
    fun calculateMenuIncome(
        player: PlayerState,
        guest: GuestCard,
        event: EventCard?,
        diceRoller: () -> Int = { DiceRoller.roll() }
    ): MenuSettlementResult {
        var menuCount = guest.menuConsumption

        // Apply event effects
        when (event?.effect) {
            EventEffect.JIAN_YI_YANG_DE -> menuCount = (menuCount - 1).coerceAtLeast(0)
            EventEffect.SHI_HE_NIAN_FENG -> menuCount += 1
            else -> {}
        }

        // Calculate bonuses from player's shops
        val menuBonuses = player.foundations.filter { it.hasModel && it.shopCard != null }
        val jiuSiCount = menuBonuses.count { it.shopCard?.type == ShopType.JIU_SI }
        val cuJuCount = menuBonuses.count { it.shopCard?.type == ShopType.CU_JU }

        val bonusFromJiuSi = jiuSiCount * 1 // +1 per 酒肆
        val bonusFromCuJu = cuJuCount * 2 // +2 per 蹴鞠场
        val totalBonus = bonusFromJiuSi + bonusFromCuJu

        // Check if guest visits these shops
        val guestShops = guest.shopTypes
        val effectiveBonus = if (guestShops.any { it == ShopType.JIU_SI || it == ShopType.CU_JU }) {
            totalBonus
        } else 0

        menuCount += effectiveBonus

        // Draw cards from refined chamber
        val drawnCards = mutableListOf<MenuCard>()
        var cardsNeeded = menuCount

        // Ensure enough cards: shuffle kitchen into refined chamber if needed
        while (cardsNeeded > 0) {
            if (player.refinedChamber.isEmpty()) {
                if (player.kitchen.isEmpty()) break
                player.refinedChamber.addAll(player.kitchen)
                player.kitchen.clear()
                player.refinedChamber.shuffle()
            }
            drawnCards.add(player.refinedChamber.removeAt(player.refinedChamber.lastIndex))
            cardsNeeded--
        }

        // Calculate income
        var totalIncome = 0
        val diceResults = mutableListOf<DiceResult>()

        drawnCards.forEach { card ->
            var cardIncome = card.baseIncome

            // 一品: add dice roll
            if (card.grade == MenuGrade.ONE) {
                val diceValue = diceRoller()
                cardIncome += diceValue
                diceResults.add(DiceResult(card.name, diceValue))
            }

            totalIncome += cardIncome
        }

        // Move drawn cards to kitchen
        player.kitchen.addAll(drawnCards)

        return MenuSettlementResult(
            cardsDrawn = drawnCards,
            totalIncome = totalIncome,
            diceResults = diceResults,
            menuCountBonus = effectiveBonus
        )
    }

    /**
     * Calculate shop income for a player serving a guest
     */
    fun calculateShopIncome(
        player: PlayerState,
        guest: GuestCard,
        event: EventCard?
    ): ShopSettlementResult {
        val guestShopTypes = guest.shopTypes
        val builtShops = player.foundations.filter { it.hasModel && it.shopCard != null }

        // Shops that the guest visits
        val visitedShops = builtShops.filter { it.shopCard!!.type in guestShopTypes }
        val activations = mutableListOf<ShopActivation>()
        val linkageDetails = mutableListOf<LinkageDetail>()

        // Apply event: 门可罗雀 - only 1 shop
        val effectiveVisitedShops = if (event?.effect == EventEffect.MEN_KE_LUO_QUE && visitedShops.isNotEmpty()) {
            listOf(visitedShops.first())
        } else {
            visitedShops
        }

        var totalIncome = 0

        for (foundation in effectiveVisitedShops) {
            val shop = foundation.shopCard!!
            var shopIncome = when (shop.incomeType) {
                IncomeType.FIXED -> shop.baseIncome
                IncomeType.DICE -> DiceRoller.roll()
                IncomeType.MENU_BONUS -> guest.menuConsumption + shop.menuBonus
                IncomeType.HOUSING_COUNT -> builtShops.size
            }

            // Apply event: 无尖不商 -1
            if (event?.effect == EventEffect.WU_JIAN_BU_SHANG) {
                shopIncome = (shopIncome - 1).coerceAtLeast(0)
            }

            // Calculate linkage bonuses
            var linkageBonus = 0
            val shopDetails = mutableListOf<LinkageDetail>()

            when (shop.type) {
                ShopType.GUA_SI -> {
                    val guanPuCount = builtShops.count { it.shopCard?.type == ShopType.GUAN_PU }
                    if (guestShopTypes.contains(ShopType.GUAN_PU) && guanPuCount > 0) {
                        val bonus = guanPuCount * 2
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.GUAN_PU, ShopType.GUA_SI, 2, guanPuCount, bonus))
                    }
                }
                ShopType.SHU_FANG -> {
                    val chaGuanCount = builtShops.count { it.shopCard?.type == ShopType.CHA_GUAN }
                    if (guestShopTypes.contains(ShopType.CHA_GUAN) && chaGuanCount > 0) {
                        val bonus = chaGuanCount * 2
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.CHA_GUAN, ShopType.SHU_FANG, 2, chaGuanCount, bonus))
                    }
                }
                ShopType.JIU_SI -> {
                    val shuoShuCount = builtShops.count { it.shopCard?.type == ShopType.SHUO_SHU }
                    if (guestShopTypes.contains(ShopType.SHUO_SHU) && shuoShuCount > 0) {
                        val bonus = shuoShuCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHUO_SHU, ShopType.JIU_SI, 1, shuoShuCount, bonus))
                    }
                }
                ShopType.CHOU_DUAN -> {
                    val shouShiCount = builtShops.count { it.shopCard?.type == ShopType.SHOU_SHI }
                    if (guestShopTypes.contains(ShopType.SHOU_SHI) && shouShiCount > 0) {
                        val bonus = shouShiCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHOU_SHI, ShopType.CHOU_DUAN, 1, shouShiCount, bonus))
                    }
                }
                ShopType.CI_QI -> {
                    val shouShiCount = builtShops.count { it.shopCard?.type == ShopType.SHOU_SHI }
                    if (guestShopTypes.contains(ShopType.SHOU_SHI) && shouShiCount > 0) {
                        val bonus = shouShiCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHOU_SHI, ShopType.CI_QI, 1, shouShiCount, bonus))
                    }
                }
                ShopType.YIN_ZI -> {
                    // 关扑铺联动: when 关扑铺 is visited
                    val guanPuCount = builtShops.count { it.shopCard?.type == ShopType.GUAN_PU }
                    if (guestShopTypes.contains(ShopType.GUAN_PU) && guanPuCount > 0) {
                        val bonus = guanPuCount * 2
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.GUAN_PU, ShopType.YIN_ZI, 2, guanPuCount, bonus))
                    }
                    // 说书场联动
                    val shuoShuCount = builtShops.count { it.shopCard?.type == ShopType.SHUO_SHU }
                    if (guestShopTypes.contains(ShopType.SHUO_SHU) && shuoShuCount > 0) {
                        val bonus = shuoShuCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHUO_SHU, ShopType.YIN_ZI, 1, shuoShuCount, bonus))
                    }
                }
                ShopType.GUAN_PU -> {
                    // 饮子铺联动
                    val yinZiCount = builtShops.count { it.shopCard?.type == ShopType.YIN_ZI }
                    if (guestShopTypes.contains(ShopType.YIN_ZI) && yinZiCount > 0) {
                        val bonus = yinZiCount * 2
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.YIN_ZI, ShopType.GUAN_PU, 2, yinZiCount, bonus))
                    }
                }
                ShopType.CHA_GUAN -> {
                    val shuoShuCount = builtShops.count { it.shopCard?.type == ShopType.SHUO_SHU }
                    if (guestShopTypes.contains(ShopType.SHUO_SHU) && shuoShuCount > 0) {
                        val bonus = shuoShuCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHUO_SHU, ShopType.CHA_GUAN, 1, shuoShuCount, bonus))
                    }
                }
                ShopType.SHUO_SHU -> {
                    val shouShiCount = builtShops.count { it.shopCard?.type == ShopType.SHOU_SHI }
                    if (guestShopTypes.contains(ShopType.SHOU_SHI) && shouShiCount > 0) {
                        val bonus = shouShiCount * 1
                        linkageBonus += bonus
                        shopDetails.add(LinkageDetail(ShopType.SHOU_SHI, ShopType.SHUO_SHU, 1, shouShiCount, bonus))
                    }
                }
                ShopType.SHOU_SHI -> {
                    // No base linkage, but can be linked FROM 说书场/绸缎庄/瓷器铺
                }
                else -> {}
            }

            linkageDetails.addAll(shopDetails)

            val total = shopIncome + linkageBonus
            activations.add(ShopActivation(shop, shopIncome, linkageBonus, total))
            totalIncome += total
        }

        return ShopSettlementResult(activations, linkageDetails, totalIncome)
    }
}
