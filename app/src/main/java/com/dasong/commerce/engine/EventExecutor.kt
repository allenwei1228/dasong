package com.dasong.commerce.engine

import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.*
import com.dasong.commerce.model.MenuPool

class EventExecutor {

    /**
     * Revert the effects of the previous event before it gets replaced.
     */
    fun executeDeactivation(
        event: EventCard,
        guestQueue: MutableList<GuestCard>
    ) {
        when (event.effect) {
            EventEffect.ZHANG_DENG_JIE_CAI -> {
                // 队列上限从6恢复到4：移除最右侧多余客人
                while (guestQueue.size > 4) {
                    guestQueue.removeAt(guestQueue.lastIndex)
                }
            }
            EventEffect.YIN_ZHUANG_SU_GUO -> {
                // 队列上限从2恢复到4：无需处理，drawNextFromDeck 会自动补齐
            }
            else -> {
                // JIAN_YI_YANG_DE, MEN_KE_LUO_QUE, WU_JIAN_BU_SHANG, SHI_HE_NIAN_FENG
                // 持续效果在 SettlementEngine 中处理，无需额外撤销
            }
        }
    }

    /**
     * Execute the one-time activation effect when a continuous event becomes active.
     * Ongoing effects (settlement modifications) are handled in SettlementEngine.
     */
    fun executeActivation(
        event: EventCard,
        guestQueue: MutableList<GuestCard>,
        guestDeck: MutableList<DeckCard>
    ) {
        when (event.effect) {
            EventEffect.ZHANG_DENG_JIE_CAI -> {
                // 队列扩为总共6张：从牌堆中抽Guest牌填满到6人（新客人排到队尾 index 0）
                while (guestQueue.size < 6 && guestDeck.isNotEmpty()) {
                    val card = guestDeck.removeAt(0)
                    if (card is DeckCard.Guest) {
                        guestQueue.add(0, card.card)
                    }
                    // Skip events in deck during activation fill
                }
            }
            EventEffect.YIN_ZHUANG_SU_GUO -> {
                // 队列减少到总共2张：移除最右侧客人直到只剩2个
                while (guestQueue.size > 2) {
                    guestQueue.removeAt(guestQueue.lastIndex)
                }
            }
            else -> {
                // JIAN_YI_YANG_DE, MEN_KE_LUO_QUE, WU_JIAN_BU_SHANG, SHI_HE_NIAN_FENG
                // Ongoing effects handled in SettlementEngine
            }
        }
    }

    /**
     * Execute an immediate event effect. The event card is discarded after execution.
     */
    fun executeImmediate(
        event: EventCard,
        players: List<PlayerState>,
        guestQueue: MutableList<GuestCard>,
        guestDeck: MutableList<DeckCard>,
        menuPool: MenuPool
    ) {
        when (event.effect) {
            EventEffect.CI_JIU_YING_XIN -> {
                // Reshuffle queue: clear all guests in queue
                guestQueue.clear()
            }
            EventEffect.KE_JUAN_ZA_SHUI -> {
                // Each built shop pays 2 coins
                players.forEach { player ->
                    val modelCount = player.foundations.count { it.hasModel && it.isBuilt }
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
            else -> {
                // Other effects are continuous-only, should not be called here
            }
        }
    }
}
