package com.ggetters.app.data.repository.event

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.remote.firestore.EventFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class OnlineEventRepository @Inject constructor(
    private val fs: EventFirestore
) : EventRepository {

    override fun all(): Flow<List<Event>> = flowOf(emptyList()) // avoid unscoped global

    suspend fun fetchAllForTeam(teamId: String): List<Event> =
        fs.fetchAllForTeam(teamId)

    override suspend fun getById(id: String): Event? = null // Use team-scoped variant
    suspend fun getById(teamId: String, id: String): Event? =
        fs.getById(teamId, id)

    override suspend fun upsert(entity: Event) =
        fs.upsert(entity.teamId, entity)

    override suspend fun delete(entity: Event) =
        fs.delete(entity.teamId, entity.id)

    override suspend fun deleteAll() { /* no-op */ }

    override suspend fun sync() { /* no-op */ }

    override fun getByTeamId(teamId: String): Flow<List<Event>> =
        fs.observeByTeamId(teamId)

    override suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ) = throw NotImplementedError()

    override fun getEventsByType(
        teamId: String,
        category: EventCategory
    ): Flow<List<Event>> = throw NotImplementedError()

    override fun getEventsByCreator(
        creatorId: String
    ): Flow<List<Event>> = throw NotImplementedError()

<<<<<<< HEAD
    override fun hydrateForTeam(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        Clogger.i("DevClass", "NEED-TO-DO-Deleting all events from remote Firestore")

//        runBlocking {
//            val allEvents = fs.getAll()
//            allEvents.forEach { event ->
//                fs.delete(event.id)
//            }
//        }
    }
=======
    override fun hydrateForTeam(id: String) { /* no-op */ }
>>>>>>> origin/staging
}
