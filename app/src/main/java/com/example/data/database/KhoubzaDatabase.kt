package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ArrivalDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.SaleDao
import com.example.data.dao.WasteDao
import com.example.data.entities.ArrivalEntity
import com.example.data.entities.AuditLogEntity
import com.example.data.entities.ExpenseEntity
import com.example.data.entities.InventoryEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SaleEntity
import com.example.data.entities.WasteEntity

@Database(
    entities = [
        ProductEntity::class,
        ArrivalEntity::class,
        SaleEntity::class,
        WasteEntity::class,
        InventoryEntity::class,
        ExpenseEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KhoubzaDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun arrivalDao(): ArrivalDao
    abstract fun saleDao(): SaleDao
    abstract fun wasteDao(): WasteDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: KhoubzaDatabase? = null

        fun getDatabase(context: Context): KhoubzaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhoubzaDatabase::class.java,
                    "khoubza_bakery.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
