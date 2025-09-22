package com.ggetters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the `event` table.
 *
 * Provides local database operations for events.
 * Used by OfflineEventRepository to support offline-first behavior.
 */
@Dao
interface EventDao {

    /** Returns all events ordered by start date. */
    @Query("SELECT * FROM event ORDER BY start_at ASC")
    fun getAll(): Flow<List<Event>>

    /** Returns a single event by ID. */
    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: String): Event?

    /** Returns all events for a given team. */
    @Query("SELECT * FROM event WHERE team_id = :teamId ORDER BY start_at ASC")
    fun getByTeamId(teamId: String): Flow<List<Event>>

    /** Insert or replace a single event. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: Event)

    /** Insert or replace multiple events. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<Event>)

    /** Delete a single event by ID. */
    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: String)

    /** Delete all events. */
    @Query("DELETE FROM event")
    suspend fun deleteAll()

    // ---------- Dirty/sync helpers ----------

    /** Returns events that were modified locally and not yet synced. */
    @Query("SELECT * FROM event WHERE team_id = :teamId AND stained_at IS NOT NULL")
    suspend fun getDirtyEvents(teamId: String): List<Event>

    /** Marks an event as clean (synced). */
    @Query("UPDATE event SET stained_at = NULL WHERE id = :id")
    suspend fun markClean(id: String)

    // ---------- Filters ----------

    /** Get events for a team within a date range. */
    @Query("""
        SELECT * FROM event
        WHERE team_id = :teamId
          AND date(start_at) >= date(:startDate)
          AND date(start_at) <= date(:endDate)
        ORDER BY start_at ASC
    """)
    suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ): List<Event>

    /** Get events of a specific type. */
    @Query("""
        SELECT * FROM event
        WHERE team_id = :teamId AND category = :category
        ORDER BY start_at ASC
    """)
    fun getEventsByType(
        teamId: String,
        category: EventCategory
    ): Flow<List<Event>>

    /** Get events created by a specific user. */
    @Query("""
        SELECT * FROM event
        WHERE creator_id = :creatorId
        ORDER BY start_at DESC
    """)
    fun getEventsByCreator(creatorId: String): Flow<List<Event>>
}
