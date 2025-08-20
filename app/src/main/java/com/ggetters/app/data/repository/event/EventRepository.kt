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

    fun hydrateForTeam(id: String)
}
