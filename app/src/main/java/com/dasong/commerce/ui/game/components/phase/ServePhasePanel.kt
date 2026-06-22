package com.dasong.commerce.ui.game.components.phase

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.GuestCard

@Composable
fun ServePhasePanel(
    guestQueue: List<GuestCard>,
    currentPlayer: PlayerState,
    onSelectGuest: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "招待阶段 - 选择客人",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "当前资金: ${currentPlayer.funds}两",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(8.dp))

            // Show guests in order: position 1-4 (rightmost is position 1)
            guestQueue.reversed().forEachIndexed { index, guest ->
                val position = index + 1
                val cost = position - 1
                val canAfford = currentPlayer.funds >= cost

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "${guest.name} (位${position})",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "消耗${guest.menuConsumption}张菜单 · " +
                                        guest.shopTypes.joinToString(" ") { it.displayName },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                if (cost > 0) "小费: ${cost}两" else "免费招待",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Button(
                            onClick = { onSelectGuest(position) },
                            enabled = canAfford
                        ) {
                            Text("招待")
                        }
                    }
                }
            }

            if (guestQueue.isEmpty()) {
                Text(
                    "客人队列为空，需要刷新",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
