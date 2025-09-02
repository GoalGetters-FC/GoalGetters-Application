// ✅ CombinedBroadcastRepository.kt
package com.ggetters.app.data.repository.broadcast

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Broadcast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedBroadcastRepository @Inject constructor(
    private val offline: OfflineBroadcastRepository,
    private val online: OnlineBroadcastRepository
) : BroadcastRepository {

    override fun all(): Flow<List<Broadcast>> = offline.all()

    override suspend fun getById(id: String): Broadcast? =
        offline.getById(id) ?: online.getById(id)

    override suspend fun upsert(entity: Broadcast) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to upsert broadcast online: ${e.message}")
        }
    }

    override suspend fun delete(entity: Broadcast) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete broadcast online: ${e.message}")
        }
    }

    override suspend fun deleteAll() {
        offline.deleteAll()
        try {
            online.deleteAll()
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete all broadcasts online: ${e.message}")
        }
    }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override fun sync() {
        TODO("Not yet implemented")
    }
}
