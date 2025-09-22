// app/src/main/java/com/ggetters/app/data/repository/lineup/CombinedLineupRepository.kt
package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.model.Lineup
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CombinedLineupRepository @Inject constructor(
    private val offline: OfflineLineupRepository,
    private val online: OnlineLineupRepository
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = offline.all()

    override suspend fun getById(id: String): Lineup? {
        // No runBlocking inside a suspend — just call both
        return offline.getById(id) ?: online.getById(id)
    }

    override suspend fun upsert(entity: Lineup) {
        offline.upsert(entity)
        online.upsert(entity)
    }

    override suspend fun delete(entity: Lineup) {
        offline.delete(entity)
        online.delete(entity)
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> =
        offline.getByEventId(eventId)

    override fun hydrateForTeam(id: String) {
        // TODO: implement if you want cross-layer hydration
    }

    override fun sync() {
        // TODO: implement WorkManager-driven sync if needed here
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
        online.deleteAll()
    }
}
