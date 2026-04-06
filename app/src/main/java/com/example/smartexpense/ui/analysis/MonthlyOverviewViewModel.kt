package com.example.smartexpense.ui.analysis

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
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class WeeklySummary(
    val weekNumber: Int,
    val weekRange: String, // e.g. "1-7"
    val totalExpense: Double,
    val totalIncome: Double
)

data class MonthlySummary(
    val monthYear: String,
    val totalExpense: Double,
    val totalIncome: Double,
    val date: Date
)

@HiltViewModel
class MonthlyOverviewViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow<MonthlySummary?>(null)
    val selectedMonth = _selectedMonth.asStateFlow()

    val monthlySummaries: StateFlow<List<MonthlySummary>> = repository.getAllTransactions()
        .map { transactions ->
            transactions.groupBy { 
                val cal = Calendar.getInstance()
                cal.time = it.date
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }.map { (key, monthTransactions) ->
                val firstTransactionDate = monthTransactions.first().date
                val cal = Calendar.getInstance()
                cal.time = firstTransactionDate
                cal.set(Calendar.DAY_OF_MONTH, 1)
                
                val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
                
                val totalExpense = monthTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                
                val totalIncome = monthTransactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                
                MonthlySummary(
                    monthYear = monthName,
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    date = cal.time
                )
            }.sortedByDescending { it.date }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalLifetimeExpense: StateFlow<Double> = repository.getAllTransactions()
        .map { transactions ->
            transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalLifetimeIncome: StateFlow<Double> = repository.getAllTransactions()
        .map { transactions ->
            transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val weeklySummaries: StateFlow<List<WeeklySummary>> = combine(
        repository.getAllTransactions(),
        _selectedMonth
    ) { transactions, selected ->
        if (selected == null) return@combine emptyList()
        
        val calendar = Calendar.getInstance()
        calendar.time = selected.date
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        
        val monthTransactions = transactions.filter {
            val cal = Calendar.getInstance()
            cal.time = it.date
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }
        
        // Group by weeks
        (1..4).map { weekIndex ->
            val weekTransactions = monthTransactions.filter {
                val cal = Calendar.getInstance()
                cal.time = it.date
                val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
                when (weekIndex) {
                    1 -> dayOfMonth in 1..7
                    2 -> dayOfMonth in 8..14
                    3 -> dayOfMonth in 15..21
                    4 -> dayOfMonth >= 22
                    else -> false
                }
            }
            
            val weekRange = when (weekIndex) {
                1 -> "W1 (1-7)"
                2 -> "W2 (8-14)"
                3 -> "W3 (15-21)"
                4 -> "W4 (22+)"
                else -> ""
            }
            
            WeeklySummary(
                weekNumber = weekIndex,
                weekRange = weekRange,
                totalExpense = weekTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                totalIncome = weekTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectMonth(summary: MonthlySummary) {
        _selectedMonth.value = summary
    }
}
