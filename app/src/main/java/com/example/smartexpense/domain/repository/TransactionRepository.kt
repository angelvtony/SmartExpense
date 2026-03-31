package com.example.smartexpense.domain.repository

import com.example.smartexpense.data.local.entity.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getTransactionsInRange(startDate: Date, endDate: Date): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun getTransactionById(id: Long): Flow<Transaction?>
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getTotalExpenseInRange(startDate: Date, endDate: Date): Flow<Double?>
    fun getTotalIncomeInRange(startDate: Date, endDate: Date): Flow<Double?>
}
