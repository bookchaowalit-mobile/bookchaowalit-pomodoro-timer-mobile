package com.bookchaowalit.moneyflow.models

import java.time.LocalDate
import java.time.LocalDateTime

data class Transaction(
    val id: Long = System.currentTimeMillis(),
    val amount: Double,
    val category: String,
    val description: String,
    val type: TransactionType,
    val date: LocalDateTime = LocalDateTime.now(),
    val isRecurring: Boolean = false,
)

enum class TransactionType { INCOME, EXPENSE }

data class Category(
    val name: String,
    val icon: String,
    val color: Long,
    val type: TransactionType,
)

data class MonthlySummary(
    val month: String,
    val income: Double,
    val expenses: Double,
) {
    val balance: Double get() = income - expenses
}

data class CategoryTotal(
    val category: String,
    val total: Double,
    val percentage: Double,
    val color: Long,
)
