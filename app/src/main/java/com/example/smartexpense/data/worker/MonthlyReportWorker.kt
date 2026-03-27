package com.example.smartexpense.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartexpense.domain.repository.TransactionRepository
import com.example.smartexpense.domain.repository.BudgetRepository
import com.example.smartexpense.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class MonthlyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        

        val currentMonthKey = dateFormat.format(calendar.time)
        

        calendar.add(Calendar.MONTH, -1)
        val previousMonthKey = dateFormat.format(calendar.time)

        budgetRepository.calculateAndSetupMonthlyBudgets(currentMonthKey)

        notificationHelper.showReportNotification(
            "Monthly Insight 💡",
            "A new month has started! Your budgets for $currentMonthKey are ready."
        )

        return Result.success()
    }
}
