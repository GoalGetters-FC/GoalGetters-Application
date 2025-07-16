package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.Broadcast
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastDao {

    @Query("SELECT * FROM broadcast")
    fun getAll(): Flow<List<Broadcast>>

    @Query("SELECT * FROM broadcast WHERE id = :id")
    suspend fun getById(id: String): Broadcast?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(broadcast: Broadcast)

    @Delete
    suspend fun delete(broadcast: Broadcast)
}
