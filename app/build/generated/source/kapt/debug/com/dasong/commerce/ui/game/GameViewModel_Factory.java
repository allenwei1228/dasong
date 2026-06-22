package com.dasong.commerce.ui.game;

import com.dasong.commerce.engine.GameEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class GameViewModel_Factory implements Factory<GameViewModel> {
  private final Provider<GameEngine> gameEngineProvider;

  public GameViewModel_Factory(Provider<GameEngine> gameEngineProvider) {
    this.gameEngineProvider = gameEngineProvider;
  }

  @Override
  public GameViewModel get() {
    return newInstance(gameEngineProvider.get());
  }

  public static GameViewModel_Factory create(Provider<GameEngine> gameEngineProvider) {
    return new GameViewModel_Factory(gameEngineProvider);
  }

  public static GameViewModel newInstance(GameEngine gameEngine) {
    return new GameViewModel(gameEngine);
  }
}
