package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.WasteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    @Query("SELECT * FROM wastes WHERE arrivalId = :arrivalId")
    fun getWastesForArrival(arrivalId: Long): Flow<List<WasteEntity>>

    @Query("SELECT * FROM wastes WHERE arrivalId = :arrivalId")
    suspend fun getWastesListForArrival(arrivalId: Long): List<WasteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaste(waste: WasteEntity): Long

    @Update
    suspend fun updateWaste(waste: WasteEntity)

    @Query("DELETE FROM wastes WHERE id = :id")
    suspend fun deleteWasteById(id: Long)

    @Query("DELETE FROM wastes WHERE arrivalId = :arrivalId")
    suspend fun deleteWastesByArrivalId(arrivalId: Long)
}
