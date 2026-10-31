package com.example.ui.screens.reports

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ArrivalWithDetails
import com.example.data.repository.BakeryRepository
import com.example.domain.calculations.DailyFinancialSummary
import com.example.domain.calculations.FinancialCalculator
import com.example.ui.components.BakeryTopBar
import com.example.ui.components.MetricCard
import com.example.ui.components.formatCurrencyDzd
import com.example.ui.theme.KhoubzaDangerRed
import com.example.ui.theme.KhoubzaForestGreen
import com.example.ui.theme.KhoubzaGoldDark
import com.example.ui.theme.KhoubzaSuccessGreen
import com.example.ui.theme.KhoubzaWarningAmber
import com.example.ui.theme.KhoubzaWheatGold
import com.example.ui.viewmodels.BakeryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportFilter(val label: String) {
    TODAY("اليوم"),
    YESTERDAY("الأمس"),
    THIS_WEEK("هذا الأسبوع"),
    THIS_MONTH("هذا الشهر"),
    PREVIOUS_MONTH("الشهر السابق")
}

@Composable
fun ReportsScreen(
    viewModel: BakeryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()

    var activeFilter by remember { mutableStateOf(ReportFilter.THIS_MONTH) }
    var reportArrivals by remember { mutableStateOf<List<ArrivalWithDetails>>(emptyList()) }
    var reportExpensesDzd by remember { mutableStateOf(0L) }

    // Date range calculation
    LaunchedEffect(activeFilter, selectedDate) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val (startDate, endDate) = when (activeFilter) {
            ReportFilter.TODAY -> {
                val todayStr = BakeryRepository.getTodayDateString()
                val todayEpoch = BakeryRepository.parseDateToEpoch(todayStr)
                todayEpoch to todayEpoch + 86400000L
            }
            ReportFilter.YESTERDAY -> {
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterdayStr = sdf.format(cal.time)
                val epoch = BakeryRepository.parseDateToEpoch(yesterdayStr)
                epoch to epoch + 86400000L
            }
            ReportFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = cal.timeInMillis
                start to System.currentTimeMillis() + 86400000L
            }
            ReportFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                start to System.currentTimeMillis() + 86400000L
            }
            ReportFilter.PREVIOUS_MONTH -> {
                cal.add(Calendar.MONTH, -1)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = cal.timeInMillis + 86400000L
                start to end
            }
        }

        launch {
            viewModel.repository.getArrivalsBetween(startDate, endDate).collect {
                reportArrivals = it
            }
        }
        launch {
            val totalExp = viewModel.repository.getTotalExpensesBetween(startDate, endDate)
            reportExpensesDzd = totalExp
        }
    }

    val summary = remember(reportArrivals, reportExpensesDzd, activeFilter) {
        FinancialCalculator.calculateDailySummary(
            dateString = activeFilter.label,
            arrivals = reportArrivals,
            expensesDzd = reportExpensesDzd
        )
    }

    // Best sellers
    val bestSellers = remember(reportArrivals) {
        reportArrivals.groupBy { it.product.id }
            .map { (_, items) ->
                val first = items.first()
                val totalSold = items.sumOf { it.totalSold }
                val totalRev = items.sumOf { it.actualRevenue }
                val totalCost = items.sumOf { it.costOfSold }
                val profit = totalRev - totalCost
                Triple(first.product.nameArabic, totalSold, totalRev)
            }
            .sortedByDescending { it.second }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BakeryTopBar(
                title = "التقارير المالية",
                selectedDate = selectedDate,
                onDateSelected = { viewModel.setSelectedDate(it) },
                isPinEnabled = isPinEnabled,
                onLockClicked = { viewModel.lockApp() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ReportFilter.values()) { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.label, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KhoubzaForestGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period Hero Net Profit Card
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (summary.netProfitDzd >= 0) KhoubzaForestGreen else KhoubzaDangerRed
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "صافي الربح للفترة (${activeFilter.label})",
                                style = MaterialTheme.typography.titleMedium.copy(color = Color.White.copy(alpha = 0.9f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = formatCurrencyDzd(summary.netProfitDzd),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KhoubzaWheatGold,
                                    fontSize = 32.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "مداخيل: ${formatCurrencyDzd(summary.totalRevenueDzd)} | تكلفة بضاعة: ${formatCurrencyDzd(summary.costOfGoodsSoldDzd)} | مصاريف: ${formatCurrencyDzd(summary.totalExpensesDzd)}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.85f))
                            )
                        }
                    }
                }

                // Financial Breakdown Metrics Grid
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "إجمالي المداخيل",
                                value = formatCurrencyDzd(summary.totalRevenueDzd),
                                subtitle = "${summary.totalSoldUnits} قطعة مباعة",
                                accentColor = KhoubzaSuccessGreen,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "تكلفة السلع الواصلة",
                                value = formatCurrencyDzd(summary.totalArrivalCostDzd),
                                subtitle = "${summary.totalReceivedUnits} قطعة واصلة",
                                accentColor = KhoubzaGoldDark,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricCard(
                                title = "خسائر الإتلاف (Gâchi)",
                                value = formatCurrencyDzd(summary.spoiledGoodsLossDzd),
                                subtitle = "${summary.totalSpoiledUnits} قطع (${"%.1f".format(summary.wastePercentage)}%)",
                                accentColor = KhoubzaDangerRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "المصاريف التشغيلية",
                                value = formatCurrencyDzd(summary.totalExpensesDzd),
                                subtitle = "كهرباء، نقل، تغليف...",
                                accentColor = KhoubzaWarningAmber,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Financial Canvas Bar Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "مقارنة المؤشرات المالية الرئيسية",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            FinancialBarChart(
                                revenue = summary.totalRevenueDzd,
                                cogs = summary.costOfGoodsSoldDzd,
                                waste = summary.spoiledGoodsLossDzd,
                                expenses = summary.totalExpensesDzd,
                                profit = summary.netProfitDzd.coerceAtLeast(0L),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        }
                    }
                }

                // Best Sellers Ranking
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "المنتجات الأكثر مبيعاً في الفترة",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            if (bestSellers.isEmpty()) {
                                Text(
                                    text = "لا توجد مبيعات مسجلة في هذه الفترة",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            } else {
                                bestSellers.forEachIndexed { index, (name, count, revenue) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = KhoubzaForestGreen.copy(alpha = 0.12f),
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "${index + 1}",
                                                        style = MaterialTheme.typography.labelMedium.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            color = KhoubzaForestGreen
                                                        )
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(text = name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                                Text(text = "$count قطعة مباعة", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                            }
                                        }

                                        Text(
                                            text = formatCurrencyDzd(revenue),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = KhoubzaSuccessGreen
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialBarChart(
    revenue: Long,
    cogs: Long,
    waste: Long,
    expenses: Long,
    profit: Long,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("المداخيل", revenue, KhoubzaSuccessGreen),
        Triple("تكلفة البضاعة", cogs, KhoubzaGoldDark),
        Triple("خسارة الإتلاف", waste, KhoubzaDangerRed),
        Triple("المصاريف", expenses, KhoubzaWarningAmber),
        Triple("صافي الربح", profit, KhoubzaForestGreen)
    )

    val maxVal = items.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L

    Canvas(modifier = modifier) {
        val barCount = items.size
        val availableWidth = size.width
        val barSpacing = 20.dp.toPx()
        val totalSpacing = barSpacing * (barCount + 1)
        val barWidth = ((availableWidth - totalSpacing) / barCount).coerceAtLeast(20.dp.toPx())
        val chartHeight = size.height - 30.dp.toPx()

        items.forEachIndexed { i, item ->
            val ratio = (item.second.toFloat() / maxVal.toFloat()).coerceIn(0.02f, 1f)
            val barHeight = chartHeight * ratio
            val x = barSpacing + i * (barWidth + barSpacing)
            val y = chartHeight - barHeight

            // Draw Bar
            drawRoundRect(
                color = item.third,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
        }
    }

    // Legend
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach { (name, _, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(name, fontSize = 10.sp, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
