package com.ggetters.app.data.repository.event

import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineEventRepository @Inject constructor(
    private val dao: EventDao
) : EventRepository {

    override fun all(): Flow<List<Event>> = dao.getAll()

    override suspend fun getById(id: String): Event? = dao.getById(id)

    override suspend fun upsert(entity: Event) {
        entity.stain()
        dao.upsert(entity)
    }

    override suspend fun delete(entity: Event) {
        dao.deleteById(entity.id)
    }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override suspend fun sync() {
        /* no-op for offline repo */
    }

    override fun getByTeamId(teamId: String): Flow<List<Event>> =
        dao.getByTeamId(teamId)

    override suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ): List<Event> = dao.getEventsByDateRange(teamId, startDate, endDate)

    override fun getEventsByType(teamId: String, category: EventCategory): Flow<List<Event>> =
        dao.getEventsByType(teamId, category)

    override fun getEventsByCreator(creatorId: String): Flow<List<Event>> =
        dao.getEventsByCreator(creatorId)

    override fun hydrateForTeam(id: String) {
        /* no-op; combined repo handles team scoping */
    }

    // helpers for Combined
    suspend fun getDirtyEvents(teamId: String) = dao.getDirtyEvents(teamId)
    suspend fun markClean(id: String) = dao.markClean(id)
    suspend fun upsertAllLocal(events: List<Event>) = dao.upsertAll(events)
}
