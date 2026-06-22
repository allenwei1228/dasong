package com.dasong.commerce.ui.result;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class ResultViewModel_Factory implements Factory<ResultViewModel> {
  @Override
  public ResultViewModel get() {
    return newInstance();
  }

  public static ResultViewModel_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ResultViewModel newInstance() {
    return new ResultViewModel();
  }

  private static final class InstanceHolder {
    private static final ResultViewModel_Factory INSTANCE = new ResultViewModel_Factory();
  }
}
