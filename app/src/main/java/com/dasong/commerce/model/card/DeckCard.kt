package com.dasong.commerce.model.card

import kotlinx.serialization.Serializable

/**
 * Sealed class representing cards in the guest deck.
 * The deck contains both GuestCards and EventCards mixed together.
 */
@Serializable
sealed class DeckCard {
    @Serializable
    data class Guest(val card: GuestCard) : DeckCard()
    @Serializable
    data class Event(val card: EventCard) : DeckCard()
}
