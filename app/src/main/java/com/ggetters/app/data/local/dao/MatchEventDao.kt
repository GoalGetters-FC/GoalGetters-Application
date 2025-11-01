package com.ggetters.app.data.local.dao

import androidx.room.*
import com.ggetters.app.data.local.entity.MatchEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for MatchEvent entities.
 * Handles CRUD operations for match events (goals, cards, substitutions, etc.)
 */
@Dao
interface MatchEventDao {
    
    @Query("SELECT * FROM match_events WHERE matchId = :matchId ORDER BY minute DESC, timestamp DESC")
    fun getEventsByMatchId(matchId: String): Flow<List<MatchEventEntity>>
    
    @Query("SELECT * FROM match_events WHERE matchId = :matchId AND eventType = :eventType ORDER BY minute DESC, timestamp DESC")
    fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEventEntity>>
    
    @Query("SELECT * FROM match_events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): MatchEventEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: MatchEventEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<MatchEventEntity>)

    @Update
    suspend fun update(event: MatchEventEntity)
    
    @Delete
    suspend fun delete(event: MatchEventEntity)
    
    @Query("DELETE FROM match_events WHERE matchId = :matchId")
    suspend fun deleteEventsByMatchId(matchId: String)
    
    @Query("DELETE FROM match_events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: String)
    
    @Query("SELECT COUNT(*) FROM match_events WHERE matchId = :matchId")
    suspend fun getEventCountByMatchId(matchId: String): Int
}
