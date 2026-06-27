package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dasong.commerce.engine.GameState

@Composable
fun PlayerPanelsRow(state: GameState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        state.players.forEach { player ->
            PlayerPanel(
                player = player,
                isCurrentPlayer = player.id == state.currentPlayer.id
            )
        }
    }
}
