package com.ggetters.app.data.repository.broadcaststatus

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CombinedBroadcastStatusRepository @Inject constructor(
    private val offline: OfflineBroadcastStatusRepository,
    private val online: OnlineBroadcastStatusRepository
) : BroadcastStatusRepository {

    override fun all(): Flow<List<BroadcastStatus>> = offline.all()

    override fun allForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>> =
        offline.allForBroadcast(broadcastId)

    override suspend fun getById(broadcastId: String, recipientId: String) =
        offline.getById(broadcastId, recipientId) ?: online.getById(broadcastId, recipientId)

    override suspend fun upsert(entity: BroadcastStatus) {
        offline.upsert(entity)
        try {
            online.upsert(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to upsert status online: ${e.message}")
        }
    }

    override suspend fun delete(entity: BroadcastStatus) {
        offline.delete(entity)
        try {
            online.delete(entity)
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to delete status online: ${e.message}")
        }
    }
}
