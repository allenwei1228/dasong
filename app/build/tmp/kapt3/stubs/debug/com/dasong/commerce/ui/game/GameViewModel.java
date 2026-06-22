package com.dasong.commerce.ui.game;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cJ\u001e\u0010\u001d\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u001aJ\u0006\u0010!\u001a\u00020\u0018J\u0006\u0010\"\u001a\u00020\u0018J\u000e\u0010#\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001aJ\u0006\u0010$\u001a\u00020\u0018J\u000e\u0010%\u001a\u00020\u00182\u0006\u0010&\u001a\u00020\u001aJ\u0016\u0010\'\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cJ\u0016\u0010(\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010)\u001a\u00020\u001aJ\u0006\u0010*\u001a\u00020\u0018R\u0016\u0010\u0005\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\n\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000e0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0019\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0010R\u0017\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\t0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0010R\u0019\u0010\u0015\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000b0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0010\u00a8\u0006+"}, d2 = {"Lcom/dasong/commerce/ui/game/GameViewModel;", "Landroidx/lifecycle/ViewModel;", "gameEngine", "Lcom/dasong/commerce/engine/GameEngine;", "(Lcom/dasong/commerce/engine/GameEngine;)V", "_settlementResult", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/dasong/commerce/ui/game/SettlementDisplayData;", "_showTurnTransition", "", "_winner", "", "gameState", "Lkotlinx/coroutines/flow/StateFlow;", "Lcom/dasong/commerce/engine/GameState;", "getGameState", "()Lkotlinx/coroutines/flow/StateFlow;", "settlementResult", "getSettlementResult", "showTurnTransition", "getShowTurnTransition", "winner", "getWinner", "buyMenuCard", "", "playerId", "", "card", "Lcom/dasong/commerce/model/card/MenuCard;", "buyShopCard", "shop", "Lcom/dasong/commerce/model/card/ShopCard;", "foundationIndex", "confirmTurnTransition", "dismissSettlement", "endBuyPhase", "endTurn", "initGame", "playerCount", "removeMenu", "selectGuest", "queuePosition", "skipPreparePhase", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class GameViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.GameEngine gameEngine = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.engine.GameState> gameState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _showTurnTransition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> showTurnTransition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.dasong.commerce.ui.game.SettlementDisplayData> _settlementResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.ui.game.SettlementDisplayData> settlementResult = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _winner = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> winner = null;
    
    @javax.inject.Inject()
    public GameViewModel(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.GameEngine gameEngine) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.engine.GameState> getGameState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getShowTurnTransition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.ui.game.SettlementDisplayData> getSettlementResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getWinner() {
        return null;
    }
    
    public final void initGame(int playerCount) {
    }
    
    public final void buyMenuCard(int playerId, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.MenuCard card) {
    }
    
    public final void buyShopCard(int playerId, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.ShopCard shop, int foundationIndex) {
    }
    
    public final void endBuyPhase(int playerId) {
    }
    
    public final void removeMenu(int playerId, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.model.card.MenuCard card) {
    }
    
    public final void skipPreparePhase() {
    }
    
    public final void selectGuest(int playerId, int queuePosition) {
    }
    
    public final void endTurn() {
    }
    
    public final void confirmTurnTransition() {
    }
    
    public final void dismissSettlement() {
    }
}