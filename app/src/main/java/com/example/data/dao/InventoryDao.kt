package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventories WHERE status = 'CARRIED_FORWARD' AND remainingQuantity > 0")
    fun getActiveCarriedInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventories WHERE sourceArrivalId = :arrivalId")
    suspend fun getInventoryForArrival(arrivalId: Long): List<InventoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(inventory: InventoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventories(inventories: List<InventoryEntity>): List<Long>

    @Update
    suspend fun updateInventory(inventory: InventoryEntity)

    @Query("UPDATE inventories SET remainingQuantity = :remaining, status = :status WHERE id = :id")
    suspend fun updateRemaining(id: Long, remaining: Int, status: String)

    @Query("DELETE FROM inventories WHERE sourceArrivalId = :arrivalId")
    suspend fun deleteInventoryByArrivalId(arrivalId: Long)
}
