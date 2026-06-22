package com.dasong.commerce.di;

import com.dasong.commerce.engine.SettlementEngine;
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
public final class AppModule_ProvideSettlementEngineFactory implements Factory<SettlementEngine> {
  @Override
  public SettlementEngine get() {
    return provideSettlementEngine();
  }

  public static AppModule_ProvideSettlementEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SettlementEngine provideSettlementEngine() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSettlementEngine());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideSettlementEngineFactory INSTANCE = new AppModule_ProvideSettlementEngineFactory();
  }
}
