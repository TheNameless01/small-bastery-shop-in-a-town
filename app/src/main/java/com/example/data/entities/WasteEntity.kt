package com.example.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wastes",
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
data class WasteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val arrivalId: Long,
    val quantity: Int,
    val reason: String = "تالف / غير صالح للبيع",
    val createdAt: Long = System.currentTimeMillis()
)
