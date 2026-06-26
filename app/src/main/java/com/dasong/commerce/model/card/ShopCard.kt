package com.dasong.commerce.model.card

import kotlinx.serialization.Serializable

@Serializable
data class ShopCard(
    val id: Int,
    val name: String,
    val type: ShopType,
    val buildCost: Int,
    val baseIncome: Int,
    val hasDiceMechanic: Boolean = false,
    val hasMenuBonus: Boolean = false,
    val menuBonus: Int = 0,
    val hasHousingBonus: Boolean = false,
    val incomeType: IncomeType = IncomeType.FIXED
)
