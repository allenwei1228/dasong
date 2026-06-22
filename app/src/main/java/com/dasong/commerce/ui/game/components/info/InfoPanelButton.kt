package com.dasong.commerce.ui.game.components.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import com.dasong.commerce.model.PlayerState

@Composable
fun InfoPanelButton(players: List<PlayerState>) {
    var showPopup by remember { mutableStateOf(false) }

    IconButton(onClick = { showPopup = true }) {
        Icon(Icons.Default.Info, contentDescription = "玩家信息")
    }

    if (showPopup) {
        GameInfoPopup(
            players = players,
            onDismiss = { showPopup = false }
        )
    }
}
