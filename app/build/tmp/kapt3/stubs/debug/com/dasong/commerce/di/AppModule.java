package com.dasong.commerce.di;

@dagger.Module()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0007J\b\u0010\u0005\u001a\u00020\u0006H\u0007J0\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010H\u0007J\b\u0010\u0011\u001a\u00020\u000bH\u0007J\b\u0010\u0012\u001a\u00020\u0010H\u0007J\b\u0010\u0013\u001a\u00020\u000eH\u0007\u00a8\u0006\u0014"}, d2 = {"Lcom/dasong/commerce/di/AppModule;", "", "()V", "provideDeckManager", "Lcom/dasong/commerce/engine/DeckManager;", "provideEventExecutor", "Lcom/dasong/commerce/engine/EventExecutor;", "provideGameEngine", "Lcom/dasong/commerce/engine/GameEngine;", "deckManager", "settlementEngine", "Lcom/dasong/commerce/engine/SettlementEngine;", "eventExecutor", "winChecker", "Lcom/dasong/commerce/engine/WinConditionChecker;", "turnManager", "Lcom/dasong/commerce/engine/TurnManager;", "provideSettlementEngine", "provideTurnManager", "provideWinConditionChecker", "app_debug"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class AppModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.dasong.commerce.di.AppModule INSTANCE = null;
    
    private AppModule() {
        super();
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.DeckManager provideDeckManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.SettlementEngine provideSettlementEngine() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.EventExecutor provideEventExecutor() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.WinConditionChecker provideWinConditionChecker() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.TurnManager provideTurnManager() {
        return null;
    }
    
    @dagger.Provides()
    @javax.inject.Singleton()
    @org.jetbrains.annotations.NotNull()
    public final com.dasong.commerce.engine.GameEngine provideGameEngine(@org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.DeckManager deckManager, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.SettlementEngine settlementEngine, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.EventExecutor eventExecutor, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.WinConditionChecker winChecker, @org.jetbrains.annotations.NotNull()
    com.dasong.commerce.engine.TurnManager turnManager) {
        return null;
    }
}