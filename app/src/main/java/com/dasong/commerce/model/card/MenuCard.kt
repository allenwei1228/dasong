package com.dasong.commerce.model.card

data class MenuCard(
    val id: Int,
    val name: String,
    val grade: MenuGrade,
    val cost: Int
) {
    val baseIncome: Int get() = when (grade) {
        MenuGrade.FOUR -> 2
        MenuGrade.THREE -> 2
        MenuGrade.TWO -> 3
        MenuGrade.ONE -> 2
    }
}
