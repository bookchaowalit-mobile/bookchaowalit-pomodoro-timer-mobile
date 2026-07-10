package com.bookchaowalit.moneyflow.viewmodel

import androidx.lifecycle.ViewModel
import com.bookchaowalit.moneyflow.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class MoneyViewModel : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    val categories = listOf(
        Category("Food & Drink", "restaurant", 0xFFFF6B6B, TransactionType.EXPENSE),
        Category("Transport", "directions_car", 0xFF4ECDC4, TransactionType.EXPENSE),
        Category("Shopping", "shopping_bag", 0xFFFFE66D, TransactionType.EXPENSE),
        Category("Bills", "receipt_long", 0xFFA8E6CF, TransactionType.EXPENSE),
        Category("Entertainment", "movie", 0xFFFF8B94, TransactionType.EXPENSE),
        Category("Health", "favorite", 0xFFB8A9C9, TransactionType.EXPENSE),
        Category("Education", "school", 0xFF6C5CE7, TransactionType.EXPENSE),
        Category("Other", "more_horiz", 0xFF636E72, TransactionType.EXPENSE),
        Category("Salary", "work", 0xFF00C896, TransactionType.INCOME),
        Category("Freelance", "laptop", 0xFF00B4D8, TransactionType.INCOME),
        Category("Investment", "trending_up", 0xFFFF9F43, TransactionType.INCOME),
        Category("Other Income", "attach_money", 0xFF95A5A6, TransactionType.INCOME),
    )

    fun addTransaction(amount: Double, category: String, description: String, type: TransactionType) {
        val tx = Transaction(amount = amount, category = category, description = description, type = type)
        _transactions.value = listOf(tx) + _transactions.value
    }

    fun deleteTransaction(id: Long) {
        _transactions.value = _transactions.value.filter { it.id != id }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    val totalBalance: Double get() = _transactions.value.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
    val totalIncome: Double get() = _transactions.value.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpenses: Double get() = _transactions.value.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

    fun expensesByCategory(): List<CategoryTotal> {
        val expenses = _transactions.value.filter { it.type == TransactionType.EXPENSE }
        val total = expenses.sumOf { it.amount }
        if (total == 0.0) return emptyList()
        return expenses.groupBy { it.category }
            .map { (cat, txs) ->
                val catTotal = txs.sumOf { it.amount }
                val color = categories.find { it.name == cat }?.color ?: 0xFF636E72
                CategoryTotal(cat, catTotal, (catTotal / total) * 100, color)
            }
            .sortedByDescending { it.total }
    }

    fun recentTransactions(limit: Int = 10): List<Transaction> = _transactions.value.take(limit)
}
