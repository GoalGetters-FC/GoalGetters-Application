package com.ggetters.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ggetters.app.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    // TODO: Backend - Implement comprehensive event queries
    // TODO: Backend - Add date range filtering for calendar views
    // TODO: Backend - Add team-specific event filtering
    // TODO: Backend - Add event status filtering (active, cancelled, completed)
    // TODO: Backend - Add full-text search for event titles and descriptions

    @Query("SELECT * FROM event ORDER BY start_at ASC")
    fun getAll(): Flow<List<Event>>

    @Query("SELECT * FROM event WHERE id = :id")
    suspend fun getById(id: String): Event?

    @Query("SELECT * FROM event WHERE team_id = :teamId ORDER BY start_at ASC")
    fun getByTeamId(teamId: String): Flow<List<Event>>

    // TODO: Backend - Implement date range queries for calendar
    @Query("SELECT * FROM event WHERE team_id = :teamId AND date(start_at) BETWEEN :startDate AND :endDate ORDER BY start_at ASC")
    suspend fun getEventsByDateRange(teamId: String, startDate: String, endDate: String): List<Event>

    // TODO: Backend - Implement event type filtering
    @Query("SELECT * FROM event WHERE team_id = :teamId AND category = :category ORDER BY start_at ASC")
    fun getEventsByType(teamId: String, category: Int): Flow<List<Event>>

    // TODO: Backend - Implement creator filtering
    @Query("SELECT * FROM event WHERE creator_id = :creatorId ORDER BY start_at ASC")
    fun getEventsByCreator(creatorId: String): Flow<List<Event>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("DELETE FROM event WHERE id = :id")
    suspend fun deleteById(id: String)

    // TODO: Backend - Implement soft delete for events
    @Query("UPDATE event SET stained_at = :stainedAt WHERE id = :id")
    suspend fun markAsDeleted(id: String, stainedAt: Long)

    // TODO: Backend - Implement recurring event support
    // TODO: Backend - Add methods for event conflicts detection
    // TODO: Backend - Add performance optimization with indexes
    // TODO: Backend - Add event statistics queries
    
    // Upsert operation (insert or replace)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: Event)
} 