package com.dasong.commerce.ui.game;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000,\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a.\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a\u001e\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00010\rH\u0007\u00a8\u0006\u000e"}, d2 = {"GameScreen", "", "playerCount", "", "onGameEnd", "Lkotlin/Function1;", "", "viewModel", "Lcom/dasong/commerce/ui/game/GameViewModel;", "TurnTransitionDialog", "nextPlayer", "Lcom/dasong/commerce/model/PlayerState;", "onConfirm", "Lkotlin/Function0;", "app_debug"})
public final class GameScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void GameScreen(int playerCount, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onGameEnd, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.ui.game.GameViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TurnTransitionDialog(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.PlayerState nextPlayer, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onConfirm) {
    }
}