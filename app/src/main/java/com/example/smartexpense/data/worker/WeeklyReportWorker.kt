package com.example.smartexpense.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartexpense.domain.repository.TransactionRepository
import com.example.smartexpense.util.NotificationHelper
import com.example.smartexpense.data.local.entity.TransactionType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class WeeklyReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val currentEnd = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val currentStart = calendar.time

        val transactions = transactionRepository.getTransactionsInRange(currentStart, currentEnd).first()
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }
        
        val topCategory = expenses
            .groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"

        notificationHelper.showReportNotification(
            "Weekly Report 📊",
            "You spent ₹${String.format("%.2f", totalExpense)} last week. Top category: $topCategory"
        )

        return Result.success()
    }
}
