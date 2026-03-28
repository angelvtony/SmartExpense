package com.example.smartexpense.domain.usecase

import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.TransactionRepository
import com.example.smartexpense.ui.dashboard.WeeklyInsight
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetWeeklyInsightsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<WeeklyInsight> {
        return repository.getAllTransactions().map { transactions ->
            val calendar = Calendar.getInstance()
            val now = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = calendar.time

            val lastWeekTxs = transactions.filter { 
                it.date >= sevenDaysAgo && it.date <= now && it.type == TransactionType.EXPENSE 
            }
            
            val weeklyTotal = lastWeekTxs.sumOf { it.amount }
            val topCategory = lastWeekTxs.groupBy { it.category }
                .maxByOrNull { entry -> entry.value.sumOf { tx -> tx.amount } }?.key ?: "None"

            WeeklyInsight(weeklyTotal, topCategory)
        }
    }
}
