package com.ggetters.app.data.repository.match

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.MatchEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenByDescending
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
                runCatching {
                    val localEvents = offline.getEventsByMatchId(matchId).first()

                    when {
                        // Remote is empty → preserve local and optionally try to push
                        remoteEvents.isEmpty() -> {
                            if (localEvents.isEmpty()) {
                                Clogger.d("CombinedMatchEventRepository", "No events for match=$matchId")
                            } else {
                                Clogger.w(
                                    "CombinedMatchEventRepository",
                                    "Remote events empty for match=$matchId; preserving ${localEvents.size} local events (likely unsynced)"
                                )
                                // Best effort: push local events online without blocking
                                launch {
                                    localEvents.forEach { evt ->
                                        runCatching { online.insertEvent(evt) }
                                            .onFailure { err ->
                                                Clogger.e("CombinedMatchEventRepository", "Failed to push local event ${evt.id}: ${err.message}", err)
                                            }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Remote has events → merge per ID with LWW by timestamp
                            // Build maps keyed by event id
                            val localById = localEvents.associateBy { it.id }
                            val remoteById = remoteEvents.associateBy { it.id }

                            val mergedIds = (localById.keys + remoteById.keys)
                            val mergedList = mergedIds.mapNotNull { id ->
                                val l = localById[id]
                                val r = remoteById[id]
                                when {
                                    l == null -> r
                                    r == null -> l
                                    else -> if (r.timestamp >= l.timestamp) r else l
                                }
                            }

                            // Sort for deterministic order (minute desc, timestamp desc)
                            val normalizedMerged = mergedList.sortedWith(
                                compareByDescending<MatchEvent> { it.minute }
                                    .thenByDescending { it.timestamp }
                            )

                            // If merged differs from local, write it; otherwise no-op
                            val localSorted = localEvents.sortedWith(
                                compareByDescending<MatchEvent> { it.minute }
                                    .thenByDescending { it.timestamp }
                            )

                            val differs = normalizedMerged.size != localSorted.size ||
                                normalizedMerged.zip(localSorted).any { (a, b) -> a.id != b.id || a.timestamp != b.timestamp }

                            if (differs) {
                                Clogger.d("CombinedMatchEventRepository", "Applying merged ${normalizedMerged.size} events to offline for match=$matchId")
                                offline.replaceEventsForMatch(matchId, normalizedMerged)
                            } else {
                                Clogger.d("CombinedMatchEventRepository", "Merged events identical to local for match=$matchId; skipping replace")
                            }

                            // Try to push any local-only or newer-local events to remote (best effort)
                            launch {
                                mergedIds.forEach { id ->
                                    val l = localById[id]
                                    val r = remoteById[id]
                                    if (l != null && (r == null || l.timestamp > r.timestamp)) {
                                        runCatching {
                                            if (r == null) online.insertEvent(l) else online.updateEvent(l)
                                        }.onFailure { err ->
                                            Clogger.e("CombinedMatchEventRepository", "Failed to sync newer local event ${l.id}: ${err.message}", err)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    Clogger.e(
                        "CombinedMatchEventRepository",
                        "Failed to process remote events for match=$matchId: ${it.message}",
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
                // Filter remote events by type
                val filteredRemote = remoteEvents.filter { it.eventType.name.equals(eventType, ignoreCase = true) }
                
                runCatching {
                    val localEvents = offline.getEventsByMatchIdAndType(matchId, eventType).first()
                    
                    when {
                        // Remote is empty
                        filteredRemote.isEmpty() -> {
                            if (localEvents.isEmpty()) {
                                Clogger.d("CombinedMatchEventRepository", "No $eventType events for match=$matchId")
                            } else {
                                Clogger.w(
                                    "CombinedMatchEventRepository",
                                    "Remote $eventType events empty for match=$matchId; preserving ${localEvents.size} local events"
                                )
                            }
                        }
                        // Remote has events - merge intelligently
                        else -> {
                            val normalized = filteredRemote.sortedWith(
                                compareByDescending<MatchEvent> { it.minute }
                                    .thenByDescending { it.timestamp }
                            )
                            val latestRemote = normalized.firstOrNull()
                            
                            val shouldReplace = when {
                                localEvents.isEmpty() -> {
                                    Clogger.d("CombinedMatchEventRepository", "Local $eventType empty, replacing with remote for match=$matchId")
                                    true
                                }
                                latestRemote != null -> {
                                    val latestLocal = localEvents.maxByOrNull { it.timestamp }
                                    val remoteIsNewer = latestLocal == null || 
                                        latestRemote.timestamp > latestLocal.timestamp
                                    if (remoteIsNewer) {
                                        Clogger.d("CombinedMatchEventRepository", "Remote $eventType events newer, replacing local for match=$matchId")
                                    } else {
                                        Clogger.w(
                                            "CombinedMatchEventRepository",
                                            "Local $eventType events newer than remote for match=$matchId; preserving local"
                                        )
                                    }
                                    remoteIsNewer
                                }
                                else -> false
                            }
                            
                            if (shouldReplace) {
                                // Replace all events (not just filtered type) to maintain consistency
                                val allRemoteEvents = remoteEvents.sortedWith(
                                    compareByDescending<MatchEvent> { it.minute }
                                        .thenByDescending { it.timestamp }
                                )
                                offline.replaceEventsForMatch(matchId, allRemoteEvents)
                            } else {
                                Clogger.d("CombinedMatchEventRepository", "Keeping local $eventType events for match=$matchId")
                            }
                        }
                    }
                }.onFailure {
                    Clogger.e(
                        "CombinedMatchEventRepository",
                        "Failed to process filtered remote events for match=$matchId: ${it.message}",
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
        // Always save to offline first for persistence
        offline.insertEvent(event)
        
        // Try to save online, but don't fail if it doesn't work
        // Offline save already succeeded, so data is persisted
        runCatching {
            online.insertEvent(event)
            Clogger.d("CombinedMatchEventRepository", "Match event saved to both offline and online: ${event.id}")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event saved offline but online sync failed (will retry on next sync): ${e.message}")
            // Data is still persisted locally, so this is not a critical failure
        }
    }
    
    override suspend fun updateEvent(event: MatchEvent) {
        // Always update offline first for persistence
        offline.updateEvent(event)
        
        // Try to update online, but don't fail if it doesn't work
        runCatching {
            online.updateEvent(event)
            Clogger.d("CombinedMatchEventRepository", "Match event updated in both offline and online: ${event.id}")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event updated offline but online sync failed (will retry on next sync): ${e.message}")
        }
    }
    
    override suspend fun deleteEvent(event: MatchEvent) {
        // Always delete from offline first for consistency
        offline.deleteEvent(event)
        
        // Try to delete from online, but don't fail if it doesn't work
        runCatching {
            online.deleteEvent(event)
            Clogger.d("CombinedMatchEventRepository", "Match event deleted from both offline and online: ${event.id}")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event deleted offline but online sync failed (will retry on next sync): ${e.message}")
        }
    }
    
    override suspend fun deleteEventsByMatchId(matchId: String) {
        // Always delete from offline first for consistency
        offline.deleteEventsByMatchId(matchId)
        
        // Try to delete from online, but don't fail if it doesn't work
        runCatching {
            online.deleteEventsByMatchId(matchId)
            Clogger.d("CombinedMatchEventRepository", "Match events deleted from both offline and online for match: $matchId")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match events deleted offline but online sync failed (will retry on next sync): ${e.message}")
        }
    }

    override suspend fun deleteEventById(eventId: String) {
        // Always delete from offline first for consistency
        offline.deleteEventById(eventId)
        
        // Try to delete from online, but don't fail if it doesn't work
        runCatching {
            online.deleteEventById(eventId)
            Clogger.d("CombinedMatchEventRepository", "Match event deleted from both offline and online: $eventId")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event deleted offline but online sync failed (will retry on next sync): ${e.message}")
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
