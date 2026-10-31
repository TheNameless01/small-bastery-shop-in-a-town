package com.example.data.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("expenseDate")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "نقل وتوصيل", "كهرباء وغاز", "تغليف وأكياس", "صيانة", "أخرى"
    val description: String,
    val amount: Long, // In DZD
    val expenseDate: Long,
    val dateString: String,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
