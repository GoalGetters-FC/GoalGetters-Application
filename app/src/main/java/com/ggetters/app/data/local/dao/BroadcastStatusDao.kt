package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BroadcastStatusDao {

    @Query("SELECT * FROM broadcast_status WHERE broadcast_id = :broadcastId")
    fun getAllForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>>

    @Query("""
      SELECT * FROM broadcast_status 
      WHERE broadcast_id = :broadcastId 
        AND recipient_id = :recipientId
    """)
    suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(status: BroadcastStatus)

    @Delete
    suspend fun delete(status: BroadcastStatus)
}
