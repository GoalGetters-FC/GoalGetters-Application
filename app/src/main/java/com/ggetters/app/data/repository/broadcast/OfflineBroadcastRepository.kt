// ✅ OfflineBroadcastRepository.kt
package com.ggetters.app.data.repository.broadcast

import com.ggetters.app.data.local.dao.BroadcastDao
import com.ggetters.app.data.model.Broadcast
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineBroadcastRepository @Inject constructor(
    private val dao: BroadcastDao
) : BroadcastRepository {
    override fun all(): Flow<List<Broadcast>> = dao.getAll()
    override suspend fun getById(id: String): Broadcast? = dao.getById(id)
    override suspend fun upsert(entity: Broadcast) = dao.upsert(entity)
    override suspend fun delete(entity: Broadcast) = dao.delete(entity)

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override fun sync() {
        TODO("Not yet implemented")
    }
}