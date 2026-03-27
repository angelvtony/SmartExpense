package com.example.smartexpense.domain.repository

import com.example.smartexpense.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgetsForMonth(month: String): Flow<List<BudgetEntity>>
    suspend fun calculateAndSetupMonthlyBudgets(month: String)
    suspend fun updateBudgetSpending(category: String, amount: Double, month: String)
}
