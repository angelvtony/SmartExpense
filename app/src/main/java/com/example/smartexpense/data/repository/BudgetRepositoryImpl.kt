package com.example.smartexpense.data.repository

import com.example.smartexpense.data.local.dao.BudgetDao
import com.example.smartexpense.data.local.dao.TransactionDao
import com.example.smartexpense.data.local.entity.BudgetEntity
import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getBudgetsForMonth(month: String): Flow<List<BudgetEntity>> =
        budgetDao.getBudgetsForMonth(month)

    override suspend fun calculateAndSetupMonthlyBudgets(month: String) {

        val existingBudgets = budgetDao.getBudgetsForMonth(month).first()
        if (existingBudgets.isNotEmpty()) return

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -3)
        val startDate = calendar.time
        val endDate = Date()

        val transactions = transactionDao.getTransactionsInRange(startDate, endDate).first()
        val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }

        val categorySpending = expenseTransactions.groupBy { it.category }
            .mapValues { (_, txs) ->
                val total = txs.sumOf { it.amount }
                (total / 3.0) * 1.15
            }

        categorySpending.forEach { (category, limit) ->
            budgetDao.insertBudget(
                BudgetEntity(
                    category = category,
                    monthlyLimit = limit,
                    currentSpent = 0.0,
                    month = month
                )
            )
        }
    }

    override suspend fun updateBudgetSpending(category: String, amount: Double, month: String) {
        val existingBudget = budgetDao.getBudgetForCategory(month, category)
        if (existingBudget != null) {
            val updatedBudget = existingBudget.copy(
                currentSpent = existingBudget.currentSpent + amount
            )
            budgetDao.updateBudget(updatedBudget)
        }
    }
}
