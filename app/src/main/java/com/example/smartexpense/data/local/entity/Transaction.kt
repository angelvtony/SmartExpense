package com.example.smartexpense.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    EXPENSE, INCOME
}

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["date"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Date,
    val paymentMethod: String,
    val note: String = "",
    val type: TransactionType
)
