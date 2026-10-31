package com.example.data.models

import androidx.room.Embedded
import androidx.room.Relation
import com.example.data.entities.ArrivalEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SaleEntity
import com.example.data.entities.WasteEntity

data class ArrivalWithDetails(
    @Embedded
    val arrival: ArrivalEntity,

    @Relation(
        parentColumn = "productId",
        entityColumn = "id"
    )
    val product: ProductEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "arrivalId"
    )
    val sales: List<SaleEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "arrivalId"
    )
    val wastes: List<WasteEntity>
) {
    val totalSold: Int
        get() = sales.sumOf { it.quantity }

    val totalSpoiled: Int
        get() = wastes.sumOf { it.quantity }

    val remainingUnsold: Int
        get() = (arrival.quantity - totalSold).coerceAtLeast(0)

    val unreconciledRemaining: Int
        get() = (arrival.quantity - totalSold - totalSpoiled).coerceAtLeast(0)

    val arrivalPurchaseCost: Long
        get() = arrival.quantity.toLong() * arrival.purchasePrice

    val potentialRevenue: Long
        get() = arrival.quantity.toLong() * arrival.sellingPrice

    val actualRevenue: Long
        get() = totalSold.toLong() * arrival.sellingPrice

    val costOfSold: Long
        get() = totalSold.toLong() * arrival.purchasePrice

    val wasteFinancialLoss: Long
        get() = totalSpoiled.toLong() * arrival.purchasePrice
}
