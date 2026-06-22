package com.dasong.commerce.engine

import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.*

class WinConditionChecker {
    fun checkWin(player: PlayerState): Boolean {
        val shopsBuilt = player.foundations.count { it.hasModel && it.shopCard != null && it.isBuilt }
        return shopsBuilt >= 8 && player.funds >= 50
    }
}
