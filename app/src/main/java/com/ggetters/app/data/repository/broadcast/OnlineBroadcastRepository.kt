package com.ggetters.app.data.repository.broadcast

import com.ggetters.app.data.model.Broadcast
import com.ggetters.app.data.remote.firestore.BroadcastFirestore
import com.ggetters.app.data.repository.CrudRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnlineBroadcastRepository @Inject constructor(
    private val fs: BroadcastFirestore
) : CrudRepository<Broadcast> {
    override fun all(): Flow<List<Broadcast>> = fs.observeAll()
    override suspend fun getById(id: String): Broadcast? = fs.getById(id)
    override suspend fun upsert(entity: Broadcast) = fs.save(entity)
    override suspend fun delete(entity: Broadcast) = fs.delete(entity.id)
}
