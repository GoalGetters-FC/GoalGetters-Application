package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.model.Lineup
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class CombinedLineupRepository @Inject constructor(
    private val offline: OfflineLineupRepository,
    private val online: OnlineLineupRepository
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = offline.all()
    override suspend fun getById(id: String): Lineup? {
        return runBlocking {
            offline.getById(id) ?: online.getById(id)
        }
    }

//    override fun getById(id: String): Lineup? = runBlocking {
//        offline.getById(id) ?: online.getById(id)
//    }

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

    override suspend fun deleteAll() {
        runBlocking {
            offline.deleteAll()
            online.deleteAll()
        }
    }
}
