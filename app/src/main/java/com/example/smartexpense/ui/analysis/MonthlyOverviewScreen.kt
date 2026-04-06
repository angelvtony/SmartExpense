package com.example.smartexpense.ui.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.*
import androidx.compose.runtime.LaunchedEffect
import com.example.smartexpense.ui.theme.GreenIncome
import com.example.smartexpense.ui.theme.RedExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyOverviewScreen(
    onBackClick: () -> Unit,
    viewModel: MonthlyOverviewViewModel = hiltViewModel()
) {
    val summaries by viewModel.monthlySummaries.collectAsState()
    val totalExpense by viewModel.totalLifetimeExpense.collectAsState()
    val totalIncome by viewModel.totalLifetimeIncome.collectAsState()
    val weeklySummaries by viewModel.weeklySummaries.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    LaunchedEffect(summaries) {
        if (selectedMonth == null && summaries.isNotEmpty()) {
            viewModel.selectMonth(summaries.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Overview") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Section: Graph
            AnimatedVisibility(
                visible = selectedMonth != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Weekly Insight: ${selectedMonth?.monthYear ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    WeeklySummaryChart(
                        weeklySummaries = weeklySummaries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            // Bottom Section: Monthly List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LifetimeSummaryCard(totalIncome, totalExpense)
                }

                item {
                    Text(
                        text = "Select Month to View Weekly Data",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (summaries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(summaries) { summary ->
                        MonthlySummaryItem(
                            summary = summary,
                            isSelected = selectedMonth?.monthYear == summary.monthYear,
                            onClick = { viewModel.selectMonth(summary) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryChart(
    weeklySummaries: List<WeeklySummary>,
    modifier: Modifier = Modifier
) {
    if (weeklySummaries.isEmpty()) return

    val maxAmount = weeklySummaries.maxOf { maxOf(it.totalExpense, it.totalIncome) }.coerceAtLeast(1.0)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        weeklySummaries.forEach { week ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(contentAlignment = Alignment.BottomCenter) {
                    Row(
                        modifier = Modifier.height(160.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Income Bar
                        val incomeHeight = (week.totalIncome / maxAmount).toFloat().coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(incomeHeight)
                                .background(GreenIncome, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                        // Expense Bar
                        val expenseHeight = (week.totalExpense / maxAmount).toFloat().coerceAtLeast(0.01f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(expenseHeight)
                                .background(RedExpense, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = week.weekRange.split(" ")[0], // Show "W1", "W2" etc
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LifetimeSummaryCard(totalIncome: Double, totalExpense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                "Lifetime Summary",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${String.format("%.2f", totalIncome)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = GreenIncome
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "₹${String.format("%.2f", totalExpense)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = RedExpense
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            val balance = totalIncome - totalExpense
            
            LinearProgressIndicator(
                progress = if (totalIncome + totalExpense > 0) (totalIncome / (totalIncome + totalExpense)).toFloat() else 0.5f,
                modifier = Modifier.fillMaxWidth().height(8.dp).background(RedExpense.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                color = GreenIncome,
                trackColor = RedExpense
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Net Savings: ₹${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (balance >= 0) GreenIncome else RedExpense,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun MonthlySummaryItem(
    summary: MonthlySummary,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    summary.monthYear,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(modifier = Modifier.padding(top = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp, 
                            contentDescription = null, 
                            tint = GreenIncome, 
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            "₹${String.format("%.0f", summary.totalIncome)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenIncome
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingDown, 
                            contentDescription = null, 
                            tint = RedExpense, 
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            "₹${String.format("%.0f", summary.totalExpense)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RedExpense
                        )
                    }
                }
            }
            
            val balance = summary.totalIncome - summary.totalExpense
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (balance >= 0) "+" else ""}₹${String.format("%.0f", balance)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (balance >= 0) GreenIncome else RedExpense
                )
                Text(
                    "Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
