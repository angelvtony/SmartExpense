package com.example.smartexpense.data.sms;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;

@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = SmsWorker.class
)
public interface SmsWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.example.smartexpense.data.sms.SmsWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(SmsWorker_AssistedFactory factory);
}
