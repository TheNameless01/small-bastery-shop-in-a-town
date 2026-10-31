package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ArrivalEntity::class,
            parentColumns = ["id"],
            childColumns = ["arrivalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("arrivalId")
    ]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val arrivalId: Long,
    val quantity: Int,
    val saleDate: Long,
    val createdAt: Long = System.currentTimeMillis()
)
