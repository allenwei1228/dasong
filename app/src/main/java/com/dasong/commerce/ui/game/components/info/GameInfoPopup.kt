package com.dasong.commerce.ui.game.components.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.model.PlayerState

@Composable
fun GameInfoPopup(
    players: List<PlayerState>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 玩家信息总览") },
        text = {
            LazyColumn {
                items(players) { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "玩家${player.seatOrder} - ${player.name}",
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row {
                                Text("💰 资金: ")
                                Text("${player.funds}两", color = Color(0xFFB8860B))
                            }
                            Text("🏠 已建店铺:")
                            val builtShops = player.foundations
                                .filter { it.hasModel && it.shopCard != null }
                                .groupBy { it.shopCard!!.type }
                                .mapValues { it.value.size }

                            if (builtShops.isEmpty()) {
                                Text("  暂无", color = Color.Gray)
                            } else {
                                builtShops.forEach { (type, count) ->
                                    Text("  · ${type.displayName}: ${count}间")
                                }
                            }
                            Text(
                                "  总模型数: ${player.foundations.count { it.hasModel }}/8",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}
