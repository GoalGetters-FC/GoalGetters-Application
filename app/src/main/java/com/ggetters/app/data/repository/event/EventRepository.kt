package com.ggetters.app.data.repository.event

import com.ggetters.app.data.model.Event
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Event operations.
 * Extends CrudRepository and adds event-specific functionality.
 * 
 * TODO: Backend - Add comprehensive event filtering methods
 * TODO: Backend - Implement calendar-specific queries
 * TODO: Backend - Add event conflict detection
 * TODO: Backend - Implement recurring event support
 * TODO: Backend - Add event statistics methods
 */
interface EventRepository : CrudRepository<Event> {
    
    /**
     * Pull all Events from Firestore and upsert into Room.
     * 
     * TODO: Backend - Implement proper sync logic
     * TODO: Backend - Add conflict resolution strategy
     * TODO: Backend - Add incremental sync support
     */
    suspend fun sync()
    
    /**
     * Get events for a specific team.
     * 
     * TODO: Backend - Implement team-based filtering
     * TODO: Backend - Add pagination support
     * TODO: Backend - Add sorting options
     */
    fun getByTeamId(teamId: String): Flow<List<Event>>
    
    /**
     * Get events within a date range for calendar view.
     * 
     * TODO: Backend - Implement date range filtering
     * TODO: Backend - Add time zone handling
     * TODO: Backend - Optimize for calendar performance
     */
    suspend fun getEventsByDateRange(teamId: String, startDate: String, endDate: String): List<Event>
    
    /**
     * Get events by type (Practice, Match, Other).
     * 
     * TODO: Backend - Implement event type filtering
     * TODO: Backend - Add support for custom event types
     */
    fun getEventsByType(teamId: String, category: Int): Flow<List<Event>>
    
    /**
     * Get events created by a specific user.
     * 
     * TODO: Backend - Implement creator filtering
     * TODO: Backend - Add permission checks
     */
    fun getEventsByCreator(creatorId: String): Flow<List<Event>>
    
    // TODO: Backend - Add RSVP management methods
    // TODO: Backend - Add event template methods
    // TODO: Backend - Add recurring event methods
    // TODO: Backend - Add event conflict detection methods
    // TODO: Backend - Add event notification methods
    // TODO: Backend - Add event analytics methods

    /**
     * Delete all events from the repository.
    */
    suspend fun deleteAll()

} 