package com.example.ui.screens.arrivals

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.ProductEntity
import com.example.data.models.ArrivalWithDetails
import com.example.ui.components.BakeryTopBar
import com.example.ui.components.formatCurrencyDzd
import com.example.ui.theme.KhoubzaDangerRed
import com.example.ui.theme.KhoubzaForestGreen
import com.example.ui.theme.KhoubzaGoldDark
import com.example.ui.theme.KhoubzaSuccessGreen
import com.example.ui.theme.KhoubzaWheatGold
import com.example.ui.viewmodels.BakeryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalsScreen(
    viewModel: BakeryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val arrivals by viewModel.arrivalsForSelectedDate.collectAsState()
    val activeProducts by viewModel.activeProducts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var arrivalToEdit by remember { mutableStateOf<ArrivalWithDetails?>(null) }
    var arrivalToDelete by remember { mutableStateOf<ArrivalWithDetails?>(null) }

    val totalPurchaseCost = arrivals.sumOf { it.arrivalPurchaseCost }
    val totalPotentialRevenue = arrivals.sumOf { it.potentialRevenue }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BakeryTopBar(
                title = "السلعة الواصلة",
                selectedDate = selectedDate,
                onDateSelected = { viewModel.setSelectedDate(it) },
                isPinEnabled = isPinEnabled,
                onLockClicked = { viewModel.lockApp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    arrivalToEdit = null
                    showAddDialog = true
                },
                containerColor = KhoubzaForestGreen,
                contentColor = KhoubzaWheatGold,
                modifier = Modifier.testTag("add_arrival_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة سلعة واصلة")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Summary Banner
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
                            text = "إجمالي تكلفة الشراء",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrencyDzd(totalPurchaseCost),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaGoldDark
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "المداخيل المتوقعة",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = formatCurrencyDzd(totalPotentialRevenue),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = KhoubzaSuccessGreen
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
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = KhoubzaForestGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد بضاعة واصلة مسجلة لتاريخ $selectedDate",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                arrivalToEdit = null
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KhoubzaForestGreen),
                            modifier = Modifier.testTag("empty_add_arrival_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تسجيل أول سلعة واصلة")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(arrivals, key = { it.arrival.id }) { item ->
                        ArrivalItemCard(
                            item = item,
                            onEdit = {
                                arrivalToEdit = item
                                showAddDialog = true
                            },
                            onDelete = {
                                arrivalToDelete = item
                            }
                        )
                    }
                }
            }
        }
    }

    // Add or Edit Arrival Dialog
    if (showAddDialog) {
        ArrivalFormDialog(
            activeProducts = activeProducts,
            existingItem = arrivalToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { productId, quantity, purchasePrice, sellingPrice, notes ->
                viewModel.addOrUpdateArrival(
                    productId = productId,
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    notes = notes
                )
                showAddDialog = false
            },
            onQuickCreateProduct = { nameArabic, nameFrench, purchase, selling ->
                viewModel.saveProduct(
                    nameArabic = nameArabic,
                    nameFrench = nameFrench,
                    category = "خبز",
                    unit = "قطعة",
                    purchasePrice = purchase,
                    sellingPrice = selling
                )
            }
        )
    }

    // Delete Confirmation Dialog
    arrivalToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { arrivalToDelete = null },
            title = { Text("حذف السلعة الواصلة") },
            text = {
                Text("هل أنت متأكد من حذف وصول ${item.product.nameArabic}؟ سيتم حذف جميع المبيعات والبقايا المرتبطة بهذا الوصول.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteArrival(item.arrival.id, item.product.nameArabic)
                        arrivalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KhoubzaDangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { arrivalToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ArrivalItemCard(
    item: ArrivalWithDetails,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("arrival_card_${item.arrival.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
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
                    if (!item.product.nameFrench.isNullOrBlank()) {
                        Text(
                            text = item.product.nameFrench,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = KhoubzaForestGreen)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = KhoubzaDangerRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("الكمية الواصلة", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${item.arrival.quantity} ${item.product.unit}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column {
                        Text("سعر الشراء", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${item.arrival.purchasePrice} دج",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Column {
                        Text("سعر البيع", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${item.arrival.sellingPrice} دج",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "تكلفة الشراء: ${formatCurrencyDzd(item.arrivalPurchaseCost)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = KhoubzaGoldDark, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "المبيعات المتوقعة: ${formatCurrencyDzd(item.potentialRevenue)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = KhoubzaSuccessGreen, fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrivalFormDialog(
    activeProducts: List<ProductEntity>,
    existingItem: ArrivalWithDetails?,
    onDismiss: () -> Unit,
    onSave: (productId: Long, quantity: Int, purchasePrice: Long, sellingPrice: Long, notes: String?) -> Unit,
    onQuickCreateProduct: (nameArabic: String, nameFrench: String?, purchase: Long, selling: Long) -> Unit
) {
    var selectedProductId by remember {
        mutableStateOf(existingItem?.product?.id ?: activeProducts.firstOrNull()?.id ?: 0L)
    }
    var quantityText by remember {
        mutableStateOf(existingItem?.arrival?.quantity?.toString() ?: "")
    }
    var purchasePriceText by remember {
        mutableStateOf(
            existingItem?.arrival?.purchasePrice?.toString()
                ?: activeProducts.find { it.id == selectedProductId }?.defaultPurchasePrice?.toString()
                ?: "0"
        )
    }
    var sellingPriceText by remember {
        mutableStateOf(
            existingItem?.arrival?.sellingPrice?.toString()
                ?: activeProducts.find { it.id == selectedProductId }?.defaultSellingPrice?.toString()
                ?: "0"
        )
    }
    var notesText by remember { mutableStateOf(existingItem?.arrival?.notes ?: "") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var showQuickProductCreate by remember { mutableStateOf(false) }
    var newProductNameArabic by remember { mutableStateOf("") }
    var newProductNameFrench by remember { mutableStateOf("") }

    val qty = quantityText.toIntOrNull() ?: 0
    val buy = purchasePriceText.toLongOrNull() ?: 0L
    val sell = sellingPriceText.toLongOrNull() ?: 0L
    val totalCost = qty * buy
    val totalPotential = qty * sell

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (existingItem != null) "تعديل السلعة الواصلة" else "تسجيل سلعة واصلة جديدة")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showQuickProductCreate) {
                    Text("إضافة منتج جديد سريعاً:", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = newProductNameArabic,
                        onValueChange = { newProductNameArabic = it },
                        label = { Text("اسم المنتج بالعربية *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newProductNameFrench,
                        onValueChange = { newProductNameFrench = it },
                        label = { Text("الاسم بالفرنسية (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { showQuickProductCreate = false }) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (newProductNameArabic.isNotBlank()) {
                                    onQuickCreateProduct(newProductNameArabic, newProductNameFrench, buy, sell)
                                    showQuickProductCreate = false
                                }
                            }
                        ) {
                            Text("حفظ المنتج")
                        }
                    }
                } else {
                    // Product Selector Dropdown
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        val currentProduct = activeProducts.find { it.id == selectedProductId }
                        OutlinedTextField(
                            value = currentProduct?.let { "${it.nameArabic} (${it.nameFrench ?: ""})" } ?: "اختر منتجاً",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المنتج") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            activeProducts.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.nameArabic} ${if (product.nameFrench != null) "(${product.nameFrench})" else ""}") },
                                    onClick = {
                                        selectedProductId = product.id
                                        if (existingItem == null) {
                                            purchasePriceText = product.defaultPurchasePrice.toString()
                                            sellingPriceText = product.defaultSellingPrice.toString()
                                        }
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = { showQuickProductCreate = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ إضافة منتج جديد للقائمة", fontSize = 12.sp)
                    }

                    // Quantity input
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("الكمية الواصلة (قطع) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("arrival_quantity_input")
                    )

                    // Purchase Price input
                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سعر الشراء للوحدة (دج) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("arrival_purchase_price_input")
                    )

                    // Selling Price input
                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سعر البيع للوحدة (دج) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("arrival_selling_price_input")
                    )

                    // Computed preview
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "إجمالي تكلفة الشراء: ${formatCurrencyDzd(totalCost)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = KhoubzaGoldDark)
                            )
                            Text(
                                text = "إجمالي المبيعات المتوقعة: ${formatCurrencyDzd(totalPotential)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = KhoubzaSuccessGreen)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedProductId > 0 && qty > 0) {
                        onSave(selectedProductId, qty, buy, sell, notesText.ifBlank { null })
                    }
                },
                enabled = selectedProductId > 0 && qty > 0,
                modifier = Modifier.testTag("save_arrival_button")
            ) {
                Text("حفظ السلعة الواصلة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
