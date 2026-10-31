package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales WHERE arrivalId = :arrivalId")
    fun getSalesForArrival(arrivalId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE arrivalId = :arrivalId")
    suspend fun getSalesListForArrival(arrivalId: Long): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sales WHERE arrivalId = :arrivalId")
    suspend fun deleteSalesByArrivalId(arrivalId: Long)
}
