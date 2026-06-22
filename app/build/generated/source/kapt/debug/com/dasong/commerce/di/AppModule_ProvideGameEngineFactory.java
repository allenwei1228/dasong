package com.dasong.commerce.di;

import com.dasong.commerce.engine.DeckManager;
import com.dasong.commerce.engine.EventExecutor;
import com.dasong.commerce.engine.GameEngine;
import com.dasong.commerce.engine.SettlementEngine;
import com.dasong.commerce.engine.TurnManager;
import com.dasong.commerce.engine.WinConditionChecker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class AppModule_ProvideGameEngineFactory implements Factory<GameEngine> {
  private final Provider<DeckManager> deckManagerProvider;

  private final Provider<SettlementEngine> settlementEngineProvider;

  private final Provider<EventExecutor> eventExecutorProvider;

  private final Provider<WinConditionChecker> winCheckerProvider;

  private final Provider<TurnManager> turnManagerProvider;

  public AppModule_ProvideGameEngineFactory(Provider<DeckManager> deckManagerProvider,
      Provider<SettlementEngine> settlementEngineProvider,
      Provider<EventExecutor> eventExecutorProvider,
      Provider<WinConditionChecker> winCheckerProvider, Provider<TurnManager> turnManagerProvider) {
    this.deckManagerProvider = deckManagerProvider;
    this.settlementEngineProvider = settlementEngineProvider;
    this.eventExecutorProvider = eventExecutorProvider;
    this.winCheckerProvider = winCheckerProvider;
    this.turnManagerProvider = turnManagerProvider;
  }

  @Override
  public GameEngine get() {
    return provideGameEngine(deckManagerProvider.get(), settlementEngineProvider.get(), eventExecutorProvider.get(), winCheckerProvider.get(), turnManagerProvider.get());
  }

  public static AppModule_ProvideGameEngineFactory create(Provider<DeckManager> deckManagerProvider,
      Provider<SettlementEngine> settlementEngineProvider,
      Provider<EventExecutor> eventExecutorProvider,
      Provider<WinConditionChecker> winCheckerProvider, Provider<TurnManager> turnManagerProvider) {
    return new AppModule_ProvideGameEngineFactory(deckManagerProvider, settlementEngineProvider, eventExecutorProvider, winCheckerProvider, turnManagerProvider);
  }

  public static GameEngine provideGameEngine(DeckManager deckManager,
      SettlementEngine settlementEngine, EventExecutor eventExecutor,
      WinConditionChecker winChecker, TurnManager turnManager) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideGameEngine(deckManager, settlementEngine, eventExecutor, winChecker, turnManager));
  }
}
