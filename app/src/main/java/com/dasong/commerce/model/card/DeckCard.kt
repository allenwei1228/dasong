package com.dasong.commerce.model.card

/**
 * Sealed class representing cards in the guest deck.
 * The deck contains both GuestCards and EventCards mixed together.
 */
sealed class DeckCard {
    data class Guest(val card: GuestCard) : DeckCard()
    data class Event(val card: EventCard) : DeckCard()
}
