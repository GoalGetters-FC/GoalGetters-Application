package com.ggetters.app.data.repository.broadcaststatus

import com.ggetters.app.data.local.dao.BroadcastStatusDao
import com.ggetters.app.data.model.BroadcastStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineBroadcastStatusRepository @Inject constructor(
    private val dao: BroadcastStatusDao
) : BroadcastStatusRepository {

    override fun all(): Flow<List<BroadcastStatus>> = dao.getAll()
    override fun allForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>> =
        dao.getAllForBroadcast(broadcastId)

    override suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus? =
        dao.getById(broadcastId, recipientId)

    override suspend fun upsert(entity: BroadcastStatus) =
        dao.upsert(entity)

    override suspend fun delete(entity: BroadcastStatus) =
        dao.delete(entity)

    override suspend fun deleteAll() {
        dao.deleteAll()
    }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }
}
