package com.ggetters.app.data.repository.event

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.remote.firestore.EventFirestore
import com.ggetters.app.data.repository.team.TeamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class OnlineEventRepository @Inject constructor(
    private val fs: EventFirestore,
    private val teamRepo: TeamRepository
) : EventRepository {

    override fun all(): Flow<List<Event>> = flowOf(emptyList()) // avoid unscoped global

    suspend fun fetchAllForTeam(teamId: String): List<Event> =
        fs.fetchAllForTeam(teamId)

    // ✅ Public getById now always resolves active teamId internally
    override suspend fun getById(id: String): Event? {
        val teamId = teamRepo.getActiveTeam().first()?.id ?: return null
        return fs.getById(teamId, id)
    }

    override suspend fun upsert(entity: Event) {
        fs.upsert(entity.teamId, entity)
    }

    override suspend fun delete(entity: Event) {
        fs.delete(entity.teamId, entity.id)
    }

    override suspend fun deleteAll() { /* no-op */ }
    override suspend fun sync() { /* no-op */ }

    override fun getByTeamId(teamId: String): Flow<List<Event>> =
        fs.observeByTeamId(teamId)

    override suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ) = throw NotImplementedError()

    override fun getEventsByType(
        teamId: String,
        category: EventCategory
    ): Flow<List<Event>> = throw NotImplementedError()

    override fun getEventsByCreator(
        creatorId: String
    ): Flow<List<Event>> = throw NotImplementedError()

    override fun hydrateForTeam(id: String) { /* no-op */ }
}
