package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.entities.ArrivalEntity
import com.example.data.models.ArrivalWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ArrivalDao {
    @Transaction
    @Query("SELECT * FROM arrivals WHERE arrivalDate = :arrivalDate ORDER BY id ASC")
    fun getArrivalsWithDetailsByDate(arrivalDate: Long): Flow<List<ArrivalWithDetails>>

    @Transaction
    @Query("SELECT * FROM arrivals WHERE dateString = :dateString ORDER BY id ASC")
    fun getArrivalsWithDetailsByDateString(dateString: String): Flow<List<ArrivalWithDetails>>

    @Transaction
    @Query("SELECT * FROM arrivals WHERE arrivalDate BETWEEN :startDate AND :endDate ORDER BY arrivalDate DESC")
    fun getArrivalsWithDetailsBetween(startDate: Long, endDate: Long): Flow<List<ArrivalWithDetails>>

    @Query("SELECT * FROM arrivals WHERE arrivalDate = :arrivalDate AND productId = :productId LIMIT 1")
    suspend fun getArrivalByDateAndProduct(arrivalDate: Long, productId: Long): ArrivalEntity?

    @Query("SELECT * FROM arrivals WHERE id = :id")
    suspend fun getArrivalById(id: Long): ArrivalEntity?

    @Transaction
    @Query("SELECT * FROM arrivals WHERE id = :id")
    suspend fun getArrivalWithDetailsById(id: Long): ArrivalWithDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrival(arrival: ArrivalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArrivals(arrivals: List<ArrivalEntity>): List<Long>

    @Update
    suspend fun updateArrival(arrival: ArrivalEntity)

    @Query("DELETE FROM arrivals WHERE id = :id")
    suspend fun deleteArrivalById(id: Long)

    @Query("SELECT DISTINCT dateString FROM arrivals ORDER BY arrivalDate DESC")
    fun getAllRecordedDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM arrivals")
    suspend fun getTotalArrivalCount(): Int
}
