package com.dasong.commerce.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dasong.commerce.ui.game.components.info.InfoPanelButton
import com.dasong.commerce.ui.game.components.phase.CurrentPhasePanel
import com.dasong.commerce.ui.game.components.phase.PhaseIndicator
import com.dasong.commerce.ui.game.components.player.PlayerPanelsRow
import com.dasong.commerce.ui.game.components.public.EventCardDisplay
import com.dasong.commerce.ui.game.components.public.PublicAreaPanel
import com.dasong.commerce.ui.game.components.settlement.DiceRollAnimationDialog
import com.dasong.commerce.ui.game.components.settlement.SettlementDialog
import com.dasong.commerce.engine.GameState
import com.dasong.commerce.engine.WinConditionChecker
import com.dasong.commerce.model.Foundation
import com.dasong.commerce.model.card.ShopCard
import com.dasong.commerce.online.OnlineViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    playerCount: Int,
    playerNames: List<String> = emptyList(),
    onGameEnd: (String) -> Unit,
    onBackToHome: () -> Unit = {},
    viewModel: GameViewModel = hiltViewModel(),
    onlineViewModel: OnlineViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settlementResult by viewModel.settlementResult.collectAsStateWithLifecycle()
    val showTurnTransition by viewModel.showTurnTransition.collectAsStateWithLifecycle()
    val winner by viewModel.winner.collectAsStateWithLifecycle()
    val showMenKeLuoQueDialog by viewModel.showMenKeLuoQueDialog.collectAsStateWithLifecycle()
    val menKeLuoQueShops by viewModel.menKeLuoQueShops.collectAsStateWithLifecycle()
    val isOnlineMode by viewModel.isOnlineMode.collectAsStateWithLifecycle()
    val isMyTurn by viewModel.isMyTurn.collectAsStateWithLifecycle()
    val waitingMessage by viewModel.waitingMessage.collectAsStateWithLifecycle()
    val showMyTurnReminder by viewModel.showMyTurnReminder.collectAsStateWithLifecycle()
    val showDiceRoll by viewModel.showDiceRoll.collectAsStateWithLifecycle()
    val pendingDiceCount by viewModel.pendingDiceCount.collectAsStateWithLifecycle()
    val diceSources by viewModel.diceSources.collectAsStateWithLifecycle()
    val shouldExitToHome by viewModel.shouldExitToHome.collectAsStateWithLifecycle()
    val disbandMessage by viewModel.disbandMessage.collectAsStateWithLifecycle()
    val showGameStartNotification by viewModel.showGameStartNotification.collectAsStateWithLifecycle()
    val gameStartInfo by viewModel.gameStartInfo.collectAsStateWithLifecycle()
    val showReconnectSummary by viewModel.showReconnectSummary.collectAsStateWithLifecycle()
    val reconnectSummaryHistory by viewModel.reconnectSummaryHistory.collectAsStateWithLifecycle()

    var showExitConfirm by remember { mutableStateOf(false) }
    var reconnectTimeout by remember { mutableStateOf(false) }

    LaunchedEffect(playerCount) {
        viewModel.initGame(playerCount, playerNames)
    }

    LaunchedEffect(winner) {
        winner?.let { onGameEnd(it) }
    }

    LaunchedEffect(shouldExitToHome) {
        if (shouldExitToHome) {
            onBackToHome()
        }
    }

    // 联机模式系统返回键：断开连接而非退出
    if (isOnlineMode) {
        BackHandler {
            onlineViewModel.disconnect()
            onGameEnd("") // 触发导航回到房间
        }
    }

    // 重连等待：gameState 还未从服务器同步下来
    if (gameState == null && isOnlineMode) {
        // 超时检测：15 秒后显示重试选项
        LaunchedEffect(Unit) {
            delay(15_000)
            if (gameState == null) {
                reconnectTimeout = true
            }
        }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("正在重新连接游戏...", style = MaterialTheme.typography.bodyLarge)
                if (reconnectTimeout) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "连接超时，请检查网络后重试",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = {
                        reconnectTimeout = false
                        onGameEnd("")
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("返回重新加入")
                    }
                }
            }
        }
        return
    }
    
    val currentState = gameState ?: return

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "玩家${currentState.currentPlayer.seatOrder} - ${currentState.currentPlayer.name}"
                            )
                            if (isOnlineMode) {
                                Text(
                                    if (isMyTurn) "🔵 你的回合" else "⏳ ${waitingMessage}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isMyTurn)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        InfoPanelButton(
                            players = currentState.players,
                            currentPlayerIndex = currentState.currentPlayerIndex,
                            currentPhase = currentState.currentPhase,
                            turnHistory = currentState.turnHistory,
                            currentTurnRecord = currentState.currentTurnRecord
                        )
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
                    viewModel = viewModel,
                    enabled = !isOnlineMode || isMyTurn
                )
            }

            // 联机模式下，仅当前回合玩家显示交互弹窗
            val showPlayerDialogs = !isOnlineMode || isMyTurn

            // Dice Roll Animation Dialog — 骰子投掷交互
            if (showPlayerDialogs && showDiceRoll && pendingDiceCount > 0) {
                DiceRollAnimationDialog(
                    diceCount = pendingDiceCount,
                    diceSources = diceSources,
                    onRollComplete = { diceValues ->
                        viewModel.onDiceRollComplete(diceValues)
                    }
                )
            }

            // Settlement Dialog
            if (showPlayerDialogs) {
                settlementResult?.let { result ->
                    SettlementDialog(
                        data = result,
                        onDismiss = { viewModel.dismissSettlement() }
                    )
                }
            }

            // Event announcement dialog — 所有玩家都弹窗通知，所有人均可确认
            val eventDialogVisible = currentState.announceEvent != null &&
                    currentState.turnStep == com.dasong.commerce.model.card.TurnStep.PHASE_3_EVENT_ANNOUNCE
            if (eventDialogVisible) {
                EventAnnouncementDialog(
                    event = currentState.announceEvent!!,
                    players = currentState.players,
                    isOverriding = currentState.activeEvent != null &&
                            currentState.announceEvent?.duration == com.dasong.commerce.model.card.EventDuration.CONTINUOUS,
                    onConfirm = { viewModel.confirmEventAnnouncement() }
                )
            }

            // Turn transition dialog（仅单机模式显示）
            if (showTurnTransition) {
                TurnTransitionDialog(
                    nextPlayer = currentState.players[
                        (currentState.currentPlayerIndex + 1) % currentState.players.size
                    ],
                    onConfirm = { viewModel.confirmTurnTransition() }
                )
            }

            // 回合切换后提醒“该我操作了”（单机/联机通用）
            if (showMyTurnReminder) {
                MyTurnReminderDialog(
                    player = currentState.currentPlayer,
                    onDismiss = { viewModel.dismissMyTurnReminder() }
                )
            }

            // 门可罗雀：选择结算店铺弹窗
            if (showPlayerDialogs && showMenKeLuoQueDialog) {
                MenKeLuoQueDialog(
                    shops = menKeLuoQueShops,
                    onSelect = { foundationIndex ->
                        viewModel.onMenKeLuoQueShopSelected(foundationIndex)
                    }
                )
            }


            // 游戏解散提示弹窗（联机模式其他玩家退出时触发）
            disbandMessage?.let { message ->
                GameDisbandedDialog(
                    message = message,
                    onConfirm = { viewModel.dismissDisbandMessage() }
                )
            }

            // 游戏开始通知弹窗：告知玩家序号和初始资金
            if (showGameStartNotification) {
                gameStartInfo?.let { info ->
                    GameStartNotificationDialog(
                        info = info,
                        onDismiss = { viewModel.dismissGameStartNotification() }
                    )
                }
            }

            // 重连摘要弹窗：离开期间的游戏进展
            if (showReconnectSummary) {
                ReconnectSummaryDialog(
                    history = reconnectSummaryHistory,
                    currentPlayerName = currentState.currentPlayer.name,
                    onDismiss = { viewModel.dismissReconnectSummary() }
                )
            }
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
fun MyTurnReminderDialog(
    player: com.dasong.commerce.model.PlayerState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🔔 轮到你了",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                Text(
                    "${player.name}，该我操作了！",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "确认后开始你的操作回合。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("开始操作")
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

@Composable
fun ExitConfirmDialog(
    isOnlineMode: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("退出游戏") },
        text = {
            Text(
                if (isOnlineMode)
                    "确定要退出游戏吗？退出后其他玩家可以继续游戏，你可以通过房间号重新加入。"
                else
                    "确定要退出游戏吗？游戏进度将不会保存。"
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("退出")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun GameDisbandedDialog(
    message: String,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                "游戏已解散",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("返回主页")
            }
        }
    )
}

/**
 * 游戏开始通知弹窗：展示所有玩家的随机顺序分配和初始资金。
 * 点击确认后进入游戏页面。
 */
@Composable
fun GameStartNotificationDialog(
    info: GameStartInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(
                "🎮 游戏开始",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "玩家顺序随机分配结果：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))

                info.players.forEach { player ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "玩家${player.seatOrder}为${player.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "初始资金 ${player.funds}两",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "点击确认后进入游戏页面。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("开始游戏")
            }
        }
    )
}

/**
 * 重连摘要弹窗：展示玩家离开期间游戏发生了什么。
 */
@Composable
fun ReconnectSummaryDialog(
    history: List<String>,
    currentPlayerName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "🔗 重连成功",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "你离开期间，游戏发生了以下操作：",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Text(
                        "暂无操作记录（游戏刚开始）。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // 显示最近 10 条记录
                    val recentHistory = history.takeLast(10)
                    recentHistory.forEach { line ->
                        Text(
                            text = "· $line",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    if (history.size > 10) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "... 共 ${history.size} 条操作记录",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                Text(
                    "当前轮到：$currentPlayerName",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("继续游戏")
            }
        }
    )
}
