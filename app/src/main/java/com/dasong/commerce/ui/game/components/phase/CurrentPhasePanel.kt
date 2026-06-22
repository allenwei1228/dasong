package com.dasong.commerce.ui.game.components.phase

import androidx.compose.runtime.Composable
import com.dasong.commerce.engine.GameState
import com.dasong.commerce.model.card.GamePhase
import com.dasong.commerce.ui.game.GameViewModel

@Composable
fun CurrentPhasePanel(
    state: GameState,
    viewModel: GameViewModel
) {
    val currentPlayer = state.currentPlayer

    when (state.currentPhase) {
        GamePhase.BUY -> BuyPhasePanel(
            player = currentPlayer,
            menuPool = state.menuPool,
            shopPool = state.shopPool,
            onBuyMenu = { card -> viewModel.buyMenuCard(currentPlayer.id, card) },
            onBuyShop = { shop, foundationIndex ->
                viewModel.buyShopCard(currentPlayer.id, shop, foundationIndex)
            },
            onEndPhase = { viewModel.endBuyPhase(currentPlayer.id) }
        )
        GamePhase.PREPARE -> {
            val canPrepare = currentPlayer.canPrepare
            PreparePhasePanel(
                player = currentPlayer,
                canPrepare = canPrepare,
                onRemove = { card -> viewModel.removeMenu(currentPlayer.id, card) },
                onSkip = { viewModel.skipPreparePhase() }
            )
        }
        GamePhase.SERVE -> ServePhasePanel(
            guestQueue = state.guestQueue,
            currentPlayer = currentPlayer,
            onSelectGuest = { pos -> viewModel.selectGuest(currentPlayer.id, pos) }
        )
    }
}
