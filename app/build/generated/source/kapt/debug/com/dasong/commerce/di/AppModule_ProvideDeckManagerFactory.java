package com.dasong.commerce.di;

import com.dasong.commerce.engine.DeckManager;
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
public final class AppModule_ProvideDeckManagerFactory implements Factory<DeckManager> {
  @Override
  public DeckManager get() {
    return provideDeckManager();
  }

  public static AppModule_ProvideDeckManagerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DeckManager provideDeckManager() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideDeckManager());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideDeckManagerFactory INSTANCE = new AppModule_ProvideDeckManagerFactory();
  }
}
