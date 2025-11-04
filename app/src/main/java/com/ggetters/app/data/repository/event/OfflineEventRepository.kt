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
        // Mark dirty and update timestamp on local edits
        entity.touch() // Updates updatedAt
        try {
            entity.stain() // Marks as dirty (stained)
        } catch (e: IllegalStateException) {
            // Already dirty, just touch to update timestamp
        }
        dao.upsert(entity)
    }

    override suspend fun delete(entity: Event) {
        dao.deleteById(entity.id)
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
    
    /**
     * Guarded upsert from remote: only updates if local is not dirty AND remote is newer.
     * Never stomps dirty local changes.
     */
    suspend fun upsertFromRemote(remote: Event) {
        val local = dao.getById(remote.id)
        
        // Debug logging
        com.ggetters.app.core.utils.Clogger.d(
            "OfflineEventRepository",
            "upsertFromRemote: id=${remote.id}, local=${local?.id}, " +
                    "localDirty=${local?.isStained()}, " +
                    "localUpdatedAt=${local?.updatedAt}, " +
                    "remoteUpdatedAt=${remote.updatedAt}"
        )
        
        // Only upsert if:
        // 1. Local doesn't exist (new event from remote), OR
        // 2. Local is clean (not dirty) AND remote is newer
        if (local == null || (!local.isStained() && remote.updatedAt > local.updatedAt)) {
            dao.upsert(remote)
            com.ggetters.app.core.utils.Clogger.d(
                "OfflineEventRepository",
                "Applied remote event to Room: id=${remote.id}"
            )
        } else {
            com.ggetters.app.core.utils.Clogger.d(
                "OfflineEventRepository",
                "Skipped remote event (guarded): id=${remote.id}, " +
                        "reason=${if (local == null) "null local" else if (local.isStained()) "local dirty" else "remote not newer"}"
            )
        }
    }
}
