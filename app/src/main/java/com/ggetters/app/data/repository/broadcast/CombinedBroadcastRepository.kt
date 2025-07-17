package com.ggetters.app.data.repository.broadcast

import com.ggetters.app.data.model.Broadcast
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CombinedBroadcastRepository @Inject constructor(
    private val offline: OfflineBroadcastRepository,
    private val online: OnlineBroadcastRepository
) : CrudRepository<Broadcast> {                // ← use single type

    override fun all(): Flow<List<Broadcast>> =
        offline.all()

    override suspend fun getById(id: String): Broadcast? =
        offline.getById(id)                   // you could also fallback to online if you want

    override suspend fun upsert(entity: Broadcast) {
        offline.upsert(entity)
        online.upsert(entity)
    }

    override suspend fun delete(entity: Broadcast) {
        offline.delete(entity)
        online.delete(entity)
    }
}
