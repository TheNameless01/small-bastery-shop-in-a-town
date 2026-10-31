package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameArabic: String,
    val nameFrench: String? = null,
    val category: String = "خبز", // e.g. "خبز", "حلويات", "فطائر", "أخرى"
    val unit: String = "قطعة",    // e.g. "قطعة", "وحدة", "كغ"
    val defaultPurchasePrice: Long = 0L, // DZD
    val defaultSellingPrice: Long = 0L,  // DZD
    val isActive: Boolean = true,
    val iconName: String = "bread",
    val createdAt: Long = System.currentTimeMillis()
)
