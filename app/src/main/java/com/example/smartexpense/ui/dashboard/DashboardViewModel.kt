package com.example.smartexpense.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpense.data.local.entity.Transaction
import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.net.Uri
import com.example.smartexpense.util.ReportExporter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import javax.inject.Inject
import com.example.smartexpense.domain.repository.BudgetRepository
import com.example.smartexpense.data.local.entity.BudgetEntity
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import android.app.Application
import com.example.smartexpense.data.worker.WeeklyReportWorker
import com.example.smartexpense.data.worker.MonthlyReportWorker
import java.util.concurrent.TimeUnit

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val application: Application
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val currentMonth = dateFormat.format(Date())

    val budgets: StateFlow<List<BudgetEntity>> = budgetRepository.getBudgetsForMonth(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<Transaction>> = repository.getRecentTransactions(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val calendar = Calendar.getInstance()
    private val startDate: Date
    private val endDate: Date

    init {
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        startDate = calendar.time
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        endDate = calendar.time

        viewModelScope.launch {
            budgetRepository.calculateAndSetupMonthlyBudgets(currentMonth)
        }

        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val workManager = WorkManager.getInstance(application)

        val weeklyRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "WeeklyReportWork",
            ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest
        )

        val monthlyRequest = PeriodicWorkRequestBuilder<MonthlyReportWorker>(30, TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "MonthlyReportWork",
            ExistingPeriodicWorkPolicy.KEEP,
            monthlyRequest
        )
    }

    val totalIncome = repository.getTotalIncomeInRange(startDate, endDate)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense = repository.getTotalExpenseInRange(startDate, endDate)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weeklyInsights = repository.getAllTransactions().map { transitions ->
        val calendar = Calendar.getInstance()
        val now = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val sevenDaysAgo = calendar.time

        val lastWeekTxs = transitions.filter { it.date >= sevenDaysAgo && it.date <= now && it.type == TransactionType.EXPENSE }
        val weeklyTotal = lastWeekTxs.sumOf { it.amount }
        val topCategory = lastWeekTxs.groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }?.key ?: "None"

        WeeklyInsight(weeklyTotal, topCategory)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyInsight(0.0, "None"))

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun downloadExcelReport(context: android.content.Context) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions().first()
            val uri = ReportExporter.generateExcelReport(context, transactions)
            uri?.let { ReportExporter.shareFile(context, it) }
        }
    }
}

data class WeeklyInsight(val total: Double, val topCategory: String)
