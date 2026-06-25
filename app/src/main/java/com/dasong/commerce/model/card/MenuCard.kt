package com.dasong.commerce.model.card

data class MenuCard(
    val id: Int,
    val name: String,
    val grade: MenuGrade,
    val cost: Int
) {
    val baseIncome: Int get() = when (grade) {
        MenuGrade.FOUR -> 1
        MenuGrade.THREE -> 2
        MenuGrade.TWO -> 3
        MenuGrade.ONE -> 2
    }



    fun cardGrade(grade: MenuGrade): String {
        return when (grade) {
            MenuGrade.ONE -> "一品"
            MenuGrade.TWO -> "二品"
            MenuGrade.THREE -> "三品"
            MenuGrade.FOUR -> "四品"
        }
    }
}
