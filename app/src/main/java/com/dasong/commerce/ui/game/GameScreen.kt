package com.dasong.commerce.ui.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    playerCount: Int,
    onGameEnd: (String) -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settlementResult by viewModel.settlementResult.collectAsStateWithLifecycle()
    val showTurnTransition by viewModel.showTurnTransition.collectAsStateWithLifecycle()
    val winner by viewModel.winner.collectAsStateWithLifecycle()

    LaunchedEffect(playerCount) {
        viewModel.initGame(playerCount)
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

        // Turn transition dialog
        if (showTurnTransition) {
            TurnTransitionDialog(
                nextPlayer = currentState.players[
                    (currentState.currentPlayerIndex + 1) % currentState.players.size
                ],
                onConfirm = { viewModel.confirmTurnTransition() }
            )
        }
    }
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
