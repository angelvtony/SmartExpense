package com.example.smartexpense;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = SmartExpenseApp.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface SmartExpenseApp_GeneratedInjector {
  void injectSmartExpenseApp(SmartExpenseApp smartExpenseApp);
}
