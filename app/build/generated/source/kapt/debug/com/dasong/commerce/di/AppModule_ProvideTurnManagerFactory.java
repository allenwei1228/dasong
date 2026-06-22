package com.dasong.commerce.di;

import com.dasong.commerce.engine.TurnManager;
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
public final class AppModule_ProvideTurnManagerFactory implements Factory<TurnManager> {
  @Override
  public TurnManager get() {
    return provideTurnManager();
  }

  public static AppModule_ProvideTurnManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TurnManager provideTurnManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTurnManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideTurnManagerFactory INSTANCE = new AppModule_ProvideTurnManagerFactory();
  }
}
