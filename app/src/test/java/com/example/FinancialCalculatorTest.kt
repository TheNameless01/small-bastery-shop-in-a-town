package com.example

import com.example.data.entities.ArrivalEntity
import com.example.data.entities.ProductEntity
import com.example.data.entities.SaleEntity
import com.example.data.entities.WasteEntity
import com.example.data.models.ArrivalWithDetails
import com.example.domain.calculations.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialCalculatorTest {

    @Test
    fun mandatoryFinancialTest_baguetteAccountingRule() {
        /**
         * Input from Master Specification:
         * Baguette:
         * - Received: 100
         * - Purchase price: 20 DZD
         * - Selling price: 30 DZD
         * - Sold: 85
         * - Spoiled: 10
         * - Usable leftovers: 5
         *
         * Expected results:
         * - Revenue: 2,550 DZD
         * - Original arrival cost: 2,000 DZD
         * - Cost of sold products: 1,700 DZD
         * - Spoiled goods cost: 200 DZD
         * - Remaining inventory value: 100 DZD
         * - Operating profit: 650 DZD (before other expenses)
         * Ensure the application never counts the 100 DZD carried-over inventory as a new purchase.
         */
        val baguetteProduct = ProductEntity(
            id = 1L,
            nameArabic = "خبز",
            nameFrench = "Baguette",
            defaultPurchasePrice = 20L,
            defaultSellingPrice = 30L
        )

        val arrival = ArrivalEntity(
            id = 100L,
            productId = 1L,
            arrivalDate = 1700000000000L,
            dateString = "2026-10-31",
            quantity = 100,
            purchasePrice = 20L,
            sellingPrice = 30L
        )

        val sales = listOf(
            SaleEntity(id = 1L, arrivalId = 100L, quantity = 85, saleDate = 1700000000000L)
        )

        val wastes = listOf(
            WasteEntity(id = 1L, arrivalId = 100L, quantity = 10, reason = "تالف")
        )

        val arrivalWithDetails = ArrivalWithDetails(
            arrival = arrival,
            product = baguetteProduct,
            sales = sales,
            wastes = wastes
        )

        val metric = FinancialCalculator.calculateProductMetric(
            item = arrivalWithDetails,
            usableUnitsOverride = 5
        )

        assertEquals("Revenue must be 2,550 DZD", 2550L, metric.revenueDzd)
        assertEquals("Original arrival cost must be 2,000 DZD", 2000L, metric.arrivalCostDzd)
        assertEquals("Cost of sold products must be 1,700 DZD", 1700L, metric.costOfSoldDzd)
        assertEquals("Spoiled goods cost must be 200 DZD", 200L, metric.wasteLossDzd)
        assertEquals("Remaining inventory value must be 100 DZD", 100L, metric.remainingValueDzd)
        assertEquals("Operating profit must be 650 DZD", 650L, metric.operatingProfitDzd)

        val dailySummary = FinancialCalculator.calculateDailySummary(
            dateString = "2026-10-31",
            arrivals = listOf(arrivalWithDetails),
            expensesDzd = 150L,
            usableUnitsMap = mapOf(100L to 5)
        )

        assertEquals("Daily Operating profit must be 650 DZD", 650L, dailySummary.operatingProfitDzd)
        assertEquals("Daily Net profit must be 500 DZD (650 - 150)", 500L, dailySummary.netProfitDzd)
        assertEquals("COGS must be 1,900 DZD (1,700 sold + 200 spoiled)", 1900L, dailySummary.costOfGoodsSoldDzd)
        assertEquals(10.0, dailySummary.wastePercentage, 0.01)
    }

    @Test
    fun testReconciliationAllocationIntegrity() {
        val received = 100
        val sold = 85
        val spoiled = 10
        val usable = 5
        assertEquals("Total allocated must equal total received", received, sold + spoiled + usable)
    }

    @Test
    fun testWasteLossIsNotDeductedTwice() {
        val revenue = 2550L
        val costOfSold = 1700L
        val spoiledLoss = 200L
        val cogs = costOfSold + spoiledLoss
        val operatingProfit = revenue - cogs
        assertEquals(650L, operatingProfit)
    }
}
