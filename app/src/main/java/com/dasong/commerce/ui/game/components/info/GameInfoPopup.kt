package com.dasong.commerce.ui.game.components.info

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dasong.commerce.engine.TurnActionRecord
import com.dasong.commerce.model.PlayerState
import com.dasong.commerce.model.card.GamePhase

@Composable
fun GameInfoPopup(
    players: List<PlayerState>,
    currentPlayerIndex: Int = 0,
    currentPhase: GamePhase = GamePhase.BUY,
    turnHistory: List<String> = emptyList(),
    currentTurnRecord: TurnActionRecord = TurnActionRecord(),
    onDismiss: () -> Unit
) {
    val currentPlayer = players.getOrNull(currentPlayerIndex)

    // 已完成的其他玩家
    val otherPlayersHistory = if (currentPlayer != null) {
        turnHistory.filter { !it.startsWith("${currentPlayer.name}:") }
    } else {
        turnHistory
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📋 本轮操作记录") },
        text = {
            Column(modifier = Modifier.heightIn(max = 500.dp)) {
                if (currentPlayer != null) {
                    Text(
                        "当前回合: ${currentPlayer.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                }

                LazyColumn {
                    // 1) 当前玩家：显示本回合进行中的按阶段操作
                    if (currentPlayer != null) {
                        item {
                            PlayerPhaseCard(
                                playerName = "${currentPlayer.name}（进行中）",
                                isCurrent = true,
                                buyActions = currentTurnRecord.buyPhase,
                                prepareActions = currentTurnRecord.preparePhase,
                                serveActions = currentTurnRecord.servePhase,
                                currentPhase = currentPhase
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // 2) 已完成的玩家
                    if (otherPlayersHistory.isNotEmpty()) {
                        item {
                            Text(
                                "其他玩家操作记录:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        itemsIndexed(otherPlayersHistory) { _, history ->
                            PlayerHistoryCard(history = history)
                        }
                    } else if (currentPlayer == null || currentTurnRecord.isEmpty()) {
                        item {
                            Text(
                                "本轮暂无其他玩家操作记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * 已完成的玩家操作记录卡片（解析 turnHistory 字符串）
 * 新格式: "玩家名: 购买:操作1、操作2；备菜:操作3；招待:操作4"
 */
@Composable
private fun PlayerHistoryCard(history: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 解析摘要
            val colonIndex = history.indexOf(':')
            if (colonIndex > 0) {
                val playerName = history.substring(0, colonIndex)
                val rest = history.substring(colonIndex + 1)

                Text(
                    playerName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))

                // 按"；"分割各阶段
                val phaseParts = rest.split("；")
                phaseParts.forEach { phasePart ->
                    val phaseColon = phasePart.indexOf(':')
                    if (phaseColon > 0) {
                        val phaseName = phasePart.substring(0, phaseColon)
                        val actions = phasePart.substring(phaseColon + 1)
                        PhaseSection(phaseName = phaseName, actions = actions.split("、"))
                    } else {
                        // 兼容旧格式（无阶段分组）
                        Text(
                            "  · $phasePart",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(history, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

/**
 * 当前玩家操作记录卡片（使用 TurnActionRecord）
 */
@Composable
private fun PlayerPhaseCard(
    playerName: String,
    isCurrent: Boolean,
    buyActions: List<String>,
    prepareActions: List<String>,
    serveActions: List<String>,
    currentPhase: GamePhase
) {
    val hasAnyActions = buyActions.isNotEmpty() || prepareActions.isNotEmpty() || serveActions.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                playerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCurrent) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))

            if (!hasAnyActions) {
                Text(
                    "暂无操作",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // 阶段1·购买
                if (buyActions.isNotEmpty()) {
                    PhaseSection(
                        phaseName = "购买",
                        actions = buyActions,
                        isActive = currentPhase == GamePhase.BUY
                    )
                }
                // 阶段2·备菜
                if (prepareActions.isNotEmpty()) {
                    PhaseSection(
                        phaseName = "备菜",
                        actions = prepareActions,
                        isActive = currentPhase == GamePhase.PREPARE
                    )
                }
                // 阶段3·招待
                if (serveActions.isNotEmpty()) {
                    PhaseSection(
                        phaseName = "招待",
                        actions = serveActions,
                        isActive = currentPhase == GamePhase.SERVE
                    )
                }
            }
        }
    }
}

@Composable
private fun PhaseSection(
    phaseName: String,
    actions: List<String>,
    isActive: Boolean = false
) {
    Text(
        "▸ 阶段·$phaseName${if (isActive) "（当前）" else ""}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
        color = if (isActive)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant
    )
    actions.forEach { action ->
        Text(
            "    · $action",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(Modifier.height(2.dp))
}

private fun TurnActionRecord.isEmpty(): Boolean =
    buyPhase.isEmpty() && preparePhase.isEmpty() && servePhase.isEmpty()
