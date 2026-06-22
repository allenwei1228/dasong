package com.dasong.commerce.ui.game.components.player

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.ui.theme.GoldHighlight

@Composable
fun PlayerPanel(
    player: PlayerState,
    isCurrentPlayer: Boolean
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val playerCount = 4 // Default estimate

    Card(
        modifier = Modifier
            .width((screenWidth * 0.85).dp)
            .border(
                width = if (isCurrentPlayer) 3.dp else 1.dp,
                color = if (isCurrentPlayer) GoldHighlight else Color.Gray,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlayer)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Player info + Funds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "玩家${player.seatOrder} - ${player.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                PlayerFundsBar(funds = player.funds)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            // Row 2: Refined Chamber + Kitchen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RefinedChamber(cards = player.refinedChamber)
                KitchenPile(cards = player.kitchen)
            }

            Spacer(Modifier.height(8.dp))

            // Row 3: Foundations
            Text(
                "地基 (总数${player.totalMenuCards}张)",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(4.dp))
            FoundationSlots(
                foundations = player.foundations,
                onSlotClick = { /* handled in buy phase */ }
            )
        }
    }
}
