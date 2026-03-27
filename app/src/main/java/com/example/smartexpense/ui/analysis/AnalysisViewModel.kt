package com.example.smartexpense.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val currentStartDate: Date
    private val currentEndDate: Date
    private val lastStartDate: Date
    private val lastEndDate: Date

    init {
        val calendar = Calendar.getInstance()
        

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        currentStartDate = calendar.time
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        currentEndDate = calendar.time

        calendar.time = currentStartDate
        calendar.add(Calendar.MONTH, -1)
        lastStartDate = calendar.time
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        lastEndDate = calendar.time
    }

    val currentMonthExpense = repository.getTotalExpenseInRange(currentStartDate, currentEndDate)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val lastMonthExpense = repository.getTotalExpenseInRange(lastStartDate, lastEndDate)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topIncomeCategories = repository.getTransactionsInRange(currentStartDate, currentEndDate)
        .map { list ->
            list.filter { it.type == TransactionType.INCOME }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .map { Pair(it.key, it.value) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topExpenseCategories = repository.getTransactionsInRange(currentStartDate, currentEndDate)
        .map { list ->
            list.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .entries
                .sortedByDescending { it.value }
                .map { Pair(it.key, it.value) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
