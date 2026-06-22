package com.dasong.commerce.model.card

data class GuestCard(
    val id: Int,
    val name: String,
    val menuConsumption: Int,
    val shopTypes: List<ShopType>,
    var tip: Int = 0  // 小费计数器：客人身上积累的小费，招待时归招待者所有
)
