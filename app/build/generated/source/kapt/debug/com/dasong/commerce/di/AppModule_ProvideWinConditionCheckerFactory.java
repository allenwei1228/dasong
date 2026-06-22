package com.dasong.commerce.di;

import com.dasong.commerce.engine.WinConditionChecker;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AppModule_ProvideWinConditionCheckerFactory implements Factory<WinConditionChecker> {
  @Override
  public WinConditionChecker get() {
    return provideWinConditionChecker();
  }

  public static AppModule_ProvideWinConditionCheckerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WinConditionChecker provideWinConditionChecker() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideWinConditionChecker());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideWinConditionCheckerFactory INSTANCE = new AppModule_ProvideWinConditionCheckerFactory();
  }
}
