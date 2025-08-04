package com.ggetters.app.data.repository.broadcaststatus

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.BroadcastStatus
import com.ggetters.app.data.remote.firestore.BroadcastStatusFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class OnlineBroadcastStatusRepository @Inject constructor(
    private val fs: BroadcastStatusFirestore
) : BroadcastStatusRepository {

    override fun all(): Flow<List<BroadcastStatus>> = emptyFlow()

    override fun allForBroadcast(broadcastId: String): Flow<List<BroadcastStatus>> =
        fs.observeAll(broadcastId)

    override suspend fun getById(broadcastId: String, recipientId: String): BroadcastStatus? =
        fs.getById(broadcastId, recipientId)

    override suspend fun upsert(entity: BroadcastStatus) =
        fs.save(entity)

    override suspend fun delete(entity: BroadcastStatus) =
        fs.delete(entity.broadcastId, entity.recipientId)

    override suspend fun deleteAll() {
        Clogger.i("DevClass", "deleteAll called")
    }
}
