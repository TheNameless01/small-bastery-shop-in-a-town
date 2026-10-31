package com.example.ui.screens.sales

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.ArrivalWithDetails
import com.example.ui.components.BakeryTopBar
import com.example.ui.components.formatCurrencyDzd
import com.example.ui.theme.KhoubzaDangerRed
import com.example.ui.theme.KhoubzaForestGreen
import com.example.ui.theme.KhoubzaSuccessGreen
import com.example.ui.theme.KhoubzaWheatGold
import com.example.ui.viewmodels.BakeryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: BakeryViewModel,
    onNavigateToArrivals: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val arrivals by viewModel.arrivalsForSelectedDate.collectAsState()

    val totalSoldUnits = arrivals.sumOf { it.totalSold }
    val totalRevenue = arrivals.sumOf { it.actualRevenue }

    var itemForDirectEdit by remember { mutableStateOf<ArrivalWithDetails?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BakeryTopBar(
                title = "المبيعات السريعة",
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
            // Live Sales Totals Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي مبيعات اليوم",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrencyDzd(totalRevenue),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaSuccessGreen
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = KhoubzaForestGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "$totalSoldUnits قطعة مباعة",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = KhoubzaForestGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (arrivals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = KhoubzaForestGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد سلعة واصلة لتسجيل مبيعاتها في هذا اليوم",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToArrivals,
                            colors = ButtonDefaults.buttonColors(containerColor = KhoubzaForestGreen)
                        ) {
                            Text("الذهاب لتسجيل السلعة الواصلة أولاً")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(arrivals, key = { it.arrival.id }) { item ->
                        SalesProductCard(
                            item = item,
                            onIncrement = { step -> viewModel.incrementSale(item, step) },
                            onDecrement = { step -> viewModel.decrementSale(item, step) },
                            onDirectEdit = { itemForDirectEdit = item }
                        )
                    }
                }
            }
        }
    }

    // Direct Quantity Edit Dialog
    itemForDirectEdit?.let { item ->
        var directQtyText by remember { mutableStateOf(item.totalSold.toString()) }
        AlertDialog(
            onDismissRequest = { itemForDirectEdit = null },
            title = { Text("تعديل كمية مباعة: ${item.product.nameArabic}") },
            text = {
                Column {
                    Text(
                        text = "الكمية المتوفرة في الوصول: ${item.arrival.quantity} قطعة",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = directQtyText,
                        onValueChange = { directQtyText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("الكمية المباعة مباشرة") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val entered = directQtyText.toIntOrNull() ?: 0
                        viewModel.setSaleQuantity(item.arrival.id, entered)
                        itemForDirectEdit = null
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemForDirectEdit = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SalesProductCard(
    item: ArrivalWithDetails,
    onIncrement: (step: Int) -> Unit,
    onDecrement: (step: Int) -> Unit,
    onDirectEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val received = item.arrival.quantity
    val sold = item.totalSold
    val remaining = (received - sold).coerceAtLeast(0)
    val progress = if (received > 0) sold.toFloat() / received.toFloat() else 0f
    val isSoldOut = remaining == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sales_card_${item.arrival.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name, Unit Price, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.product.nameArabic,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "سعر البيع: ${item.arrival.sellingPrice} دج",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = KhoubzaWheatGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSoldOut) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isSoldOut) "نفدت الكمية" else "المتبقي: $remaining",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSoldOut) KhoubzaDangerRed else KhoubzaForestGreen,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stock progress indicator
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isSoldOut) KhoubzaDangerRed else KhoubzaForestGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Fast Counter Row: [-] [Sold Count] [+]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button (Touch target 54dp)
                FilledIconButton(
                    onClick = { onDecrement(1) },
                    enabled = sold > 0,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("decrement_sale_${item.arrival.id}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = KhoubzaForestGreen.copy(alpha = 0.15f),
                        contentColor = KhoubzaForestGreen
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "إنقاص 1",
                        modifier = Modifier.size(26.dp)
                    )
                }

                // Middle: Big Sold Counter & Direct Edit Click
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onDirectEdit() }
                ) {
                    Text(
                        text = "$sold",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = KhoubzaForestGreen,
                            fontSize = 38.sp
                        ),
                        modifier = Modifier.testTag("sold_count_${item.arrival.id}")
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "من أصل $received واصلة",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "تعديل مباشر",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Increment Button (Touch target 54dp)
                FilledIconButton(
                    onClick = { onIncrement(1) },
                    enabled = remaining > 0,
                    modifier = Modifier
                        .size(54.dp)
                        .testTag("increment_sale_${item.arrival.id}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = KhoubzaForestGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "زيادة 1",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Increment Chips (+5, +10) and Live Revenue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onIncrement(5) },
                        enabled = remaining >= 5,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+5", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { onIncrement(10) },
                        enabled = remaining >= 10,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("+10", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Text(
                    text = "المداخيل: ${formatCurrencyDzd(item.actualRevenue)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KhoubzaSuccessGreen
                    )
                )
            }
        }
    }
}
