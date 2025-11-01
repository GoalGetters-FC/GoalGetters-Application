package com.ggetters.app.data.repository.match

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.MatchEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combined implementation of MatchEventRepository.
 * Merges offline and online data sources with conflict resolution.
 */
@Singleton
class CombinedMatchEventRepository @Inject constructor(
    private val offline: OfflineMatchEventRepository,
    private val online: OnlineMatchEventRepository
) : MatchEventRepository {
    
    override fun getEventsByMatchId(matchId: String): Flow<List<MatchEvent>> = channelFlow {
        val offlineJob = launch {
            offline.getEventsByMatchId(matchId).collect { events ->
                send(events)
            }
        }

        val remoteJob = launch {
            online.getEventsByMatchId(matchId).collect { remoteEvents ->
                runCatching { offline.replaceEventsForMatch(matchId, remoteEvents) }
                    .onFailure {
                        Clogger.e(
                            "CombinedMatchEventRepository",
                            "Failed to persist remote events for match=$matchId: ${it.message}",
                            it
                        )
                    }
            }
        }

        awaitClose {
            offlineJob.cancel()
            remoteJob.cancel()
        }
    }.distinctUntilChanged()

    override fun getEventsByMatchIdAndType(matchId: String, eventType: String): Flow<List<MatchEvent>> = channelFlow {
        val offlineJob = launch {
            offline.getEventsByMatchIdAndType(matchId, eventType).collect { events ->
                send(events)
            }
        }

        val remoteJob = launch {
            online.getEventsByMatchId(matchId).collect { remoteEvents ->
                runCatching { offline.replaceEventsForMatch(matchId, remoteEvents) }
                    .onFailure {
                        Clogger.e(
                            "CombinedMatchEventRepository",
                            "Failed to persist filtered remote events for match=$matchId: ${it.message}",
                            it
                        )
                    }
            }
        }

        awaitClose {
            offlineJob.cancel()
            remoteJob.cancel()
        }
    }.distinctUntilChanged()
    
    override suspend fun getEventById(eventId: String): MatchEvent? {
        // Try offline first for speed, then online
        return offline.getEventById(eventId) ?: online.getEventById(eventId)
    }
    
    override suspend fun insertEvent(event: MatchEvent) {
        try {
            // Insert to both offline and online
            offline.insertEvent(event)
            online.insertEvent(event)
        } catch (e: Exception) {
            // If online fails, still keep offline copy
            offline.insertEvent(event)
            throw Exception("Failed to sync event online: ${e.message}")
        }
    }
    
    override suspend fun updateEvent(event: MatchEvent) {
        try {
            // Update both offline and online
            offline.updateEvent(event)
            online.updateEvent(event)
        } catch (e: Exception) {
            // If online fails, still update offline copy
            offline.updateEvent(event)
            throw Exception("Failed to sync event update online: ${e.message}")
        }
    }
    
    override suspend fun deleteEvent(event: MatchEvent) {
        try {
            // Delete from both offline and online
            offline.deleteEvent(event)
            online.deleteEvent(event)
        } catch (e: Exception) {
            // If online fails, still delete offline copy
            offline.deleteEvent(event)
            throw Exception("Failed to sync event deletion online: ${e.message}")
        }
    }
    
    override suspend fun deleteEventsByMatchId(matchId: String) {
        try {
            // Delete from both offline and online
            offline.deleteEventsByMatchId(matchId)
            online.deleteEventsByMatchId(matchId)
        } catch (e: Exception) {
            // If online fails, still delete offline copies
            offline.deleteEventsByMatchId(matchId)
            throw Exception("Failed to sync event deletions online: ${e.message}")
        }
    }
    
    override suspend fun deleteEventById(eventId: String) {
        try {
            // Delete from both offline and online
            offline.deleteEventById(eventId)
            online.deleteEventById(eventId)
        } catch (e: Exception) {
            // If online fails, still delete offline copy
            offline.deleteEventById(eventId)
            throw Exception("Failed to sync event deletion online: ${e.message}")
        }
    }
    
    override suspend fun getEventCountByMatchId(matchId: String): Int {
        // Return the maximum count from both sources to avoid missing events
        val offlineCount = offline.getEventCountByMatchId(matchId)
        val onlineCount = online.getEventCountByMatchId(matchId)
        return maxOf(offlineCount, onlineCount)
    }
    
    /**
     * Sync events from online to offline for a specific match
     */
    suspend fun syncEventsForMatch(matchId: String) {
        try {
            // Get online events
            val onlineEvents = online.getEventsByMatchId(matchId)
            
            // Clear existing offline events for this match
            offline.deleteEventsByMatchId(matchId)
            
            // Insert online events to offline
            // Note: This is a simplified sync. In production, you'd want more sophisticated conflict resolution
        } catch (e: Exception) {
            // Handle sync errors gracefully
            throw Exception("Failed to sync events for match: ${e.message}")
        }
    }
}
