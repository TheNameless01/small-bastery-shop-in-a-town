package com.example.ui.screens.products

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.Switch
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
import com.example.ui.components.BakeryTopBar
import com.example.ui.theme.KhoubzaDangerRed
import com.example.ui.theme.KhoubzaForestGreen
import com.example.ui.theme.KhoubzaGoldDark
import com.example.ui.theme.KhoubzaSuccessGreen
import com.example.ui.theme.KhoubzaWheatGold
import com.example.ui.viewmodels.BakeryViewModel

val PRODUCT_CATEGORIES = listOf("خبز", "حلويات", "فطائر", "معجنات", "أخرى")
val PRODUCT_UNITS = listOf("قطعة", "وحدة", "حبة", "كغ", "رغيف")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: BakeryViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isPinEnabled by viewModel.isPinProtectionEnabled.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    var showProductDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            BakeryTopBar(
                title = "كتالوج المنتجات",
                selectedDate = selectedDate,
                onDateSelected = { viewModel.setSelectedDate(it) },
                isPinEnabled = isPinEnabled,
                onLockClicked = { viewModel.lockApp() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showProductDialog = true
                },
                containerColor = KhoubzaForestGreen,
                contentColor = KhoubzaWheatGold,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منتج جديد")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Note Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = KhoubzaForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الأسعار الافتراضية هنا هي اقتراحات فقط؛ تعديلها لا يغير إطلاقاً أسعار السلع الواصلة التاريخية السابقة.",
                        style = MaterialTheme.typography.bodySmall.copy(color = KhoubzaForestGreen)
                    )
                }
            }

            if (allProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BakeryDining,
                            contentDescription = null,
                            tint = KhoubzaForestGreen.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد منتجات مسجلة في الكتالوج",
                            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                productToEdit = null
                                showProductDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KhoubzaForestGreen)
                        ) {
                            Text("إضافة أول منتج")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = product.nameArabic,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (!product.nameFrench.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${product.nameFrench})",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = product.category,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text(
                                            text = "الشراء الافتراضي: ${product.defaultPurchasePrice} دج",
                                            style = MaterialTheme.typography.bodySmall.copy(color = KhoubzaGoldDark)
                                        )
                                        Text(
                                            text = "البيع الافتراضي: ${product.defaultSellingPrice} دج",
                                            style = MaterialTheme.typography.bodySmall.copy(color = KhoubzaSuccessGreen)
                                        )
                                        Text(
                                            text = "الوحدة: ${product.unit}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            productToEdit = product
                                            showProductDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = KhoubzaForestGreen)
                                    }
                                    IconButton(
                                        onClick = { productToDelete = product }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = KhoubzaDangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Product Add/Edit Dialog
    if (showProductDialog) {
        var nameArabic by remember { mutableStateOf(productToEdit?.nameArabic ?: "") }
        var nameFrench by remember { mutableStateOf(productToEdit?.nameFrench ?: "") }
        var category by remember { mutableStateOf(productToEdit?.category ?: PRODUCT_CATEGORIES[0]) }
        var unit by remember { mutableStateOf(productToEdit?.unit ?: PRODUCT_UNITS[0]) }
        var purchasePriceText by remember { mutableStateOf(productToEdit?.defaultPurchasePrice?.toString() ?: "") }
        var sellingPriceText by remember { mutableStateOf(productToEdit?.defaultSellingPrice?.toString() ?: "") }

        var categoryExpanded by remember { mutableStateOf(false) }
        var unitExpanded by remember { mutableStateOf(false) }

        val buy = purchasePriceText.toLongOrNull() ?: 0L
        val sell = sellingPriceText.toLongOrNull() ?: 0L

        AlertDialog(
            onDismissRequest = { showProductDialog = false },
            title = { Text(if (productToEdit != null) "تعديل المنتج" else "إضافة منتج جديد") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameArabic,
                        onValueChange = { nameArabic = it },
                        label = { Text("اسم المنتج بالعربية *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nameFrench,
                        onValueChange = { nameFrench = it },
                        label = { Text("الاسم بالفرنسية (مثال: Baguette)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = !categoryExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الفئة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                PRODUCT_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = cat
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("الوحدة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                PRODUCT_UNITS.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            unit = u
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = purchasePriceText,
                        onValueChange = { purchasePriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سعر الشراء الافتراضي (دج)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = sellingPriceText,
                        onValueChange = { sellingPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("سعر البيع الافتراضي (دج)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameArabic.isNotBlank()) {
                            viewModel.saveProduct(
                                id = productToEdit?.id ?: 0L,
                                nameArabic = nameArabic,
                                nameFrench = nameFrench,
                                category = category,
                                unit = unit,
                                purchasePrice = buy,
                                sellingPrice = sell
                            )
                            showProductDialog = false
                        }
                    },
                    enabled = nameArabic.isNotBlank()
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProductDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Delete Product Dialog
    productToDelete?.let { product ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("حذف المنتج") },
            text = { Text("هل أنت متأكد من حذف المنتج '${product.nameArabic}'؟") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KhoubzaDangerRed)
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
