package com.example.smartexpense.data.repository

import com.example.smartexpense.data.local.dao.TransactionDao
import com.example.smartexpense.data.local.entity.Transaction
import com.example.smartexpense.data.local.entity.TransactionType
import com.example.smartexpense.domain.repository.TransactionRepository
import com.example.smartexpense.domain.repository.BudgetRepository
import com.example.smartexpense.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val budgetRepository: BudgetRepository,
    private val notificationHelper: NotificationHelper
) : TransactionRepository {
    override fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()

    override fun getTransactionsInRange(startDate: Date, endDate: Date): Flow<List<Transaction>> =
        dao.getTransactionsInRange(startDate, endDate)

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        dao.getRecentTransactions(limit)

    override fun getTransactionById(id: Long): Flow<Transaction?> =
        dao.getTransactionById(id)

    override suspend fun insertTransaction(transaction: Transaction) {
        dao.insertTransaction(transaction)

        if (transaction.type == TransactionType.EXPENSE) {
            val monthKey = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(transaction.date)
            budgetRepository.updateBudgetSpending(transaction.category, transaction.amount, monthKey)

            val budgets = budgetRepository.getBudgetsForMonth(monthKey).first()
            val categoryBudget = budgets.find { it.category == transaction.category }
            
            categoryBudget?.let {
                val percentage = ((it.currentSpent / it.monthlyLimit) * 100).toInt()
                if (percentage >= 100) {
                    notificationHelper.showBudgetAlert(it.category, percentage, true)
                } else if (percentage >= 80) {
                    notificationHelper.showBudgetAlert(it.category, percentage, false)
                }
            }
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)

    override suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
    
    override fun getTotalExpenseInRange(startDate: Date, endDate: Date): Flow<Double?> =
        dao.getTotalExpenseInRange(startDate, endDate)

    override fun getTotalIncomeInRange(startDate: Date, endDate: Date): Flow<Double?> =
        dao.getTotalIncomeInRange(startDate, endDate)
}
