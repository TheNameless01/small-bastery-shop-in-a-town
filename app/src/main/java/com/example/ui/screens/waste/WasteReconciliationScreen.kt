package com.example.ui.screens.waste

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun WasteReconciliationScreen(
    viewModel: BakeryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val arrivals by viewModel.arrivalsForSelectedDate.collectAsState()
    val summary by viewModel.dailySummary.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BakeryTopBar(
                title = "البقايا والإتلاف (Gâchi)",
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
            // Summary Header Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "خسارة الإتلاف (Gâchi)",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrencyDzd(summary.spoiledGoodsLossDzd),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaDangerRed
                            )
                        )
                        Text(
                            text = "${summary.totalSpoiledUnits} قطع تالفة (${"%.1f".format(summary.wastePercentage)}%)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "قيمة البقايا الصالحة (أصول)",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrencyDzd(summary.remainingInventoryValueDzd),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaWheatGold
                            )
                        )
                        Text(
                            text = "${summary.totalUsableLeftoverUnits} قطع صالحة للغد",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text(
                        text = "لا توجد سلع واصلة لتسوية بقاياها في هذا اليوم",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(arrivals, key = { it.arrival.id }) { item ->
                        ProductReconciliationCard(
                            item = item,
                            onSaveReconciliation = { spoiled, usable, reason ->
                                viewModel.recordReconciliation(item.arrival.id, spoiled, usable, reason)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductReconciliationCard(
    item: ArrivalWithDetails,
    onSaveReconciliation: (spoiled: Int, usable: Int, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val received = item.arrival.quantity
    val sold = item.totalSold
    val initialUnsold = (received - sold).coerceAtLeast(0)

    // Current state from DB
    val dbSpoiled = item.totalSpoiled
    val dbUsable = (initialUnsold - dbSpoiled).coerceAtLeast(0)

    var spoiledQty by remember(item) { mutableIntStateOf(dbSpoiled) }
    var usableQty by remember(item) { mutableIntStateOf(dbUsable) }
    var reason by remember(item) { mutableStateOf("تالف / غير صالح للبيع") }

    val totalAllocated = sold + spoiledQty + usableQty
    val isBalanced = totalAllocated == received
    val remainingToAllocate = received - totalAllocated

    val spoiledLossDzd = spoiledQty.toLong() * item.arrival.purchasePrice
    val usableAssetDzd = usableQty.toLong() * item.arrival.purchasePrice

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("reconciliation_card_${item.arrival.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.product.nameArabic,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "الواصل: $received | المباع: $sold | غير المباع: $initialUnsold",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isBalanced) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isBalanced) KhoubzaSuccessGreen else KhoubzaDangerRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isBalanced) "مسوّى بالكامل" else "باقٍ: $remainingToAllocate",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isBalanced) KhoubzaSuccessGreen else KhoubzaDangerRed,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two Sections: Usable Leftovers (صالح للغد) vs Spoiled (تالف/Gâchi)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Usable Leftovers Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Inventory,
                                contentDescription = null,
                                tint = KhoubzaWheatGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صالح للغد", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledIconButton(
                                onClick = { if (usableQty > 0) usableQty-- },
                                enabled = usableQty > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "$usableQty",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KhoubzaWheatGold
                                )
                            )
                            FilledIconButton(
                                onClick = {
                                    if (totalAllocated < received) {
                                        usableQty++
                                    }
                                },
                                enabled = totalAllocated < received,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "أصول: ${formatCurrencyDzd(usableAssetDzd)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = KhoubzaWheatGold)
                        )
                    }
                }

                // Spoiled Goods Box (Gâchi)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = KhoubzaDangerRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تالف (Gâchi)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FilledIconButton(
                                onClick = { if (spoiledQty > 0) spoiledQty-- },
                                enabled = spoiledQty > 0,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = "$spoiledQty",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = KhoubzaDangerRed
                                )
                            )
                            FilledIconButton(
                                onClick = {
                                    if (totalAllocated < received) {
                                        spoiledQty++
                                    }
                                },
                                enabled = totalAllocated < received,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "خسارة: ${formatCurrencyDzd(spoiledLossDzd)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = KhoubzaDangerRed)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Quick buttons (All to Usable, All to Spoiled) and Save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            usableQty = initialUnsold
                            spoiledQty = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("الكل صالح", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            spoiledQty = initialUnsold
                            usableQty = 0
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("الكل تالف", fontSize = 11.sp, color = KhoubzaDangerRed)
                    }
                }

                Button(
                    onClick = {
                        onSaveReconciliation(spoiledQty, usableQty, reason)
                    },
                    enabled = isBalanced,
                    colors = ButtonDefaults.buttonColors(containerColor = KhoubzaForestGreen),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("save_reconciliation_${item.arrival.id}")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حفظ التسوية", fontSize = 12.sp)
                }
            }
        }
    }
}
