package com.dasong.commerce.util

import kotlin.random.Random

object DiceRoller {
    fun roll(): Int = Random.nextInt(1, 7) // 1-6
}
