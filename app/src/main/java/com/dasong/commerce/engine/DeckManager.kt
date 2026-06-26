package com.dasong.commerce.engine

import com.dasong.commerce.model.CardDataProvider
import com.dasong.commerce.model.Foundation
import com.dasong.commerce.model.MenuPool
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.ShopPool
import com.dasong.commerce.model.card.*

open class DeckManager {

    private val allMenuCards = CardDataProvider.menuCards
    private val allShopCards = CardDataProvider.shopCards
    private val allGuestCards = CardDataProvider.guestCards
    private val allEventCards = CardDataProvider.eventCards

    fun initMenuPool(): MenuPool {
        val shuffled = allMenuCards.toMutableList()
        shuffled.shuffle()
        val pool = MenuPool()
        shuffled.forEach { card ->
            pool.getPile(card.grade).add(card)
        }
        return pool
    }

    fun initShopPool(): ShopPool {
        val shuffled = allShopCards.toMutableList()
        shuffled.shuffle()
        val pool = ShopPool()
        repeat(4) {
            if (shuffled.isNotEmpty()) {
                pool.available.add(shuffled.removeAt(0))
            }
        }
        return pool
    }

    /**
     * Create combined guest+event deck with events interspersed among guests.
     * Returns the full combined deck and the initial guest queue (first 4 guests).
     */
    open fun createCombinedDeck(): MutableList<DeckCard> {
        val combined = mutableListOf<DeckCard>()
        val guests = allGuestCards.toMutableList().also { it.shuffle() }
        val events = allEventCards.toMutableList().also { it.shuffle() }

        // Distribute events throughout the guest deck
        val totalGuests = guests.size
        val totalEvents = events.size
        val interval = if (totalEvents > 0) totalGuests / (totalEvents + 1) else totalGuests
        if (interval == 0) {
            // Every guest followed by an event
            guests.forEachIndexed { i, guest ->
                combined.add(DeckCard.Guest(guest))
                if (i < totalEvents) {
                    combined.add(DeckCard.Event(events[i]))
                }
            }
        } else {
            var guestIdx = 0
            var eventIdx = 0
            var counter = 0

            while (guestIdx < totalGuests) {
                combined.add(DeckCard.Guest(guests[guestIdx++]))
                counter++
                if (counter >= interval && eventIdx < totalEvents) {
                    combined.add(DeckCard.Event(events[eventIdx++]))
                    counter = 0
                }
            }
            while (eventIdx < totalEvents) {
                combined.add(DeckCard.Event(events[eventIdx++]))
            }
        }

        return combined
    }

    fun drawShopFromPool(shopPool: ShopPool): ShopCard? {
        val remaining = allShopCards.filter { s ->
            shopPool.available.none { it.id == s.id }
        }
        val shuffled = remaining.toMutableList().also { it.shuffle() }
        return if (shuffled.isNotEmpty()) shuffled[0] else null
    }

    fun getInitialMenuForPlayer(): List<MenuCard> {
        val fourGrade = allMenuCards.filter { it.grade == MenuGrade.FOUR }
        val threeGrade = allMenuCards.filter { it.grade == MenuGrade.THREE }

        val initial = mutableListOf<MenuCard>()
        initial.addAll(fourGrade.shuffled().take(6))
        initial.addAll(threeGrade.shuffled().take(2))
        initial.shuffle()
        return initial
    }

    fun getStartingFunds(seatOrder: Int): Int = when (seatOrder) {
        1 -> 5
        2 -> 6
        3 -> 7
        4 -> 8
        else -> 5
    }
}
