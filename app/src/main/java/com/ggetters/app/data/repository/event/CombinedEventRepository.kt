package com.ggetters.app.data.repository.event

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
        return offline.getById(id) ?: online.getById(id)
    }

    override suspend fun upsert(entity: Event) = offline.upsert(entity)

    override suspend fun delete(entity: Event) = offline.delete(entity)

    override suspend fun deleteAll() = offline.deleteAll()

    override suspend fun sync() {
        Clogger.i("EventRepo", "Syncing events …")

        try {
            val team = teamRepo.getActiveTeam().first()
            if (team == null) {
                Clogger.w("EventRepo", "No active team → skipping sync")
                return
            }
            val teamId = team.id

            // --- Push dirty ---
            offline.getDirtyEvents(teamId).forEach { e ->
                runCatching { online.upsert(e) }
                    .onSuccess { offline.markClean(e.id) }
                    .onFailure { err ->
                        Clogger.e("EventRepo", "Failed to push event ${e.id}: ${err.message}", err)
                    }
            }

            // --- Pull remote ---
            val remote = online.fetchAllForTeam(teamId)
            Clogger.i("EventRepo", "Fetched ${remote.size} remote events")

            // Filter invalid events (defensive)
            val validRemote = remote.filter { e ->
                if (e.teamId.isBlank() || e.startAt == null) {
                    Clogger.w("EventRepo", "Skipping invalid event ${e.id}")
                    false
                } else true
            }

            // --- Guarded upsert: never stomp dirty local changes ---
            validRemote.forEach { remoteEvent ->
                offline.upsertFromRemote(remoteEvent)
            }
            Clogger.i("EventRepo", "Applied ${validRemote.size} remote events to local DB (guarded)")

        } catch (e: Throwable) {
            Clogger.e("EventRepo", "Sync failed: ${e.message}", e)
        }
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

    override fun hydrateForTeam(id: String) {
        // Run sync in the background so it won't block UI
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                Clogger.d("EventRepo", "Hydrating events for team $id …")
                sync()
            } catch (e: Exception) {
                Clogger.e("EventRepo", "Hydrate failed for team $id", e)
            }
        }
    }

}
