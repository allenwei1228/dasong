package com.dasong.commerce.engine;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B7\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\b\b\u0002\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019J\u001e\u0010\u001a\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u001d\u001a\u00020\u0017J\u000e\u0010\u001e\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0017J\u0006\u0010\u001f\u001a\u00020\u0015J\u000e\u0010 \u001a\u00020\u00152\u0006\u0010!\u001a\u00020\u0017J\u0006\u0010\"\u001a\u00020\u0015J\u0016\u0010#\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0019J\b\u0010$\u001a\u00020\u000fH\u0002J\u0016\u0010%\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010&\u001a\u00020\u0017J\u000e\u0010\'\u001a\u00020(2\u0006\u0010\u0016\u001a\u00020\u0017J\u000e\u0010)\u001a\u00020*2\u0006\u0010\u0016\u001a\u00020\u0017J\u0006\u0010+\u001a\u00020\u0015R\u0016\u0010\r\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u0010\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006,"}, d2 = {"Lcom/dasong/commerce/engine/GameEngine;", "", "deckManager", "Lcom/dasong/commerce/engine/DeckManager;", "settlementEngine", "Lcom/dasong/commerce/engine/SettlementEngine;", "eventExecutor", "Lcom/dasong/commerce/engine/EventExecutor;", "winChecker", "Lcom/dasong/commerce/engine/WinConditionChecker;", "turnManager", "Lcom/dasong/commerce/engine/TurnManager;", "(Lcom/dasong/commerce/engine/DeckManager;Lcom/dasong/commerce/engine/SettlementEngine;Lcom/dasong/commerce/engine/EventExecutor;Lcom/dasong/commerce/engine/WinConditionChecker;Lcom/dasong/commerce/engine/TurnManager;)V", "_gameState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/dasong/commerce/engine/GameState;", "gameState", "Lkotlinx/coroutines/flow/StateFlow;", "getGameState", "()Lkotlinx/coroutines/flow/StateFlow;", "buyMenuCard", "", "playerId", "", "card", "Lcom/dasong/commerce/model/card/MenuCard;", "buyShopCard", "shop", "Lcom/dasong/commerce/model/card/ShopCard;", "foundationIndex", "endBuyPhase", "endTurn", "initGame", "playerCount", "refreshGuestQueue", "removeMenu", "requireState", "selectGuest", "queuePosition", "settleMenuIncome", "Lcom/dasong/commerce/engine/MenuSettlementResult;", "settleShopIncome", "Lcom/dasong/commerce/engine/ShopSettlementResult;", "skipPreparePhase", "app_debug"})
public final class GameEngine {
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.DeckManager deckManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.SettlementEngine settlementEngine = null;
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.EventExecutor eventExecutor = null;
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.WinConditionChecker winChecker = null;
    @org.jetbrains.annotations.NotNull()
    private final com.dasong.commerce.engine.TurnManager turnManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.dasong.commerce.engine.GameState> _gameState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.engine.GameState> gameState = null;
    
    public GameEngine(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.DeckManager deckManager, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.SettlementEngine settlementEngine, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.EventExecutor eventExecutor, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.WinConditionChecker winChecker, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.TurnManager turnManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.dasong.commerce.engine.GameState> getGameState() {
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
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.MenuSettlementResult settleMenuIncome(int playerId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.ShopSettlementResult settleShopIncome(int playerId) {
        return null;
    }
    
    public final void refreshGuestQueue() {
    }
    
    public final void endTurn() {
    }
    
    private final com.dasong.commerce.engine.GameState requireState() {
        return null;
    }
    
    public GameEngine() {
        super();
    }
}