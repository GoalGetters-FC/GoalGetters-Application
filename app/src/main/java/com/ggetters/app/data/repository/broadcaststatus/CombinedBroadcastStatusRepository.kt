package com.ggetters.app.data.repository.broadcaststatus

import com.ggetters.app.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedBroadcastStatusRepository @Inject constructor(
    private val offline: OfflineBroadcastStatusRepository,
    private val online: OnlineBroadcastStatusRepository
) : BroadcastStatusRepository {

    override fun allForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>> =
        offline.allForBroadcast(broadcastId)

    override suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus? =
        offline.getById(broadcastId, recipientId)
            ?: online.getById(broadcastId, recipientId)

    override suspend fun upsert(entity: BroadcastStatus) {
        offline.upsert(entity)
        online.upsert(entity)
    }

    override suspend fun delete(entity: BroadcastStatus) {
        offline.delete(entity)
        online.delete(entity)
    }
}
