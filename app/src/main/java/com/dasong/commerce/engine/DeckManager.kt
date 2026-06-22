package com.dasong.commerce.engine

import com.dasong.commerce.model.CardDataProvider
import com.dasong.commerce.model.Foundation
import com.dasong.commerce.model.MenuPool
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.ShopPool
import com.dasong.commerce.model.card.*

class DeckManager {

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

    fun initGuestDeck(): Pair<MutableList<GuestCard>, MutableList<GuestCard>> {
        val guestDeck = allGuestCards.toMutableList()
        val eventCards = allEventCards.toMutableList()
        guestDeck.shuffle()
        eventCards.shuffle()

        // Combine guests + events, with events interspersed
        val combined = mutableListOf<Any>()
        val guestIterator = guestDeck.iterator()
        val eventIterator = eventCards.iterator()

        // Insert events at intervals
        var eventInterval = guestDeck.size / (eventCards.size + 1)
        var counter = 0
        while (guestIterator.hasNext()) {
            combined.add(guestIterator.next())
            counter++
            if (counter >= eventInterval && eventIterator.hasNext()) {
                combined.add(eventIterator.next())
                counter = 0
            }
        }
        while (eventIterator.hasNext()) {
            combined.add(eventIterator.next())
        }

        // Draw initial queue
        val queue = mutableListOf<GuestCard>()
        val deck = mutableListOf<GuestCard>()

        for (item in combined) {
            when (item) {
                is GuestCard -> {
                    if (queue.size < 4) {
                        queue.add(item)
                    } else {
                        deck.add(item)
                    }
                }
                is EventCard -> deck.add(GuestCard(-item.id, item.name, 0, emptyList()))
                // Mark as "skip" placeholder - actually we need a different approach
            }
        }

        // Simpler approach: just shuffle guests, and intermix events separately
        return Pair(guestDeck, queue)
    }

    /**
     * Create combined guest+event deck properly
     */
    fun createCombinedDeck(): MutableList<Any> {
        val combined = mutableListOf<Any>()
        val guests = allGuestCards.toMutableList().also { it.shuffle() }
        val events = allEventCards.toMutableList().also { it.shuffle() }

        // Distribute events throughout the guest deck
        val totalGuests = guests.size
        val totalEvents = events.size
        val interval = totalGuests / (totalEvents + 1)

        var guestIdx = 0
        var eventIdx = 0
        var counter = 0

        while (guestIdx < totalGuests) {
            combined.add(guests[guestIdx++])
            counter++
            if (counter >= interval && eventIdx < totalEvents) {
                combined.add(events[eventIdx++])
                counter = 0
            }
        }
        while (eventIdx < totalEvents) {
            combined.add(events[eventIdx++])
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
