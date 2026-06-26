package com.dasong.commerce.ui.game.components.info

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import com.dasong.commerce.engine.TurnActionRecord
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.GamePhase

@Composable
fun InfoPanelButton(
    players: List<PlayerState>,
    currentPlayerIndex: Int = 0,
    currentPhase: GamePhase = GamePhase.BUY,
    turnHistory: List<String> = emptyList(),
    currentTurnRecord: TurnActionRecord = TurnActionRecord()
) {
    var showPopup by remember { mutableStateOf(false) }

    IconButton(onClick = { showPopup = true }) {
        Icon(Icons.Default.Info, contentDescription = "回合操作记录")
    }

    if (showPopup) {
        GameInfoPopup(
            players = players,
            currentPlayerIndex = currentPlayerIndex,
            currentPhase = currentPhase,
            turnHistory = turnHistory,
            currentTurnRecord = currentTurnRecord,
            onDismiss = { showPopup = false }
        )
    }
}
