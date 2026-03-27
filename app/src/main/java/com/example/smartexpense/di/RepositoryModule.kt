package com.example.smartexpense.di

import com.example.smartexpense.data.repository.TransactionRepositoryImpl
import com.example.smartexpense.domain.repository.TransactionRepository
import com.example.smartexpense.data.repository.BudgetRepositoryImpl
import com.example.smartexpense.domain.repository.BudgetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        budgetRepositoryImpl: BudgetRepositoryImpl
    ): BudgetRepository
}
