package com.dasong.commerce.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasong.commerce.ui.game.components.info.InfoPanelButton
import com.dasong.commerce.ui.game.components.phase.CurrentPhasePanel
import com.dasong.commerce.ui.game.components.phase.PhaseIndicator
import com.dasong.commerce.ui.game.components.player.PlayerPanelsRow
import com.dasong.commerce.ui.game.components.public.EventCardDisplay
import com.dasong.commerce.ui.game.components.public.PublicAreaPanel
import com.dasong.commerce.ui.game.components.settlement.SettlementDialog
import com.dasong.commerce.engine.GameState
import com.dasong.commerce.engine.WinConditionChecker
import com.dasong.commerce.model.Foundation
import com.dasong.commerce.model.card.ShopCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    playerCount: Int,
    playerNames: List<String> = emptyList(),
    onGameEnd: (String) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settlementResult by viewModel.settlementResult.collectAsStateWithLifecycle()
    val showTurnTransition by viewModel.showTurnTransition.collectAsStateWithLifecycle()
    val winner by viewModel.winner.collectAsStateWithLifecycle()
    val showMenKeLuoQueDialog by viewModel.showMenKeLuoQueDialog.collectAsStateWithLifecycle()
    val menKeLuoQueShops by viewModel.menKeLuoQueShops.collectAsStateWithLifecycle()

    LaunchedEffect(playerCount) {
        viewModel.initGame(playerCount, playerNames)
    }

    LaunchedEffect(winner) {
        winner?.let { onGameEnd(it) }
    }

    val currentState = gameState ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "玩家${currentState.currentPlayer.seatOrder} - ${currentState.currentPlayer.name}"
                    )
                },
                actions = {
                    InfoPanelButton(players = currentState.players)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PhaseIndicator(currentPhase = currentState.currentPhase)

            PublicAreaPanel(state = currentState)

            EventCardDisplay(event = currentState.activeEvent)

            PlayerPanelsRow(state = currentState)

            Spacer(Modifier.height(8.dp))

            CurrentPhasePanel(
                state = currentState,
                viewModel = viewModel
            )
        }

        // Settlement Dialog
        settlementResult?.let { result ->
            SettlementDialog(
                data = result,
                onDismiss = { viewModel.dismissSettlement() }
            )
        }

        // Event announcement dialog
        if (currentState.announceEvent != null && currentState.turnStep == com.dasong.commerce.model.card.TurnStep.PHASE_3_EVENT_ANNOUNCE) {
            EventAnnouncementDialog(
                event = currentState.announceEvent!!,
                players = currentState.players,
                isOverriding = currentState.activeEvent != null &&
                        currentState.announceEvent?.duration == com.dasong.commerce.model.card.EventDuration.CONTINUOUS,
                onConfirm = { viewModel.confirmEventAnnouncement() }
            )
        }

        // Turn transition dialog
        if (showTurnTransition) {
            TurnTransitionDialog(
                nextPlayer = currentState.players[
                    (currentState.currentPlayerIndex + 1) % currentState.players.size
                ],
                onConfirm = { viewModel.confirmTurnTransition() }
            )
        }

        // 门可罗雀：选择结算店铺弹窗
        if (showMenKeLuoQueDialog) {
            MenKeLuoQueDialog(
                shops = menKeLuoQueShops,
                onSelect = { foundationIndex ->
                    viewModel.onMenKeLuoQueShopSelected(foundationIndex)
                }
            )
        }
    }
}

@Composable
fun EventAnnouncementDialog(
    event: com.dasong.commerce.model.card.EventCard,
    players: List<com.dasong.commerce.model.PlayerState>,
    isOverriding: Boolean,
    onConfirm: () -> Unit
) {
    // 苛捐杂税：计算每位玩家需要支付的税额
    val keJuanZaShuiDetails = if (event.effect == com.dasong.commerce.model.card.EventEffect.KE_JUAN_ZA_SHUI) {
        players.map { player ->
            val modelCount = player.foundations.count { it.hasModel && it.isBuilt }
            val tax = modelCount * 2
            Triple(player.name, modelCount, tax)
        }
    } else {
        emptyList()
    }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                when {
                    isOverriding -> "⚠️ 新事件覆盖"
                    event.duration == com.dasong.commerce.model.card.EventDuration.IMMEDIATE -> "⚡ 即时事件"
                    else -> "📜 持续事件"
                }
            )
        },
        text = {
            Column {
                Text(
                    "【${event.name}】",
                    style = MaterialTheme.typography.headlineSmall,
                    color = when (event.type) {
                        com.dasong.commerce.model.card.EventType.POSITIVE -> MaterialTheme.colorScheme.primary
                        com.dasong.commerce.model.card.EventType.NEGATIVE -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    event.description,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (event.duration == com.dasong.commerce.model.card.EventDuration.CONTINUOUS) "持续生效" else "立即生效后丢弃",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isOverriding) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "之前的事件效果将被替换",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                // 苛捐杂税：展示每位玩家需支付的税额
                if (keJuanZaShuiDetails.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "每位玩家需支付：",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    keJuanZaShuiDetails.forEach { (name, modelCount, tax) ->
                        Text(
                            "  $name：${modelCount}个店铺模型 × 2 = $tax 两",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("确认")
            }
        }
    )
}

@Composable
fun TurnTransitionDialog(
    nextPlayer: com.dasong.commerce.model.PlayerState,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("回合结束") },
        text = {
            Column {
                Text("当前回合已完成，请将设备交给：")
                Spacer(Modifier.height(12.dp))
                Text(
                    "玩家${nextPlayer.seatOrder} - ${nextPlayer.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("确认")
            }
        }
    )
}

@Composable
fun MenKeLuoQueDialog(
    shops: List<Foundation>,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                "门可罗雀",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    "门庭冷落，只能选择一个店铺结算收入：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                shops.forEach { foundation ->
                    val shop = foundation.shopCard ?: return@forEach
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "${shop.name} (#${foundation.index + 1}号地)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "类型: ${shop.type.displayName} | 收入: ${shop.baseIncome}两",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onSelect(foundation.index) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("选择此店铺")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {} // 不显示确认按钮，由各店铺的选择按钮代替
    )
}
