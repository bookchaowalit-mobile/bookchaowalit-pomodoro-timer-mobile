package com.bookchaowalit.moneyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bookchaowalit.moneyflow.models.*
import com.bookchaowalit.moneyflow.viewmodel.MoneyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoneyFlowApp()
        }
    }
}

val DarkBg = Color(0xFF0A0E17)
val DarkCard = Color(0xFF111827)
val DarkInput = Color(0xFF1F2937)
val Green = Color(0xFF00C896)
val Red = Color(0xFFFF6B6B)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF6B7280)

@Composable
fun MoneyFlowApp(viewModel: MoneyViewModel = viewModel()) {
    val screen by viewModel.currentScreen.collectAsState()

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF111827)) {
                NavigationBarItem(screen == "home", { viewModel.navigateTo("home") }, Icons.Default.Home, "Home")
                NavigationBarItem(screen == "stats", { viewModel.navigateTo("stats") }, Icons.Default.PieChart, "Stats")
                NavigationBarItem(screen == "transactions", { viewModel.navigateTo("transactions") }, Icons.Default.List, "History")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (screen) {
                "home" -> HomeScreen(viewModel)
                "stats" -> StatsScreen(viewModel)
                "transactions" -> TransactionsScreen(viewModel)
            }
        }
    }
}

@Composable
fun NavigationBarItem(selected: Boolean, onClick: () -> Unit, icon: ImageVector, label: String) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 12.sp) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Green, selectedTextColor = Green,
            unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary,
            indicatorColor = Color.Transparent
        )
    )
}

@Composable
fun HomeScreen(viewModel: MoneyViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().verticalScroll(ScrollState()).padding(20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("MoneyFlow", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(24.dp))

        // Balance Card
        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Green, Color(0xFF00A878))),
                    RoundedCornerShape(20.dp)
                ).padding(24.dp)
            ) {
                Column {
                    Text("Total Balance", color = Color(0xB3FFFFFF), fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("$%.2f".format(viewModel.totalBalance), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowUpward, "Income", tint = Color(0xB3FFFFFF), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Income", color = Color(0xB3FFFFFF), fontSize = 12.sp)
                            }
                            Text("$%.2f".format(viewModel.totalIncome), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDownward, "Expense", tint = Color(0xB3FFFFFF), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Expenses", color = Color(0xB3FFFFFF), fontSize = 12.sp)
                            }
                            Text("$%.2f".format(viewModel.totalExpenses), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick Actions
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionBtn("Add Expense", Icons.Default.Remove, Red) { showAddDialog = true }
            QuickActionBtn("Add Income", Icons.Default.Add, Green) { showAddDialog = true }
        }

        Spacer(Modifier.height(24.dp))
        Text("Recent Transactions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(12.dp))

        val recent = viewModel.recentTransactions(5)
        if (recent.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, "Empty", tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No transactions yet", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            recent.forEach { tx -> TransactionCard(tx) { viewModel.deleteTransaction(tx.id) } }
        }

        Spacer(Modifier.height(100.dp))
    }

    if (showAddDialog) AddTransactionDialog(viewModel) { showAddDialog = false }
}

@Composable
fun QuickActionBtn(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        Modifier.weight(1f).clickable(onClick = onClick).height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(Modifier.fillMaxSize().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TransactionCard(tx: Transaction, onDelete: () -> Unit = {}) {
    val isIncome = tx.type == TransactionType.INCOME
    Card(
        Modifier.fillMaxWidth().padding(bottom = 8.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).clip(CircleShape).background(if (isIncome) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    "", tint = if (isIncome) Green else Red, modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(tx.category, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(tx.description.ifEmpty { tx.category }, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
            }
            Text(
                "${if (isIncome) "+" else "-"}$%.2f".format(tx.amount),
                color = if (isIncome) Green else Red,
                fontSize = 16.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatsScreen(viewModel: MoneyViewModel) {
    val catTotals = viewModel.expensesByCategory()

    Column(Modifier.fillMaxSize().verticalScroll(ScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("Spending Stats", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(20.dp))

        if (catTotals.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PieChart, "Empty", tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Add expenses to see stats", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            catTotals.forEach { ct ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(12.dp).clip(CircleShape).background(Color(ct.color)))
                            Spacer(Modifier.width(10.dp))
                            Text(ct.category, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text("$%.2f".format(ct.total), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        // Progress bar
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(DarkInput)) {
                            Box(Modifier.fillMaxWidth(ct.percentage / 100f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(ct.color)))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("%.1f%% of total".format(ct.percentage), color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun TransactionsScreen(viewModel: MoneyViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(ScrollState()).padding(20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("All Transactions", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text("${transactions.size} total", color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = DarkCard)) {
                Column(Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.List, "Empty", tint = TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("No transactions yet", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            transactions.forEach { tx -> TransactionCard(tx) { viewModel.deleteTransaction(tx.id) } }
        }
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun AddTransactionDialog(viewModel: MoneyViewModel, onDismiss: () -> Unit = {}) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Drink") }
    var isExpense by remember { mutableStateOf(true) }

    val type = if (isExpense) TransactionType.EXPENSE else TransactionType.INCOME
    val cats = viewModel.categories.filter { it.type == type }

    AlertDialog(
        containerColor = DarkCard,
        onDismissRequest = onDismiss,
        title = { Text(if (isExpense) "Add Expense" else "Add Income", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Green, unfocusedBorderColor = DarkInput,
                        focusedLabelColor = Green, unfocusedLabelColor = TextSecondary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Green, unfocusedBorderColor = DarkInput,
                        focusedLabelColor = Green, unfocusedLabelColor = TextSecondary,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(isExpense, { isExpense = true }, "Expense", Red)
                    FilterChip(!isExpense, { isExpense = false }, "Income", Green)
                }
                Spacer(Modifier.height(12.dp))
                Text("Category", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                cats.forEach { cat ->
                    FilterChip(selectedCategory == cat.name, { selectedCategory = cat.name }, cat.name, Color(cat.color))
                    Spacer(Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull() ?: return@TextButton
                viewModel.addTransaction(amt, selectedCategory, description, type)
                onDismiss()
            }) { Text("Save", color = Green, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun FilterChip(selected: Boolean, onClick: () -> Unit, label: String, color: Color) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 13.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            containerColor = DarkInput,
            labelColor = TextSecondary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = DarkInput, selectedBorderColor = color, enabled = true, selected = selected
        ),
        modifier = Modifier.height(32.dp)
    )
}
