package com.ggetters.app.data.repository.lineup

import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.remote.firestore.LineupFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class OnlineLineupRepository @Inject constructor(
    private val firestore: LineupFirestore
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = flow {
        emit(firestore.getAll())
    }

    override suspend fun getById(id: String): Lineup? {
        return runBlocking {
            firestore.getById(id)
        }
    }

//    override fun getById(id: String): Lineup? = runBlocking {
//        firestore.getById(id)
//    }

    override suspend fun upsert(entity: Lineup) {
        firestore.save(entity)
    }

    override suspend fun delete(entity: Lineup) {
        firestore.delete(entity)
    }

    override suspend fun deleteAll() {
        runBlocking {
            firestore.getAll().forEach { lineup ->
                firestore.delete(lineup)
            }
        }
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> = flow {
        emit(firestore.getByEventId(eventId))
    }

    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override fun sync() {
        TODO("Not yet implemented")
    }


}
