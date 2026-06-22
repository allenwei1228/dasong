package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dasong.commerce.engine.GameState

@Composable
fun PlayerPanelsRow(state: GameState) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.players) { player ->
            PlayerPanel(
                player = player,
                isCurrentPlayer = player.id == state.currentPlayer.id
            )
        }
    }
}
