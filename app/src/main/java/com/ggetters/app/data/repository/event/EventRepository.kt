package com.ggetters.app.data.repository.event

import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow

interface EventRepository : CrudRepository<Event> {
    suspend fun sync()

    fun getByTeamId(teamId: String): Flow<List<Event>>

    suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ): List<Event>

    fun getEventsByType(
        teamId: String,
        category: EventCategory
    ): Flow<List<Event>>

    fun getEventsByCreator(creatorId: String): Flow<List<Event>>
<<<<<<< HEAD
    fun hydrateForTeam(id: String)

    // TODO: Backend - Add RSVP management methods
    // TODO: Backend - Add event template methods
    // TODO: Backend - Add recurring event methods
    // TODO: Backend - Add event conflict detection methods
    // TODO: Backend - Add event notification methods
    // TODO: Backend - Add event analytics methods
=======
>>>>>>> origin/staging

    fun hydrateForTeam(id: String)
}
