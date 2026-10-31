package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun DashboardScreen(
    viewModel: BakeryViewModel,
    onNavigateToArrivals: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToWaste: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()
    val arrivals by viewModel.arrivalsForSelectedDate.collectAsState()
    val expenses by viewModel.expensesForSelectedDate.collectAsState()

    val totalUnreconciled = arrivals.sumOf { it.unreconciledRemaining }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        BakeryTopBar(
            title = "لوحة التحكم",
            selectedDate = selectedDate,
            onDateSelected = { viewModel.setSelectedDate(it) },
            isPinEnabled = isPinEnabled,
            onLockClicked = { viewModel.lockApp() }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Profit Hero Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("net_profit_card"),
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صافي الربح اليومي",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (summary.netProfitDzd >= 0) "ربح إيجابي" else "عجز",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = formatCurrencyDzd(summary.netProfitDzd),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaWheatGold,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "الربح التشغيلي: ${formatCurrencyDzd(summary.operatingProfitDzd)} | المصاريف: ${formatCurrencyDzd(summary.totalExpensesDzd)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        )
                    }
                }
            }

            // Quick Operational Metrics Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "المداخيل (المبيعات)",
                            value = formatCurrencyDzd(summary.totalRevenueDzd),
                            subtitle = "${summary.totalSoldUnits} قطعة مباعة",
                            icon = Icons.Default.PointOfSale,
                            accentColor = KhoubzaSuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "تكلفة السلعة الواصلة",
                            value = formatCurrencyDzd(summary.totalArrivalCostDzd),
                            subtitle = "${summary.totalReceivedUnits} قطعة واصلة",
                            icon = Icons.Default.LocalShipping,
                            accentColor = KhoubzaGoldDark,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "خسارة البقايا (Gâchi)",
                            value = formatCurrencyDzd(summary.spoiledGoodsLossDzd),
                            subtitle = "${summary.totalSpoiledUnits} قطعة تالفة (${"%.1f".format(summary.wastePercentage)}%)",
                            icon = Icons.Default.DeleteOutline,
                            accentColor = KhoubzaDangerRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "المصاريف الإضافية",
                            value = formatCurrencyDzd(summary.totalExpensesDzd),
                            subtitle = "${expenses.size} عمليات صرف",
                            icon = Icons.Default.AccountBalanceWallet,
                            accentColor = KhoubzaWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Reconciliation Status Alert
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (totalUnreconciled > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (totalUnreconciled > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (totalUnreconciled > 0) KhoubzaDangerRed else KhoubzaSuccessGreen,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (totalUnreconciled > 0) "تنبيه تسوية البقايا" else "حالة اليومية ممتازة",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (totalUnreconciled > 0) {
                                        "يوجد $totalUnreconciled قطعة متبقية لم تسوّ بين الصالح والتالف"
                                    } else {
                                        "تمت تسوية جميع الكميات الواصلة والمباعة والتالفة"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (totalUnreconciled > 0) {
                            Button(
                                onClick = onNavigateToWaste,
                                colors = ButtonDefaults.buttonColors(containerColor = KhoubzaDangerRed),
                                modifier = Modifier.testTag("reconcile_now_button")
                            ) {
                                Text("تسوية الآن", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Quick Operations Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onNavigateToSales,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("nav_sales_quick_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = KhoubzaForestGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسجيل المبيعات", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onNavigateToArrivals,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("nav_arrivals_quick_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("السلعة الواصلة", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Today's Products Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تفاصيل منتجات اليوم (${arrivals.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "المتبقي الصالح: ${summary.totalUsableLeftoverUnits}",
                        style = MaterialTheme.typography.labelSmall.copy(color = KhoubzaWheatGold)
                    )
                }
            }

            if (arrivals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "لم يتم تسجيل أي سلعة واصلة لتاريخ $selectedDate",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = onNavigateToArrivals) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة سلعة واصلة الآن")
                            }
                        }
                    }
                }
            } else {
                items(arrivals) { item ->
                    val metric = FinancialCalculator.calculateProductMetric(item)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.product.nameArabic,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (!item.product.nameFrench.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${item.product.nameFrench})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "وصل: ${item.arrival.quantity} | بِيع: ${item.totalSold} | تالف: ${item.totalSpoiled} | باقٍ: ${item.remainingUnsold}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrencyDzd(metric.revenueDzd),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = KhoubzaSuccessGreen
                                    )
                                )
                                Text(
                                    text = "ربح: ${formatCurrencyDzd(metric.operatingProfitDzd)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (metric.operatingProfitDzd >= 0) KhoubzaForestGreen else KhoubzaDangerRed,
                                        fontWeight = FontWeight.SemiBold
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
