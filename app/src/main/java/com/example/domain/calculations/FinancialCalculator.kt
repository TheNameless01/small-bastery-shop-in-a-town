package com.example.domain.calculations

import com.example.data.models.ArrivalWithDetails

data class DailyFinancialSummary(
    val dateString: String,
    val totalReceivedUnits: Int,
    val totalSoldUnits: Int,
    val totalSpoiledUnits: Int,
    val totalUsableLeftoverUnits: Int,
    val totalRevenueDzd: Long,
    val totalArrivalCostDzd: Long,
    val costOfSoldProductsDzd: Long,
    val spoiledGoodsLossDzd: Long,
    val remainingInventoryValueDzd: Long,
    val costOfGoodsSoldDzd: Long,
    val operatingProfitDzd: Long,
    val totalExpensesDzd: Long,
    val netProfitDzd: Long,
    val wastePercentage: Double
)

data class ProductFinancialMetric(
    val productId: Long,
    val arabicName: String,
    val frenchName: String?,
    val unit: String,
    val receivedUnits: Int,
    val soldUnits: Int,
    val spoiledUnits: Int,
    val usableUnits: Int,
    val purchasePrice: Long,
    val sellingPrice: Long,
    val revenueDzd: Long,
    val arrivalCostDzd: Long,
    val costOfSoldDzd: Long,
    val wasteLossDzd: Long,
    val remainingValueDzd: Long,
    val operatingProfitDzd: Long,
    val wastePercentage: Double
)

object FinancialCalculator {

    /**
     * Calculates the financial metrics for a single product arrival record.
     * Respects the exact accounting rule:
     * COGS = Cost of sold units + Cost of spoiled units.
     * Usable remaining stock is an asset, never counted as loss or new purchase.
     */
    fun calculateProductMetric(
        item: ArrivalWithDetails,
        usableUnitsOverride: Int? = null
    ): ProductFinancialMetric {
        val received = item.arrival.quantity
        val sold = item.totalSold
        val spoiled = item.totalSpoiled
        val purchasePrice = item.arrival.purchasePrice
        val sellingPrice = item.arrival.sellingPrice

        val usable = usableUnitsOverride ?: (received - sold - spoiled).coerceAtLeast(0)

        val revenue = sold.toLong() * sellingPrice
        val arrivalCost = received.toLong() * purchasePrice
        val costOfSold = sold.toLong() * purchasePrice
        val wasteLoss = spoiled.toLong() * purchasePrice
        val remainingValue = usable.toLong() * purchasePrice
        val cogs = costOfSold + wasteLoss
        val operatingProfit = revenue - cogs
        val wastePct = if (received > 0) (spoiled.toDouble() / received.toDouble()) * 100.0 else 0.0

        return ProductFinancialMetric(
            productId = item.product.id,
            arabicName = item.product.nameArabic,
            frenchName = item.product.nameFrench,
            unit = item.product.unit,
            receivedUnits = received,
            soldUnits = sold,
            spoiledUnits = spoiled,
            usableUnits = usable,
            purchasePrice = purchasePrice,
            sellingPrice = sellingPrice,
            revenueDzd = revenue,
            arrivalCostDzd = arrivalCost,
            costOfSoldDzd = costOfSold,
            wasteLossDzd = wasteLoss,
            remainingValueDzd = remainingValue,
            operatingProfitDzd = operatingProfit,
            wastePercentage = wastePct
        )
    }

    /**
     * Calculates daily financial summary across all products and expenses for that day.
     *
     * Rules:
     * - Do NOT subtract Gâchi/waste costs twice.
     * - Total COGS = costOfSold + spoiledLoss.
     * - Operating Profit = totalRevenue - COGS.
     * - Net Profit = Operating Profit - totalExpenses.
     */
    fun calculateDailySummary(
        dateString: String,
        arrivals: List<ArrivalWithDetails>,
        expensesDzd: Long = 0L,
        usableUnitsMap: Map<Long, Int> = emptyMap()
    ): DailyFinancialSummary {
        var totalReceived = 0
        var totalSold = 0
        var totalSpoiled = 0
        var totalUsable = 0
        var totalRevenue = 0L
        var totalArrivalCost = 0L
        var costOfSold = 0L
        var spoiledLoss = 0L
        var remainingInventoryValue = 0L

        for (item in arrivals) {
            val usableOverride = usableUnitsMap[item.arrival.id]
            val metric = calculateProductMetric(item, usableOverride)
            totalReceived += metric.receivedUnits
            totalSold += metric.soldUnits
            totalSpoiled += metric.spoiledUnits
            totalUsable += metric.usableUnits
            totalRevenue += metric.revenueDzd
            totalArrivalCost += metric.arrivalCostDzd
            costOfSold += metric.costOfSoldDzd
            spoiledLoss += metric.wasteLossDzd
            remainingInventoryValue += metric.remainingValueDzd
        }

        val cogs = costOfSold + spoiledLoss
        val operatingProfit = totalRevenue - cogs
        val netProfit = operatingProfit - expensesDzd
        val wastePercentage = if (totalReceived > 0) {
            (totalSpoiled.toDouble() / totalReceived.toDouble()) * 100.0
        } else {
            0.0
        }

        return DailyFinancialSummary(
            dateString = dateString,
            totalReceivedUnits = totalReceived,
            totalSoldUnits = totalSold,
            totalSpoiledUnits = totalSpoiled,
            totalUsableLeftoverUnits = totalUsable,
            totalRevenueDzd = totalRevenue,
            totalArrivalCostDzd = totalArrivalCost,
            costOfSoldProductsDzd = costOfSold,
            spoiledGoodsLossDzd = spoiledLoss,
            remainingInventoryValueDzd = remainingInventoryValue,
            costOfGoodsSoldDzd = cogs,
            operatingProfitDzd = operatingProfit,
            totalExpensesDzd = expensesDzd,
            netProfitDzd = netProfit,
            wastePercentage = wastePercentage
        )
    }
}
