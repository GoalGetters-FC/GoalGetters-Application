package com.ggetters.app.data.repository.match

import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.remote.firestore.MatchEventFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Online implementation of MatchEventRepository.
 * Handles Firestore operations for match events.
 */
@Singleton
class OnlineMatchEventRepository @Inject constructor(
    private val matchEventFirestore: MatchEventFirestore
) : MatchEventRepository {
    
    override fun getEventsByMatchId(matchId: String): Flow<List<MatchEvent>> {
        return matchEventFirestore.getEventsByMatchId(matchId)
    }
    
    override fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEvent>> {
        return matchEventFirestore.getEventsByMatchIdAndType(matchId, eventType)
    }
    
    override suspend fun getEventById(eventId: String): MatchEvent? {
        return matchEventFirestore.getEventById(eventId)
    }
    
    override suspend fun insertEvent(event: MatchEvent) {
        matchEventFirestore.insertEvent(event)
    }
    
    override suspend fun updateEvent(event: MatchEvent) {
        matchEventFirestore.updateEvent(event)
    }
    
    override suspend fun deleteEvent(event: MatchEvent) {
        matchEventFirestore.deleteEvent(event)
    }
    
    override suspend fun deleteEventsByMatchId(matchId: String) {
        matchEventFirestore.deleteEventsByMatchId(matchId)
    }
    
    override suspend fun deleteEventById(eventId: String) {
        matchEventFirestore.deleteEventById(eventId)
    }
    
    override suspend fun getEventCountByMatchId(matchId: String): Int {
        return matchEventFirestore.getEventCountByMatchId(matchId)
    }

    override suspend fun refreshFromRemote(matchId: String) {
        // Online source is authoritative; no local cache to refresh here.
        // Combined repository will orchestrate the actual offline replacement.
    }

    suspend fun fetchEventsByMatchIdOnce(matchId: String): List<MatchEvent> {
        return matchEventFirestore.fetchEventsByMatchIdOnce(matchId)
    }
}
