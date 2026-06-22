package com.dasong.commerce.engine

import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.*
import com.dasong.commerce.model.MenuPool

class EventExecutor {

    fun execute(
        event: EventCard,
        players: List<PlayerState>,
        guestQueue: MutableList<GuestCard>,
        menuPool: MenuPool
    ) {
        when (event.effect) {
            EventEffect.JIAN_YI_YANG_DE -> {
                // Menu consumption -1 (handled in settlement)
            }
            EventEffect.MEN_KE_LUO_QUE -> {
                // Only 1 shop settlement (handled in settlement)
            }
            EventEffect.WU_JIAN_BU_SHANG -> {
                // Shop income -1 (handled in settlement)
            }
            EventEffect.SHI_HE_NIAN_FENG -> {
                // Menu consumption +1 (handled in settlement)
            }
            EventEffect.ZHANG_DENG_JIE_CAI -> {
                // Queue expanded to 6 - just add more guests
                while (guestQueue.size < 6) {
                    guestQueue.add(
                        GuestCard(999, "临时客人", 2, listOf(ShopType.YIN_ZI))
                    )
                }
            }
            EventEffect.YIN_ZHUANG_SU_GUO -> {
                // Discard rightmost 2 guests
                repeat(2) {
                    if (guestQueue.isNotEmpty()) {
                        guestQueue.removeAt(guestQueue.lastIndex)
                    }
                }
            }
            EventEffect.CI_JIU_YING_XIN -> {
                // Reshuffle queue
                guestQueue.clear()
            }
            EventEffect.KE_JUAN_ZA_SHUI -> {
                // Each model pays 2 coins
                players.forEach { player ->
                    val modelCount = player.foundations.count { it.hasModel }
                    val tax = modelCount * 2
                    player.funds = (player.funds - tax).coerceAtLeast(0)
                }
            }
            EventEffect.SHUO_GUO_LEI_LEI -> {
                // Each player gets 2 four-grade menu cards
                players.forEach { player ->
                    val fourGradeCards = menuPool.gradeFour.toList()
                    if (fourGradeCards.size >= 2) {
                        val taken = fourGradeCards.take(2)
                        menuPool.gradeFour.removeAll(taken.toSet())
                        player.kitchen.addAll(taken)
                    }
                }
            }
        }
    }
}
