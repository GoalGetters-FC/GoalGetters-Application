package com.ggetters.app.data.repository.event

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CombinedEventRepository @Inject constructor(
    private val offline: OfflineEventRepository,
    private val online: OnlineEventRepository,
    private val teamRepo: com.ggetters.app.data.repository.team.TeamRepository
) : EventRepository {

    // Stream events for the active team
    override fun all(): Flow<List<Event>> =
        teamRepo.getActiveTeam().flatMapLatest { t ->
            t?.let { offline.getByTeamId(it.id) } ?: flowOf(emptyList())
        }

    override suspend fun getById(id: String): Event? {
        val teamId = teamRepo.getActiveTeam().first()?.id ?: return null
        return offline.getById(id) ?: online.getById(teamId, id)
    }

    override suspend fun upsert(entity: Event) = offline.upsert(entity)

    override suspend fun delete(entity: Event) = offline.delete(entity)

    override suspend fun deleteAll() = offline.deleteAll()

    // Push dirty → Pull fresh remote → Replace local
    override suspend fun sync() {
        val team = teamRepo.getActiveTeam().first() ?: return
        val teamId = team.id

        // Push dirty events first
        offline.getDirtyEvents(teamId).forEach { e ->
            runCatching { online.upsert(e) }
                .onSuccess { offline.markClean(e.id) }
        }

        // Pull remote + replace local
        val remote = online.fetchAllForTeam(teamId)
        offline.upsertAllLocal(remote)
    }

    override fun getByTeamId(teamId: String): Flow<List<Event>> =
        offline.getByTeamId(teamId)

    override suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ) = offline.getEventsByDateRange(teamId, startDate, endDate)

    override fun getEventsByType(
        teamId: String,
        category: EventCategory
    ): Flow<List<Event>> = offline.getEventsByType(teamId, category)

    override fun getEventsByCreator(creatorId: String) =
        offline.getEventsByCreator(creatorId)

<<<<<<< HEAD
    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        try {
            offline.deleteAll()
            online.deleteAll()
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete all events: ${e.message}")
        }
    }
    // TODO: Backend - Implement combined RSVP management
    // TODO: Backend - Add combined event analytics
    // TODO: Backend - Implement combined conflict detection
    // TODO: Backend - Add combined search functionality
    // TODO: Backend - Implement combined recurring event support
} 
=======
    override fun hydrateForTeam(id: String) { /* optional no-op */ }
}
>>>>>>> origin/staging
