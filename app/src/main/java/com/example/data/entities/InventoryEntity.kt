package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventories",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ArrivalEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceArrivalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productId"),
        Index("sourceArrivalId")
    ]
)
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val sourceArrivalId: Long,
    val originalQuantity: Int,
    val remainingQuantity: Int,
    val unitCost: Long, // Preserves original purchase cost DZD
    val status: String = "CARRIED_FORWARD", // "CARRIED_FORWARD", "CONSUMED", "DISCARDED"
    val carryDate: Long = System.currentTimeMillis(),
    val notes: String? = null
)
