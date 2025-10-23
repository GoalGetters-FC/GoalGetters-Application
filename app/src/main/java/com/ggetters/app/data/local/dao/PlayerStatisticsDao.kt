package com.ggetters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ggetters.app.data.model.PlayerStatistics
import kotlinx.coroutines.flow.Flow

/**
 * DAO for PlayerStatistics entities.
 * Handles CRUD operations for player statistics.
 */
@Dao
interface PlayerStatisticsDao {
    
    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId")
    suspend fun getByPlayerId(playerId: String): PlayerStatistics?
    
    @Query("SELECT * FROM player_statistics WHERE playerId = :playerId")
    fun getByPlayerIdFlow(playerId: String): Flow<PlayerStatistics?>
    
    @Query("SELECT * FROM player_statistics")
    fun getAll(): Flow<List<PlayerStatistics>>
    
    @Query("SELECT * FROM player_statistics WHERE playerId IN (:playerIds)")
    fun getByPlayerIds(playerIds: List<String>): Flow<List<PlayerStatistics>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(statistics: PlayerStatistics)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(statistics: List<PlayerStatistics>)
    
    @Query("DELETE FROM player_statistics WHERE playerId = :playerId")
    suspend fun deleteByPlayerId(playerId: String)
    
    @Query("DELETE FROM player_statistics")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM player_statistics")
    suspend fun getCount(): Int
}
