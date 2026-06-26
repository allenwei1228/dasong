package com.dasong.commerce.model.card

import kotlinx.serialization.Serializable

@Serializable
data class EventCard(
    val id: Int,
    val name: String,
    val type: EventType,
    val description: String,
    val effect: EventEffect,
    val duration: EventDuration = EventDuration.CONTINUOUS
)
