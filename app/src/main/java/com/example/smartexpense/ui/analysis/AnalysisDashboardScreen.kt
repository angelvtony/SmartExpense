package com.example.smartexpense.ui.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartexpense.ui.theme.GreenIncome
import com.example.smartexpense.ui.theme.RedExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisDashboardScreen(
    onBackClick: () -> Unit,
    onSeeAllMonthsClick: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val currentExpense by viewModel.currentMonthExpense.collectAsState()
    val lastExpense by viewModel.lastMonthExpense.collectAsState()
    val topIncomes by viewModel.topIncomeCategories.collectAsState()
    val topExpenses by viewModel.topExpenseCategories.collectAsState()

    val chartColors = listOf(
        Color(0xFF5E35B1), Color(0xFF039BE5), Color(0xFF43A047), 
        Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFF8E24AA)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis Dashboard") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Comparison",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                MonthlyComparisonCard(currentExpense, lastExpense)
            }

            item {
                Text(
                    text = "Expenses Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (topExpenses.isEmpty()) {
                item {
                    Text("No expenses this month.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DonutChart(
                                data = topExpenses,
                                colors = chartColors,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ChartLegend(data = topExpenses, colors = chartColors)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Income Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (topIncomes.isEmpty()) {
                item {
                    Text("No income this month.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            DonutChart(
                                data = topIncomes,
                                colors = chartColors.reversed(),
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            ChartLegend(data = topIncomes, colors = chartColors.reversed())
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DonutChart(
    data: List<Pair<String, Double>>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second }.toFloat()
    if (total == 0f) return

    val maxCategory = data.maxByOrNull { it.second }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val thickness = size.width / 6f
            var currentAngle = -90f
            
            data.forEachIndexed { index, pair ->
                val sweepAngle = (pair.second.toFloat() / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = thickness, cap = StrokeCap.Butt),
                    size = Size(size.width - thickness, size.height - thickness),
                    topLeft = Offset(thickness / 2f, thickness / 2f)
                )
                currentAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Total", 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "₹${String.format("%.0f", total)}", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ChartLegend(data: List<Pair<String, Double>>, colors: List<Color>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEachIndexed { index, pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(colors[index % colors.size], CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pair.first,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "₹${String.format("%.2f", pair.second)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun MonthlyComparisonCard(currentExpense: Double, lastExpense: Double) {
    val diff = currentExpense - lastExpense
    val percentChange = if (lastExpense > 0) (diff / lastExpense) * 100 else if (currentExpense > 0) 100.0 else 0.0
    val isLower = diff < 0
    val diffText = String.format("%.2f", kotlin.math.abs(diff))
    val percentText = String.format("%.1f", kotlin.math.abs(percentChange))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Monthly Expense Comparison",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isLower) GreenIncome.copy(alpha = 0.2f) else RedExpense.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLower) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isLower) GreenIncome else RedExpense,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    if (currentExpense == 0.0 && lastExpense == 0.0) {
                        Text(
                            text = "No expenses recorded this or last month.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (diff == 0.0) {
                        Text(
                            text = "Expenses are exactly the same.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else if (isLower) {
                        Text(
                            text = "This month's expense is lower!",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenIncome
                        )
                        Text(
                            text = "You spent ₹$diffText ($percentText%) less than last month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Warning: Expenses increased.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = RedExpense
                        )
                        Text(
                            text = "You spent ₹$diffText ($percentText%) more than last month.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
