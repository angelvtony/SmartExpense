package com.example.smartexpense.data.worker;

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
    topLevelClass = WeeklyReportWorker.class
)
public interface WeeklyReportWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.example.smartexpense.data.worker.WeeklyReportWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(
      WeeklyReportWorker_AssistedFactory factory);
}
