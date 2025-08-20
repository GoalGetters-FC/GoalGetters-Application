// app/src/main/java/com/ggetters/app/data/local/dao/EventDao.kt
package com.ggetters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ggetters.app.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM event ORDER BY start_at ASC")
    fun getAll(): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: String): Event?

    @Query("SELECT * FROM event WHERE team_id = :teamId ORDER BY start_at ASC")
    fun getByTeamId(teamId: String): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM event")
    suspend fun deleteAll()

    // ----- dirty/sync helpers -----
    @Query("SELECT * FROM event WHERE team_id = :teamId AND stained_at IS NOT NULL")
    suspend fun getDirtyEvents(teamId: String): List<Event>

    @Query("UPDATE event SET stained_at = NULL WHERE id = :id")
    suspend fun markClean(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<Event>)

    // ----- filters -----
    // If start_at is stored as ISO-8601 TEXT, this works; otherwise compare epoch millis.
    @Query("""
        SELECT * FROM event
        WHERE team_id = :teamId
          AND date(start_at) >= date(:startDate)
          AND date(start_at) <= date(:endDate)
        ORDER BY start_at ASC
    """)
    suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,  // e.g. "2025-08-20" or "2025-08-20T00:00:00"
        endDate: String
    ): List<Event>

    @Query("""
        SELECT * FROM event
        WHERE team_id = :teamId AND category = :category
        ORDER BY start_at ASC
    """)
    fun getEventsByType(teamId: String, category: Int): Flow<List<Event>>

    @Query("""
        SELECT * FROM event
        WHERE creator_id = :creatorId
        ORDER BY start_at DESC
    """)
    fun getEventsByCreator(creatorId: String): Flow<List<Event>>
}
