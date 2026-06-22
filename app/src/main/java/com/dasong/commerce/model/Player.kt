package com.dasong.commerce.model

import com.dasong.commerce.model.card.MenuCard
import com.dasong.commerce.model.card.ShopCard

data class Foundation(
    val index: Int,
    var shopCard: ShopCard? = null,
    var hasModel: Boolean = false,
    var isBuilt: Boolean = false  // 店铺房屋是否已购买（购买后效果才生效）
) {
    val clearCost: Int get() = when (index) {
        0 -> 0
        1 -> 2
        2 -> 3
        3 -> 6
        4 -> 7
        5 -> 8
        6 -> 9
        7 -> 10
        else -> throw IllegalArgumentException("最多8块地基")
    }
}

data class PlayerState(
    val id: Int,
    val name: String,
    val seatOrder: Int,
    var funds: Int,
    val refinedChamber: MutableList<MenuCard> = mutableListOf(),
    val kitchen: MutableList<MenuCard> = mutableListOf(),
    val foundations: MutableList<Foundation> = (0..7).map { Foundation(it) }.toMutableList()
) {
    val totalMenuCards: Int get() = refinedChamber.size + kitchen.size
    val canPrepare: Boolean get() = totalMenuCards > 6
}

data class MenuPool(
    val gradeOne: MutableList<MenuCard> = mutableListOf(),
    val gradeTwo: MutableList<MenuCard> = mutableListOf(),
    val gradeThree: MutableList<MenuCard> = mutableListOf(),
    val gradeFour: MutableList<MenuCard> = mutableListOf()
) {
    fun getPile(grade: com.dasong.commerce.model.card.MenuGrade): MutableList<MenuCard> = when (grade) {
        com.dasong.commerce.model.card.MenuGrade.ONE -> gradeOne
        com.dasong.commerce.model.card.MenuGrade.TWO -> gradeTwo
        com.dasong.commerce.model.card.MenuGrade.THREE -> gradeThree
        com.dasong.commerce.model.card.MenuGrade.FOUR -> gradeFour
    }

    val totalSize: Int get() = gradeOne.size + gradeTwo.size + gradeThree.size + gradeFour.size
}

data class ShopPool(
    val available: MutableList<ShopCard> = mutableListOf()
)
