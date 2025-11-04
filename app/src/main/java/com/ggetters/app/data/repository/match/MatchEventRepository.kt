package com.ggetters.app.data.repository.match

import com.ggetters.app.data.model.MatchEvent
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for MatchEvent operations.
 * Handles CRUD operations for match events (goals, cards, substitutions, etc.)
 */
interface MatchEventRepository {
    
    /**
     * Get all events for a specific match, ordered by minute (newest first)
     */
    fun getEventsByMatchId(matchId: String): Flow<List<MatchEvent>>
    
    /**
     * Get events of a specific type for a match
     */
    fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEvent>>
    
    /**
     * Get a specific event by ID
     */
    suspend fun getEventById(eventId: String): MatchEvent?
    
    /**
     * Insert a new match event
     */
    suspend fun insertEvent(event: MatchEvent)
    
    /**
     * Update an existing match event
     */
    suspend fun updateEvent(event: MatchEvent)
    
    /**
     * Delete a match event
     */
    suspend fun deleteEvent(event: MatchEvent)
    
    /**
     * Delete all events for a specific match
     */
    suspend fun deleteEventsByMatchId(matchId: String)
    
    /**
     * Delete a specific event by ID
     */
    suspend fun deleteEventById(eventId: String)
    
    /**
     * Get the count of events for a match
     */
    suspend fun getEventCountByMatchId(matchId: String): Int
}
