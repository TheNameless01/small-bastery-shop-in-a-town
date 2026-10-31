package com.example.data.repository

import com.example.data.database.KhoubzaDatabase
import com.example.data.entities.ArrivalEntity
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.ExpenseEntity
import com.example.data.entities.InventoryEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SaleEntity
import com.example.data.entities.WasteEntity
import com.example.data.models.ArrivalWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BakeryRepository(private val database: KhoubzaDatabase) {

    private val productDao = database.productDao()
    private val arrivalDao = database.arrivalDao()
    private val saleDao = database.saleDao()
    private val wasteDao = database.wasteDao()
    private val inventoryDao = database.inventoryDao()
    private val expenseDao = database.expenseDao()
    private val auditLogDao = database.auditLogDao()

    companion object {
        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun parseDateToEpoch(dateString: String): Long {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                sdf.parse(dateString)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    // --- Product Operations ---
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProducts()

    suspend fun insertProduct(product: ProductEntity): Long = withContext(Dispatchers.IO) {
        val id = productDao.insertProduct(product)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "إضافة منتج",
                details = "تمت إضافة المنتج: ${product.nameArabic}"
            )
        )
        id
    }

    suspend fun updateProduct(product: ProductEntity) = withContext(Dispatchers.IO) {
        productDao.updateProduct(product)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "تعديل منتج",
                details = "تم تعديل المنتج: ${product.nameArabic}"
            )
        )
    }

    suspend fun setProductActive(id: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        productDao.setProductActive(id, isActive)
    }

    suspend fun deleteProduct(id: Long, nameArabic: String) = withContext(Dispatchers.IO) {
        productDao.deleteProductById(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "حذف منتج",
                details = "تم حذف المنتج: $nameArabic"
            )
        )
    }

    // --- Daily Arrival Operations ---
    fun getArrivalsForDate(dateString: String): Flow<List<ArrivalWithDetails>> {
        return arrivalDao.getArrivalsWithDetailsByDateString(dateString)
    }

    fun getAllRecordedDates(): Flow<List<String>> {
        return arrivalDao.getAllRecordedDates()
    }

    fun getArrivalsBetween(startDate: Long, endDate: Long): Flow<List<ArrivalWithDetails>> {
        return arrivalDao.getArrivalsWithDetailsBetween(startDate, endDate)
    }

    suspend fun addOrUpdateArrival(
        productId: Long,
        dateString: String,
        quantity: Int,
        purchasePrice: Long,
        sellingPrice: Long,
        notes: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val arrivalDate = parseDateToEpoch(dateString)
        val existing = arrivalDao.getArrivalByDateAndProduct(arrivalDate, productId)
        val product = productDao.getProductById(productId)
        val productName = product?.nameArabic ?: "منتج #$productId"

        val arrivalId = if (existing != null) {
            val updated = existing.copy(
                quantity = quantity,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                notes = notes
            )
            arrivalDao.updateArrival(updated)
            auditLogDao.insertLog(
                AuditLogEntity(
                    action = "تحديث سلعة واصلة",
                    details = "تحديث كمية $productName ليوم $dateString إلى $quantity بـ $purchasePrice دج"
                )
            )
            existing.id
        } else {
            val newArrival = ArrivalEntity(
                productId = productId,
                arrivalDate = arrivalDate,
                dateString = dateString,
                quantity = quantity,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                status = "CONFIRMED",
                notes = notes
            )
            val newId = arrivalDao.insertArrival(newArrival)
            auditLogDao.insertLog(
                AuditLogEntity(
                    action = "تسجيل سلعة واصلة",
                    details = "وصول $quantity من $productName ليوم $dateString بـ $purchasePrice دج"
                )
            )
            newId
        }
        arrivalId
    }

    suspend fun deleteArrival(arrivalId: Long, productName: String) = withContext(Dispatchers.IO) {
        arrivalDao.deleteArrivalById(arrivalId)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "حذف سلعة واصلة",
                details = "تم حذف وصول $productName"
            )
        )
    }

    // --- Fast Sales Counter Operations ---
    suspend fun updateSalesQuantity(
        arrivalId: Long,
        newTotalQuantity: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val arrival = arrivalDao.getArrivalById(arrivalId)
            ?: return@withContext Result.failure(Exception("لم يتم العثور على سجل السلعة الواصلة"))

        if (newTotalQuantity < 0) {
            return@withContext Result.failure(Exception("لا يمكن أن تكون الكمية المباعة سالبة"))
        }

        if (newTotalQuantity > arrival.quantity) {
            return@withContext Result.failure(Exception("الكمية المباعة ($newTotalQuantity) أكبر من الكمية المتوفرة في الوصول (${arrival.quantity})"))
        }

        val existingSales = saleDao.getSalesListForArrival(arrivalId)
        if (existingSales.isEmpty()) {
            if (newTotalQuantity > 0) {
                saleDao.insertSale(
                    SaleEntity(
                        arrivalId = arrivalId,
                        quantity = newTotalQuantity,
                        saleDate = arrival.arrivalDate
                    )
                )
            }
        } else {
            val primarySale = existingSales.first()
            if (newTotalQuantity > 0) {
                saleDao.updateSale(primarySale.copy(quantity = newTotalQuantity))
            } else {
                saleDao.deleteSaleById(primarySale.id)
            }
            // Delete any duplicate secondary records if present
            for (i in 1 until existingSales.size) {
                saleDao.deleteSaleById(existingSales[i].id)
            }
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "تحديث مبيعات",
                details = "تعديل مبيعات السجل #$arrivalId إلى $newTotalQuantity"
            )
        )
        Result.success(Unit)
    }

    // --- Waste (Gâchi) & Reconciliation Operations ---
    suspend fun recordWasteAndReconciliation(
        arrivalId: Long,
        spoiledQuantity: Int,
        usableLeftoverQuantity: Int,
        wasteReason: String = "تالف / غير صالح للبيع"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val arrivalWithDetails = arrivalDao.getArrivalWithDetailsById(arrivalId)
            ?: return@withContext Result.failure(Exception("سجل السلعة الواصلة غير موجود"))

        val totalReceived = arrivalWithDetails.arrival.quantity
        val sold = arrivalWithDetails.totalSold

        if (spoiledQuantity < 0 || usableLeftoverQuantity < 0) {
            return@withContext Result.failure(Exception("الكميات لا يمكن أن تكون سالبة"))
        }

        val allocated = sold + spoiledQuantity + usableLeftoverQuantity
        if (allocated != totalReceived) {
            val diff = totalReceived - allocated
            val msg = if (diff > 0) {
                "مجموع الكميات (مباع $sold + تالف $spoiledQuantity + متبقي $usableLeftoverQuantity = $allocated) أقل من الواصل ($totalReceived). يرجى تسوية المتبقي ($diff)"
            } else {
                "مجموع الكميات (مباع $sold + تالف $spoiledQuantity + متبقي $usableLeftoverQuantity = $allocated) يتجاوز الواصل ($totalReceived)"
            }
            return@withContext Result.failure(Exception(msg))
        }

        // Update Waste Entity
        val existingWastes = wasteDao.getWastesListForArrival(arrivalId)
        if (existingWastes.isEmpty()) {
            if (spoiledQuantity > 0) {
                wasteDao.insertWaste(
                    WasteEntity(
                        arrivalId = arrivalId,
                        quantity = spoiledQuantity,
                        reason = wasteReason
                    )
                )
            }
        } else {
            val primaryWaste = existingWastes.first()
            if (spoiledQuantity > 0) {
                wasteDao.updateWaste(primaryWaste.copy(quantity = spoiledQuantity, reason = wasteReason))
            } else {
                wasteDao.deleteWasteById(primaryWaste.id)
            }
            for (i in 1 until existingWastes.size) {
                wasteDao.deleteWasteById(existingWastes[i].id)
            }
        }

        // Update Carried Inventory Entity
        val existingInventories = inventoryDao.getInventoryForArrival(arrivalId)
        if (existingInventories.isEmpty()) {
            if (usableLeftoverQuantity > 0) {
                inventoryDao.insertInventory(
                    InventoryEntity(
                        productId = arrivalWithDetails.product.id,
                        sourceArrivalId = arrivalId,
                        originalQuantity = usableLeftoverQuantity,
                        remainingQuantity = usableLeftoverQuantity,
                        unitCost = arrivalWithDetails.arrival.purchasePrice,
                        status = "CARRIED_FORWARD"
                    )
                )
            }
        } else {
            val primaryInv = existingInventories.first()
            if (usableLeftoverQuantity > 0) {
                inventoryDao.updateInventory(
                    primaryInv.copy(
                        originalQuantity = usableLeftoverQuantity,
                        remainingQuantity = usableLeftoverQuantity,
                        unitCost = arrivalWithDetails.arrival.purchasePrice,
                        status = "CARRIED_FORWARD"
                    )
                )
            } else {
                inventoryDao.deleteInventoryByArrivalId(arrivalId)
            }
        }

        auditLogDao.insertLog(
            AuditLogEntity(
                action = "تسوية اليومية",
                details = "تسوية منتج ${arrivalWithDetails.product.nameArabic}: مباع $sold, تالف $spoiledQuantity, صالح للغد $usableLeftoverQuantity"
            )
        )
        Result.success(Unit)
    }

    // --- Carried Inventory ---
    val activeCarriedInventory: Flow<List<InventoryEntity>> = inventoryDao.getActiveCarriedInventory()

    // --- Expenses Operations ---
    fun getExpensesForDate(dateString: String): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesByDateString(dateString)
    }

    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    suspend fun getTotalExpensesBetween(startDate: Long, endDate: Long): Long = withContext(Dispatchers.IO) {
        expenseDao.getTotalExpensesBetween(startDate, endDate) ?: 0L
    }

    suspend fun insertExpense(expense: ExpenseEntity): Long = withContext(Dispatchers.IO) {
        val id = expenseDao.insertExpense(expense)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "إضافة مصروف",
                details = "مصروف: ${expense.category} - ${expense.description} (${expense.amount} دج)"
            )
        )
        id
    }

    suspend fun deleteExpense(id: Long, description: String, amount: Long) = withContext(Dispatchers.IO) {
        expenseDao.deleteExpenseById(id)
        auditLogDao.insertLog(
            AuditLogEntity(
                action = "حذف مصروف",
                details = "تم حذف المصروف: $description ($amount دج)"
            )
        )
    }

    // --- Audit Logs ---
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentAuditLogs()

    // --- Demo Data Initializer ---
    suspend fun seedDemoDataIfEmpty() = withContext(Dispatchers.IO) {
        val count = productDao.getProductCount()
        if (count == 0) {
            val defaultProducts = listOf(
                ProductEntity(
                    nameArabic = "خبز",
                    nameFrench = "Baguette",
                    category = "خبز",
                    unit = "قطعة",
                    defaultPurchasePrice = 20L,
                    defaultSellingPrice = 30L,
                    iconName = "baguette"
                ),
                ProductEntity(
                    nameArabic = "كرواسون",
                    nameFrench = "Croissant",
                    category = "حلويات",
                    unit = "قطعة",
                    defaultPurchasePrice = 35L,
                    defaultSellingPrice = 50L,
                    iconName = "croissant"
                ),
                ProductEntity(
                    nameArabic = "ميل فوي",
                    nameFrench = "Mille-feuille",
                    category = "حلويات",
                    unit = "قطعة",
                    defaultPurchasePrice = 80L,
                    defaultSellingPrice = 120L,
                    iconName = "millefeuille"
                ),
                ProductEntity(
                    nameArabic = "خبز بالشوكولا",
                    nameFrench = "Pain au chocolat",
                    category = "حلويات",
                    unit = "قطعة",
                    defaultPurchasePrice = 40L,
                    defaultSellingPrice = 60L,
                    iconName = "chocolate_bread"
                )
            )
            val insertedIds = productDao.insertProducts(defaultProducts)

            // Seed today's sample arrival as described in prompt
            val today = getTodayDateString()
            val todayEpoch = parseDateToEpoch(today)

            if (insertedIds.size >= 3) {
                // Baguette: 100 received, 20 purchase, 30 selling
                val baguetteArrivalId = arrivalDao.insertArrival(
                    ArrivalEntity(
                        productId = insertedIds[0],
                        arrivalDate = todayEpoch,
                        dateString = today,
                        quantity = 100,
                        purchasePrice = 20L,
                        sellingPrice = 30L
                    )
                )
                // 85 sold
                saleDao.insertSale(
                    SaleEntity(
                        arrivalId = baguetteArrivalId,
                        quantity = 85,
                        saleDate = todayEpoch
                    )
                )
                // 10 spoiled
                wasteDao.insertWaste(
                    WasteEntity(
                        arrivalId = baguetteArrivalId,
                        quantity = 10,
                        reason = "تالف / مكسور"
                    )
                )
                // 5 usable leftovers carried
                inventoryDao.insertInventory(
                    InventoryEntity(
                        productId = insertedIds[0],
                        sourceArrivalId = baguetteArrivalId,
                        originalQuantity = 5,
                        remainingQuantity = 5,
                        unitCost = 20L,
                        status = "CARRIED_FORWARD"
                    )
                )

                // Croissant: 40 received, 35 purchase, 50 selling, 30 sold, 5 spoiled, 5 usable
                val croissantArrivalId = arrivalDao.insertArrival(
                    ArrivalEntity(
                        productId = insertedIds[1],
                        arrivalDate = todayEpoch,
                        dateString = today,
                        quantity = 40,
                        purchasePrice = 35L,
                        sellingPrice = 50L
                    )
                )
                saleDao.insertSale(
                    SaleEntity(
                        arrivalId = croissantArrivalId,
                        quantity = 30,
                        saleDate = todayEpoch
                    )
                )
                wasteDao.insertWaste(
                    WasteEntity(
                        arrivalId = croissantArrivalId,
                        quantity = 5,
                        reason = "غير صالح للبيع"
                    )
                )
                inventoryDao.insertInventory(
                    InventoryEntity(
                        productId = insertedIds[1],
                        sourceArrivalId = croissantArrivalId,
                        originalQuantity = 5,
                        remainingQuantity = 5,
                        unitCost = 35L,
                        status = "CARRIED_FORWARD"
                    )
                )

                // Mille-feuille: 20 received, 80 purchase, 120 selling, 15 sold, 3 spoiled, 2 usable
                val mfArrivalId = arrivalDao.insertArrival(
                    ArrivalEntity(
                        productId = insertedIds[2],
                        arrivalDate = todayEpoch,
                        dateString = today,
                        quantity = 20,
                        purchasePrice = 80L,
                        sellingPrice = 120L
                    )
                )
                saleDao.insertSale(
                    SaleEntity(
                        arrivalId = mfArrivalId,
                        quantity = 15,
                        saleDate = todayEpoch
                    )
                )
                wasteDao.insertWaste(
                    WasteEntity(
                        arrivalId = mfArrivalId,
                        quantity = 3,
                        reason = "تالف"
                    )
                )
                inventoryDao.insertInventory(
                    InventoryEntity(
                        productId = insertedIds[2],
                        sourceArrivalId = mfArrivalId,
                        originalQuantity = 2,
                        remainingQuantity = 2,
                        unitCost = 80L,
                        status = "CARRIED_FORWARD"
                    )
                )

                // Sample expense: Transport 500 DZD
                expenseDao.insertExpense(
                    ExpenseEntity(
                        category = "نقل وتوصيل",
                        description = "نقل الدفعة الصباحية",
                        amount = 500L,
                        expenseDate = todayEpoch,
                        dateString = today
                    )
                )
            }
        }
    }

    suspend fun resetAllData() = withContext(Dispatchers.IO) {
        database.clearAllTables()
        seedDemoDataIfEmpty()
    }

    // --- JSON Backup Export ---
    suspend fun exportDataAsJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "Khoubza")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val products = productDao.getAllProducts().first()
        val productsArr = JSONArray()
        for (p in products) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("nameArabic", p.nameArabic)
            obj.put("nameFrench", p.nameFrench ?: "")
            obj.put("category", p.category)
            obj.put("unit", p.unit)
            obj.put("defaultPurchasePrice", p.defaultPurchasePrice)
            obj.put("defaultSellingPrice", p.defaultSellingPrice)
            productsArr.put(obj)
        }
        root.put("products", productsArr)

        val expenses = expenseDao.getAllExpenses().first()
        val expensesArr = JSONArray()
        for (e in expenses) {
            val obj = JSONObject()
            obj.put("category", e.category)
            obj.put("description", e.description)
            obj.put("amount", e.amount)
            obj.put("expenseDate", e.expenseDate)
            obj.put("dateString", e.dateString)
            expensesArr.put(obj)
        }
        root.put("expenses", expensesArr)

        root.toString(2)
    }

    suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("تاريخ,المنتج,الواصل,سعر الشراء,سعر البيع,المباع,التالف,المتبقي الصالح,المداخيل,تكلفة البضاعة,الربح\n")
        val dates = arrivalDao.getAllRecordedDates().first()
        for (date in dates) {
            val arrivals = arrivalDao.getArrivalsWithDetailsByDateString(date).first()
            for (item in arrivals) {
                val metric = com.example.domain.calculations.FinancialCalculator.calculateProductMetric(item)
                sb.append("${date},${metric.arabicName},${metric.receivedUnits},${metric.purchasePrice},${metric.sellingPrice},${metric.soldUnits},${metric.spoiledUnits},${metric.usableUnits},${metric.revenueDzd},${metric.costOfSoldDzd + metric.wasteLossDzd},${metric.operatingProfitDzd}\n")
            }
        }
        sb.toString()
    }
}
