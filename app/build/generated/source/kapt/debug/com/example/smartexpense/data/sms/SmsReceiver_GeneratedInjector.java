package com.example.smartexpense.data.sms;

import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.hilt.internal.GeneratedEntryPoint;

@OriginatingElement(
    topLevelClass = SmsReceiver.class
)
@GeneratedEntryPoint
@InstallIn(SingletonComponent.class)
public interface SmsReceiver_GeneratedInjector {
  void injectSmsReceiver(SmsReceiver smsReceiver);
}
