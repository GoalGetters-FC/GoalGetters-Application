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
import java.util.UUID

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
                    val editGuardActive = MatchEventLocalEditGuard.wasRecentlyEdited(matchId)

                    if (editGuardActive) {
                                Clogger.w(
                                    "CombinedMatchEventRepository",
                            "Recent local event edit guard active; skipping remote overwrite for match=$matchId"
                                )
                    } else {
                            val normalized = remoteEvents.sortedWith(
                                compareByDescending<MatchEvent> { it.minute }
                                    .thenByDescending { it.timestamp }
                            )

                        // Never replace local with an empty remote list to avoid visible resets
                        if (normalized.isEmpty()) {
                                        Clogger.w(
                                            "CombinedMatchEventRepository",
                                "Remote emitted 0 events; skipping replace to avoid wipe (match=$matchId)"
                                        )
                            return@collect
                            }
                            
                        Clogger.d(
                            "CombinedMatchEventRepository",
                            "Applying ONLINE-preferred ${normalized.size} events to offline for match=$matchId (guard inactive)"
                        )
                                offline.replaceEventsForMatch(matchId, normalized)
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
                val editGuardActive = MatchEventLocalEditGuard.wasRecentlyEdited(matchId)
                runCatching {
                    if (editGuardActive) {
                                Clogger.w(
                                    "CombinedMatchEventRepository",
                            "Recent local event edit guard active; skipping filtered remote overwrite for match=$matchId"
                                )
                    } else {
                        val allRemoteEvents = remoteEvents.sortedWith(
                                compareByDescending<MatchEvent> { it.minute }
                                    .thenByDescending { it.timestamp }
                            )
                        if (allRemoteEvents.isEmpty()) {
                            val localNow = try { offline.getEventsByMatchId(matchId).first() } catch (_: Exception) { emptyList() }
                            if (localNow.isNotEmpty()) {
                                        Clogger.w(
                                            "CombinedMatchEventRepository",
                                    "Remote (filtered path) emitted 0 events but local has ${localNow.size}; skipping replace (match=$matchId)"
                                        )
                                return@collect
                            }
                        }
                        Clogger.d(
                            "CombinedMatchEventRepository",
                            "Applying ONLINE-preferred (filtered path) ${allRemoteEvents.size} events to offline for match=$matchId (guard inactive)"
                                )
                                offline.replaceEventsForMatch(matchId, allRemoteEvents)
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
        // Ensure a stable ID so Firestore writes do not fail silently
        val safe = if (event.id.isNullOrBlank()) event.copy(id = UUID.randomUUID().toString()) else event
        // Always save to offline first for persistence
        offline.insertEvent(safe)
        // Mark local edit guard for this match
        MatchEventLocalEditGuard.markEdited(safe.matchId)
        
        // Try to save online, but don't fail if it doesn't work
        // Offline save already succeeded, so data is persisted
        runCatching {
            online.insertEvent(safe)
            Clogger.d("CombinedMatchEventRepository", "Match event saved to both offline and online: ${safe.id}")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event saved offline but online sync failed (will retry on next sync): ${e.message}")
            // Data is still persisted locally, so this is not a critical failure
        }
    }
    
    override suspend fun updateEvent(event: MatchEvent) {
        // Ensure a stable ID on updates as well
        val safe = if (event.id.isNullOrBlank()) event.copy(id = UUID.randomUUID().toString()) else event
        // Always update offline first for persistence
        offline.updateEvent(safe)
        // Mark local edit guard for this match
        MatchEventLocalEditGuard.markEdited(safe.matchId)
        
        // Try to update online, but don't fail if it doesn't work
        runCatching {
            online.updateEvent(safe)
            Clogger.d("CombinedMatchEventRepository", "Match event updated in both offline and online: ${safe.id}")
        }.onFailure { e ->
            Clogger.w("CombinedMatchEventRepository", "Match event updated offline but online sync failed (will retry on next sync): ${e.message}")
        }
    }
    
    override suspend fun deleteEvent(event: MatchEvent) {
        // Always delete from offline first for consistency
        offline.deleteEvent(event)
        // Mark local edit guard for this match
        MatchEventLocalEditGuard.markEdited(event.matchId)
        
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
        // Mark local edit guard for this match
        MatchEventLocalEditGuard.markEdited(matchId)
        
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
    
    override suspend fun refreshFromRemote(matchId: String) {
        val editGuardActive = MatchEventLocalEditGuard.wasRecentlyEdited(matchId)
        if (editGuardActive) {
            Clogger.w(
                "CombinedMatchEventRepository",
                "Refresh skipped due to recent local edit for match=$matchId"
            )
            return
        }
        runCatching {
            val remote = online.fetchEventsByMatchIdOnce(matchId)
            if (remote.isEmpty()) {
                Clogger.w(
                    "CombinedMatchEventRepository",
                    "Manual refresh found 0 remote events; skipping replace (match=$matchId)"
                )
                return
            }
            Clogger.d(
                "CombinedMatchEventRepository",
                "Manual refresh applying ${remote.size} remote events → offline for match=$matchId"
            )
            offline.replaceEventsForMatch(matchId, remote)
        }.onFailure {
            Clogger.e(
                "CombinedMatchEventRepository",
                "Manual refresh failed for match=$matchId: ${it.message}",
                it
            )
        }
    }
    
    // Deprecated helper retained for reference; use refreshFromRemote instead.
}
