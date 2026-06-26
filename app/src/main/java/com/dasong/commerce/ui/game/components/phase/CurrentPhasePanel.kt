package com.dasong.commerce.ui.game.components.phase

import androidx.compose.runtime.Composable
import com.dasong.commerce.engine.GameState
import com.dasong.commerce.model.card.GamePhase
import com.dasong.commerce.ui.game.GameViewModel

@Composable
fun CurrentPhasePanel(
    state: GameState,
    viewModel: GameViewModel,
    enabled: Boolean = true
) {
    val currentPlayer = state.currentPlayer

    when (state.currentPhase) {
        GamePhase.BUY -> BuyPhasePanel(
            player = currentPlayer,
            menuPool = state.menuPool,
            shopPool = state.shopPool,
            menuBoughtThisTurn = state.menuBoughtThisTurn,
            shopPlacedThisTurn = state.shopPlacedThisTurn,
            onBuyMenu = { card -> viewModel.buyMenuCard(currentPlayer.id, card) },
            onPlaceShop = { shop, foundationIndex ->
                viewModel.placeShopCard(currentPlayer.id, shop, foundationIndex)
            },
            onBuildHouse = { foundationIndex ->
                viewModel.buildShopHouse(currentPlayer.id, foundationIndex)
            },
            onEndPhase = { viewModel.endBuyPhase(currentPlayer.id) },
            enabled = enabled
        )
        GamePhase.PREPARE -> {
            val canPrepare = currentPlayer.canPrepare
            PreparePhasePanel(
                player = currentPlayer,
                canPrepare = canPrepare,
                onRemove = { card -> viewModel.removeMenu(currentPlayer.id, card) },
                onSkip = { viewModel.skipPreparePhase() },
                enabled = enabled
            )
        }
        GamePhase.SERVE -> ServePhasePanel(
            guestQueue = state.guestQueue,
            currentPlayer = currentPlayer,
            onSelectGuest = { pos -> viewModel.selectGuest(currentPlayer.id, pos) },
            enabled = enabled
        )
    }
}
