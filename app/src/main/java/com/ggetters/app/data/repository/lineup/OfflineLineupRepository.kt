package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.local.dao.LineupDao
import com.ggetters.app.data.model.Lineup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class OfflineLineupRepository @Inject constructor(
    private val dao: LineupDao
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = dao.getAll()

    override suspend fun getById(id: String): Lineup? {
        return runBlocking {
            dao.getById(id)
        }
    }

    override suspend fun upsert(entity: Lineup) {
        dao.save(entity)
    }

    override suspend fun delete(entity: Lineup) {
        dao.delete(entity)
    }

    override suspend fun deleteAll() {
        runBlocking {
            dao.deleteAll()
        }
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> =
        dao.getByEventId(eventId)
}


