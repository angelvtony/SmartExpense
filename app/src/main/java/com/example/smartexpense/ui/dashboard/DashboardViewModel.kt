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
import com.example.smartexpense.domain.usecase.GetWeeklyInsightsUseCase
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val getWeeklyInsightsUseCase: GetWeeklyInsightsUseCase,
    private val application: Application
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val currentMonth = dateFormat.format(Date())

    private val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val startCalendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endCalendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val startDate = startCalendar.time
    val endDate = endCalendar.time

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getRecentTransactions(10),
        budgetRepository.getBudgetsForMonth(currentMonth),
        repository.getTotalIncomeInRange(startDate, endDate),
        repository.getTotalExpenseInRange(startDate, endDate),
        getWeeklyInsightsUseCase()
    ) { recentTransactions, budgets, totalIncome, totalExpense, weeklyInsight ->
        DashboardUiState(
            recentTransactions = recentTransactions,
            budgets = budgets,
            totalIncome = totalIncome ?: 0.0,
            totalExpense = totalExpense ?: 0.0,
            weeklyInsight = weeklyInsight,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            budgetRepository.calculateAndSetupMonthlyBudgets(currentMonth)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun downloadExcelReport(context: android.content.Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val transactions = repository.getAllTransactions().first()
            val uri = ReportExporter.generateExcelReport(context, transactions)
            uri?.let { 
                withContext(Dispatchers.Main) {
                    ReportExporter.shareFile(context, it)
                }
            }
        }
    }
}

data class DashboardUiState(
    val recentTransactions: List<Transaction> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val weeklyInsight: WeeklyInsight = WeeklyInsight(0.0, "None"),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class WeeklyInsight(val total: Double, val topCategory: String)
