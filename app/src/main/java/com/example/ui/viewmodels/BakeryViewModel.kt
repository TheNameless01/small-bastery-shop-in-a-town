package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.KhoubzaDatabase
import com.example.data.entities.ExpenseEntity
import com.example.data.entities.ProductEntity
import com.example.data.models.ArrivalWithDetails
import com.example.data.repository.BakeryRepository
import com.example.domain.calculations.DailyFinancialSummary
import com.example.domain.calculations.FinancialCalculator
import com.example.security.PinManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class UiMessage {
    data class Success(val message: String) : UiMessage()
    data class Error(val message: String) : UiMessage()
}

class BakeryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = KhoubzaDatabase.getDatabase(application)
    val repository = BakeryRepository(database)
    val pinManager = PinManager(application)

    // Current selected date for operational screens (Arrivals, Sales, Reconciliation, Dashboard)
    private val _selectedDate = MutableStateFlow(BakeryRepository.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // PIN lock state
    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    val isPinProtectionEnabled: StateFlow<Boolean> = pinManager.isPinEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Message events
    private val _uiMessages = MutableSharedFlow<UiMessage>()
    val uiMessages: SharedFlow<UiMessage> = _uiMessages.asSharedFlow()

    // Products
    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProducts: StateFlow<List<ProductEntity>> = repository.activeProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current date arrivals with details
    private val _arrivalsForSelectedDate = MutableStateFlow<List<ArrivalWithDetails>>(emptyList())
    val arrivalsForSelectedDate: StateFlow<List<ArrivalWithDetails>> = _arrivalsForSelectedDate.asStateFlow()

    // Current date expenses
    private val _expensesForSelectedDate = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expensesForSelectedDate: StateFlow<List<ExpenseEntity>> = _expensesForSelectedDate.asStateFlow()

    // Daily summary derived reactively
    val dailySummary: StateFlow<DailyFinancialSummary> = combine(
        arrivalsForSelectedDate,
        expensesForSelectedDate,
        selectedDate
    ) { arrivals, expenses, date ->
        val totalExp = expenses.sumOf { it.amount }
        FinancialCalculator.calculateDailySummary(
            dateString = date,
            arrivals = arrivals,
            expensesDzd = totalExp
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailyFinancialSummary(
            dateString = BakeryRepository.getTodayDateString(),
            totalReceivedUnits = 0,
            totalSoldUnits = 0,
            totalSpoiledUnits = 0,
            totalUsableLeftoverUnits = 0,
            totalRevenueDzd = 0,
            totalArrivalCostDzd = 0,
            costOfSoldProductsDzd = 0,
            spoiledGoodsLossDzd = 0,
            remainingInventoryValueDzd = 0,
            costOfGoodsSoldDzd = 0,
            operatingProfitDzd = 0,
            totalExpensesDzd = 0,
            netProfitDzd = 0,
            wastePercentage = 0.0
        )
    )

    // Recorded dates for selector
    val recordedDates: StateFlow<List<String>> = repository.getAllRecordedDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed demo data on first launch
            repository.seedDemoDataIfEmpty()
            checkPinStatus()
        }

        // Observe arrivals and expenses for selected date
        viewModelScope.launch {
            selectedDate.collect { date ->
                launch {
                    repository.getArrivalsForDate(date).collect {
                        _arrivalsForSelectedDate.value = it
                    }
                }
                launch {
                    repository.getExpensesForDate(date).collect {
                        _expensesForSelectedDate.value = it
                    }
                }
            }
        }
    }

    private suspend fun checkPinStatus() {
        if (!pinManager.hasPinConfigured()) {
            _isUnlocked.value = true
        }
    }

    fun setSelectedDate(dateString: String) {
        _selectedDate.value = dateString
    }

    // --- PIN Actions ---
    fun unlockWithPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = pinManager.verifyPin(pin)
            if (valid) {
                _isUnlocked.value = true
            }
            onResult(valid)
        }
    }

    fun lockApp() {
        viewModelScope.launch {
            if (pinManager.hasPinConfigured()) {
                _isUnlocked.value = false
            }
        }
    }

    fun setupNewPin(newPin: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = pinManager.setPin(newPin)
            if (ok) {
                _isUnlocked.value = true
                _uiMessages.emit(UiMessage.Success("تم تفعيل رمز الحماية بنجاح"))
            } else {
                _uiMessages.emit(UiMessage.Error("رمز الحماية يجب أن يتكون من 4 أرقام على الأقل"))
            }
            onComplete(ok)
        }
    }

    fun disablePin(currentPin: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = pinManager.disablePin(currentPin)
            if (ok) {
                _isUnlocked.value = true
                _uiMessages.emit(UiMessage.Success("تم إلغاء قفل الحماية"))
            } else {
                _uiMessages.emit(UiMessage.Error("رمز الحماية غير صحيح"))
            }
            onComplete(ok)
        }
    }

    // --- Daily Arrival Actions ---
    fun addOrUpdateArrival(
        productId: Long,
        quantity: Int,
        purchasePrice: Long,
        sellingPrice: Long,
        notes: String? = null
    ) {
        viewModelScope.launch {
            try {
                if (quantity <= 0) {
                    _uiMessages.emit(UiMessage.Error("يرجى إدخال كمية صحيحة أكبر من الصفر"))
                    return@launch
                }
                if (purchasePrice < 0 || sellingPrice < 0) {
                    _uiMessages.emit(UiMessage.Error("الأسعار لا يمكن أن تكون سالبة"))
                    return@launch
                }
                repository.addOrUpdateArrival(
                    productId = productId,
                    dateString = _selectedDate.value,
                    quantity = quantity,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    notes = notes
                )
                _uiMessages.emit(UiMessage.Success("تم تسجيل السلعة الواصلة بنجاح"))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("خطأ أثناء الحفظ: ${e.message}"))
            }
        }
    }

    fun deleteArrival(arrivalId: Long, productName: String) {
        viewModelScope.launch {
            try {
                repository.deleteArrival(arrivalId, productName)
                _uiMessages.emit(UiMessage.Success("تم حذف السلعة الواصلة"))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("فشل الحذف: ${e.message}"))
            }
        }
    }

    // --- Fast Sales Actions ---
    fun incrementSale(arrival: ArrivalWithDetails, step: Int = 1) {
        val currentSold = arrival.totalSold
        val target = currentSold + step
        if (target > arrival.arrival.quantity) {
            viewModelScope.launch {
                _uiMessages.emit(UiMessage.Error("الكمية المباعة لا يمكن أن تتجاوز الكمية الواصلة (${arrival.arrival.quantity})"))
            }
            return
        }
        setSaleQuantity(arrival.arrival.id, target)
    }

    fun decrementSale(arrival: ArrivalWithDetails, step: Int = 1) {
        val currentSold = arrival.totalSold
        val target = (currentSold - step).coerceAtLeast(0)
        setSaleQuantity(arrival.arrival.id, target)
    }

    fun setSaleQuantity(arrivalId: Long, quantity: Int) {
        viewModelScope.launch {
            val result = repository.updateSalesQuantity(arrivalId, quantity)
            result.onFailure {
                _uiMessages.emit(UiMessage.Error(it.message ?: "فشل تسجيل المبيعات"))
            }
        }
    }

    // --- Reconciliation (Waste / Leftovers) Actions ---
    fun recordReconciliation(
        arrivalId: Long,
        spoiledQty: Int,
        usableQty: Int,
        reason: String
    ) {
        viewModelScope.launch {
            val result = repository.recordWasteAndReconciliation(
                arrivalId = arrivalId,
                spoiledQuantity = spoiledQty,
                usableLeftoverQuantity = usableQty,
                wasteReason = reason
            )
            result.onSuccess {
                _uiMessages.emit(UiMessage.Success("تمت تسوية الكميات بنجاح وحفظها"))
            }.onFailure {
                _uiMessages.emit(UiMessage.Error(it.message ?: "خطأ في تسوية الكميات"))
            }
        }
    }

    // --- Expense Actions ---
    fun addExpense(
        category: String,
        description: String,
        amount: Long,
        notes: String? = null
    ) {
        viewModelScope.launch {
            if (amount <= 0) {
                _uiMessages.emit(UiMessage.Error("يرجى إدخال مبلغ صحيح"))
                return@launch
            }
            val dateStr = _selectedDate.value
            val epoch = BakeryRepository.parseDateToEpoch(dateStr)
            val expense = ExpenseEntity(
                category = category,
                description = description,
                amount = amount,
                expenseDate = epoch,
                dateString = dateStr,
                notes = notes
            )
            repository.insertExpense(expense)
            _uiMessages.emit(UiMessage.Success("تم تسجيل المصروف بنجاح"))
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense.id, expense.description, expense.amount)
            _uiMessages.emit(UiMessage.Success("تم حذف المصروف"))
        }
    }

    // --- Product Management Actions ---
    fun saveProduct(
        id: Long = 0,
        nameArabic: String,
        nameFrench: String?,
        category: String,
        unit: String,
        purchasePrice: Long,
        sellingPrice: Long,
        iconName: String = "bread"
    ) {
        viewModelScope.launch {
            if (nameArabic.isBlank()) {
                _uiMessages.emit(UiMessage.Error("يرجى إدخال اسم المنتج بالعربية"))
                return@launch
            }
            val product = ProductEntity(
                id = id,
                nameArabic = nameArabic.trim(),
                nameFrench = nameFrench?.trim()?.ifBlank { null },
                category = category,
                unit = unit,
                defaultPurchasePrice = purchasePrice,
                defaultSellingPrice = sellingPrice,
                iconName = iconName
            )
            if (id == 0L) {
                repository.insertProduct(product)
                _uiMessages.emit(UiMessage.Success("تمت إضافة المنتج بنجاح"))
            } else {
                repository.updateProduct(product)
                _uiMessages.emit(UiMessage.Success("تم تعديل المنتج بنجاح"))
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product.id, product.nameArabic)
                _uiMessages.emit(UiMessage.Success("تم حذف المنتج"))
            } catch (e: Exception) {
                _uiMessages.emit(UiMessage.Error("لا يمكن حذف المنتج لأنه مرتبط بسجلات وصول سابقة. يمكنك إلغاء تفعيله بدلاً من ذلك."))
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _uiMessages.emit(UiMessage.Success("تمت استعادة البيانات الافتراضية بنجاح"))
        }
    }
}
