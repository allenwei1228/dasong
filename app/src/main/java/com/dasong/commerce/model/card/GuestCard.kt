package com.dasong.commerce.model.card

data class GuestCard(
    val id: Int,
    val name: String,
    val menuConsumption: Int,
    val shopTypes: List<ShopType>
)
