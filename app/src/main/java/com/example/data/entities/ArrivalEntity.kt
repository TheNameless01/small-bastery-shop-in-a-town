package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "arrivals",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("productId"),
        Index("arrivalDate"),
        Index(value = ["arrivalDate", "productId"], unique = true)
    ]
)
data class ArrivalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val arrivalDate: Long, // Epoch day or normalized midnight timestamp
    val dateString: String, // e.g. "2026-10-31"
    val quantity: Int,     // Total received quantity
    val purchasePrice: Long, // Purchase price per unit for this arrival (DZD)
    val sellingPrice: Long,  // Selling price per unit for this arrival (DZD)
    val status: String = "CONFIRMED", // "DRAFT", "CONFIRMED"
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
