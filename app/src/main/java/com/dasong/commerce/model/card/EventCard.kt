package com.dasong.commerce.model.card

data class EventCard(
    val id: Int,
    val name: String,
    val type: EventType,
    val description: String,
    val effect: EventEffect
)
