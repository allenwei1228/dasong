package com.dasong.commerce.di;

import com.dasong.commerce.engine.EventExecutor;
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
public final class AppModule_ProvideEventExecutorFactory implements Factory<EventExecutor> {
  @Override
  public EventExecutor get() {
    return provideEventExecutor();
  }

  public static AppModule_ProvideEventExecutorFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EventExecutor provideEventExecutor() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideEventExecutor());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideEventExecutorFactory INSTANCE = new AppModule_ProvideEventExecutorFactory();
  }
}
