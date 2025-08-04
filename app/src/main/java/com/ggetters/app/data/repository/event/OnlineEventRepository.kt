package com.ggetters.app.data.repository.event

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.remote.firestore.EventFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Online (Firestore) implementation of EventRepository.
 * Handles remote data operations for events.
 * 
 * TODO: Backend - Implement proper error handling and retry logic
 * TODO: Backend - Add network connectivity checks
 * TODO: Backend - Implement rate limiting
 * TODO: Backend - Add proper security validation
 */
class OnlineEventRepository @Inject constructor(
    private val fs: EventFirestore
) : EventRepository {

    // TODO: Backend - Add network error recovery
    // TODO: Backend - Implement retry strategies
    // TODO: Backend - Add connection state management
    // TODO: Backend - Implement proper logging

    override fun all(): Flow<List<Event>> = fs.observeAll()

    override suspend fun getById(id: String): Event? = fs.getById(id)

    override suspend fun upsert(entity: Event) = fs.save(entity)

    override suspend fun delete(entity: Event) = fs.delete(entity.id)

    override suspend fun sync() {
        // TODO: Backend - Implement remote sync logic
        // This is handled by the CombinedEventRepository
    }

    override fun getByTeamId(teamId: String): Flow<List<Event>> = fs.observeByTeamId(teamId)

    override suspend fun getEventsByDateRange(
        teamId: String,
        startDate: String,
        endDate: String
    ): List<Event> {
        // TODO: Backend - Implement remote date range queries
        throw NotImplementedError("Remote date range queries not yet implemented")
    }

    override fun getEventsByType(teamId: String, category: Int): Flow<List<Event>> {
        // TODO: Backend - Implement remote event type filtering
        throw NotImplementedError("Remote event type filtering not yet implemented")
    }

    override fun getEventsByCreator(creatorId: String): Flow<List<Event>> {
        // TODO: Backend - Implement remote creator filtering
        throw NotImplementedError("Remote creator filtering not yet implemented")
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
}


    // TODO: Backend - Implement remote RSVP management
    // TODO: Backend - Add remote event validation
    // TODO: Backend - Implement remote event search
    // TODO: Backend - Add remote event analytics
    // TODO: Backend - Implement remote conflict detection
    // TODO: Backend - Add real-time event updates
