package com.ggetters.app.data.repository.lineup

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.Lineup
import com.google.firebase.perf.FirebasePerformance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class CombinedLineupRepository @Inject constructor(
    private val offline: OfflineLineupRepository,
    private val online: OnlineLineupRepository
) : LineupRepository {

    override fun all(): Flow<List<Lineup>> = offline.all()

    override suspend fun getById(id: String): Lineup? {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_getById")
        trace.start()
        try {
            val result = offline.getById(id) ?: online.getById(id)
            if (result != null) trace.putMetric("lineup_found", 1) else trace.putMetric("lineup_found", 0)
            return result
        } finally {
            trace.stop()
        }
    }

    override suspend fun upsert(entity: Lineup) {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_upsert")
        trace.start()
        try {
            Clogger.d("CombinedLineupRepository", "Upserting lineup: id=${entity.id}, eventId=${entity.eventId}, formation=${entity.formation}, spots.size=${entity.spots.size}")
            entity.spots.forEach { spot ->
                Clogger.d("CombinedLineupRepository", "  Spot: position=${spot.position}, userId=${spot.userId}, number=${spot.number}")
            }
            
            // Mark recent local edit to guard against immediate remote overwrite
            LineupLocalEditGuard.markEdited(entity.eventId)

            // Always save to offline first for persistence
            offline.upsert(entity)
            Clogger.d("CombinedLineupRepository", "Lineup saved to offline: ${entity.id}")
            
            // Try to save online, but don't fail if it doesn't work
            // Offline save already succeeded, so data is persisted
            runCatching {
            online.upsert(entity)
                Clogger.d("CombinedLineupRepository", "Lineup saved to both offline and online: ${entity.id}")
            }.onFailure { e ->
                Clogger.w("CombinedLineupRepository", "Lineup saved offline but online sync failed (will retry on next sync): ${e.message}")
                // Data is still persisted locally, so this is not a critical failure
            }
            
            trace.putMetric("lineup_upserted", 1)
        } finally {
            trace.stop()
        }
    }

    override suspend fun delete(entity: Lineup) {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_delete")
        trace.start()
        try {
            offline.delete(entity)
            online.delete(entity)
            trace.putMetric("lineup_deleted", 1)
        } finally {
            trace.stop()
        }
    }

    override fun getByEventId(eventId: String): Flow<List<Lineup>> = channelFlow {
        // Use a flag to track if we've emitted initial offline data
        var hasEmittedInitialOffline = false
        
        val offlineJob = launch {
            offline.getByEventId(eventId).collect { lineups ->
                Clogger.d("CombinedLineupRepository", "Offline Flow emitted for event=$eventId: lineups.size=${lineups.size}, hasEmittedInitialOffline=$hasEmittedInitialOffline")
                lineups.forEachIndexed { index, lineup ->
                    Clogger.d("CombinedLineupRepository", "  Offline Lineup[$index]: id=${lineup.id}, formation=${lineup.formation}, spots.size=${lineup.spots.size}")
                    lineup.spots.forEach { spot ->
                        Clogger.d("CombinedLineupRepository", "    Offline Spot: position=${spot.position}, userId=${spot.userId}")
                    }
                }
                
                // Always send offline data immediately so UI can show it
                // The remote job will handle merge logic and replace if needed
                send(lineups)
                if (!hasEmittedInitialOffline) {
                    hasEmittedInitialOffline = true
                }
            }
        }

        val remoteJob = launch {
            online.getByEventId(eventId).collect { remoteLineups ->
                runCatching<Unit> {
                    val localLineups = offline.getByEventId(eventId).first()
                    
                    when {
                        // Remote is empty
                        remoteLineups.isEmpty() -> {
                            if (localLineups.isEmpty()) {
                                // Both are empty, nothing to do
                                Clogger.d("CombinedLineupRepository", "No lineup data for event=$eventId")
                            } else {
                                // Remote is empty but local has data - preserve local data
                                // This happens when local changes haven't synced yet or sync failed
                                val latestLocal = localLineups.maxByOrNull { it.updatedAt }
                                val isRecent = latestLocal?.let { 
                                    // Consider local recent within 10 minutes to avoid premature overwrite by empty remote
                                    java.time.Duration.between(it.updatedAt, java.time.Instant.now()).seconds < 600
                                } ?: false
                                
                                if (isRecent) {
                                    Clogger.w(
                                        "CombinedLineupRepository",
                                        "Remote lineup empty for event=$eventId; preserving recent local lineup (updated ${java.time.Duration.between(latestLocal.updatedAt, java.time.Instant.now()).seconds}s ago). Attempting sync..."
                                    )
                                    // Try to sync the local data online (fire and forget)
                                    launch {
                runCatching {
                                            online.upsert(latestLocal)
                                            Clogger.d("CombinedLineupRepository", "Successfully synced local lineup to remote for event=$eventId")
                                        }.onFailure {
                                            Clogger.e("CombinedLineupRepository", "Failed to sync local lineup to remote: ${it.message}", it)
                                        }
                                    }
                        } else {
                            Clogger.w(
                                "CombinedLineupRepository",
                                        "Remote lineup empty for event=$eventId; preserving ${localLineups.size} local entries (likely unsynced changes)"
                                    )
                                }
                                // DO NOT delete local data - it may contain unsaved changes
                            }
                        }
                        // Remote has data - merge intelligently instead of blind replacement
                        else -> {
                            val normalized = remoteLineups.sortedByDescending { it.updatedAt }
                            val latestRemote = normalized.firstOrNull()
                            
                            // latestRemote cannot be null here since we're in the else branch (remoteLineups is not empty)
                            if (latestRemote == null) {
                                Clogger.w("CombinedLineupRepository", "Remote lineups list is not empty but firstOrNull returned null for event=$eventId")
                                return@runCatching
                            }
                            
                            when {
                                localLineups.isEmpty() -> {
                                    // Local is empty - but check if remote has empty spots
                                    // If remote has empty spots, this might be stale data that would overwrite user's work
                                    // Only replace if remote has actual spots data
                                    if (latestRemote.spots.isEmpty()) {
                                        Clogger.w(
                                            "CombinedLineupRepository",
                                            "Local empty but remote has empty spots for event=$eventId; skipping replacement to prevent data loss. This remote lineup is likely stale."
                                        )
                                        // Don't replace - keep local empty instead of accepting remote's empty spots
                                        // This prevents remote's stale empty lineup from overwriting when user adds players
                                    } else {
                                        // Guard: if there was a very recent local edit, do not replace yet
                                        if (LineupLocalEditGuard.wasRecentlyEdited(eventId)) {
                                            Clogger.w(
                                                "CombinedLineupRepository",
                                                "Recent local edit guard active; skipping replace with remote despite local empty for event=$eventId"
                                            )
                                            // Try to push local if any (no-op if none)
                                            launch { runCatching { localLineups.firstOrNull()?.let { online.upsert(it) } } }
                                        } else {
                                            Clogger.d("CombinedLineupRepository", "Local empty, replacing with remote (with spots) for event=$eventId")
                                            offline.replaceForEvent(eventId, normalized)
                                        }
                                    }
                                }
                                else -> {
                                    val latestLocal = localLineups.maxByOrNull { it.updatedAt }
                                    val remoteIsNewer = latestLocal == null || 
                                        latestRemote.updatedAt.isAfter(latestLocal.updatedAt)
                                    
                                    // CRITICAL: Always prioritize local data with spots over remote empty spots
                                    // This prevents data loss when remote syncs an empty lineup
                                    if (latestLocal != null && latestLocal.spots.isNotEmpty() && latestRemote.spots.isEmpty()) {
                                        // Local has spots, remote is empty - ALWAYS preserve local, regardless of timestamp
                                        Clogger.w(
                                            "CombinedLineupRepository",
                                            "Local lineup has spots but remote has empty spots for event=$eventId; preserving local spots to prevent data loss"
                                        )
                                        // Try to sync local to remote to fix the remote state
                                        launch {
                                            runCatching {
                                                online.upsert(latestLocal)
                                                Clogger.d("CombinedLineupRepository", "Successfully synced local lineup (with spots) to remote for event=$eventId")
                                            }.onFailure {
                                                Clogger.e("CombinedLineupRepository", "Failed to sync local lineup to remote: ${it.message}", it)
                                            }
                                        }
                                        // Don't replace - local data is the source of truth when it has spots
                                    } else {
                                        // Check if local was saved recently (within last 5 minutes) - protect it from being overwritten
                                        val localIsRecent = latestLocal?.let {
                                            java.time.Duration.between(it.updatedAt, java.time.Instant.now()).seconds < 300
                                        } ?: false
                                        val editGuardActive = LineupLocalEditGuard.wasRecentlyEdited(eventId)
                                        
                                        if (remoteIsNewer) {
                                            // Remote is newer - but ALWAYS preserve local spots if they exist and remote has empty spots
                                            // This is critical: never overwrite local spots with empty remote spots, regardless of timestamp
                                            if (latestRemote.spots.isEmpty() && latestLocal != null && latestLocal.spots.isNotEmpty()) {
                                                // Remote is newer but has empty spots, local has spots - ALWAYS preserve local spots
                                                Clogger.w(
                                                    "CombinedLineupRepository",
                                                    "Remote lineup newer for event=$eventId but has empty spots; preserving local spots to prevent data loss"
                                                )
                                                val mergedLineup = latestRemote.copy(
                                                    spots = latestLocal.spots,
                                                    // Update timestamp to reflect that we're preserving local data
                                                    updatedAt = java.time.Instant.now()
                                                )
                                                val mergedSet = normalized.map { lineup ->
                                                    if (lineup.id == latestRemote.id) mergedLineup else lineup
                                                }
                                                offline.replaceForEvent(eventId, mergedSet)
                                                // Also try to sync the merged lineup back to remote
                                                launch {
                                                    runCatching {
                                                        online.upsert(mergedLineup)
                                                        Clogger.d("CombinedLineupRepository", "Successfully synced merged lineup (with preserved spots) to remote for event=$eventId")
                                                    }.onFailure {
                                                        Clogger.e("CombinedLineupRepository", "Failed to sync merged lineup to remote: ${it.message}", it)
                                                    }
                                                }
                                            } else if (latestRemote.spots.isEmpty() && latestLocal != null && (localIsRecent || editGuardActive)) {
                                                // Remote has empty spots, local is recent but also has empty spots
                                                // Don't overwrite recent local save even if both are empty - local might be in the process of being populated
                                                Clogger.w(
                                                    "CombinedLineupRepository",
                                                    "Remote lineup newer with empty spots, but local was saved recently or edit guard is active (${ 
                                                        java.time.Duration.between(latestLocal.updatedAt, java.time.Instant.now()).seconds
                                                    }s ago) for event=$eventId; preserving local to prevent data loss during save"
                                                )
                                                // Try to sync local to remote instead
                                                launch {
                                                    runCatching {
                                                        latestLocal.let { online.upsert(it) }
                                                        Clogger.d("CombinedLineupRepository", "Successfully synced local lineup to remote for event=$eventId")
                                                    }.onFailure {
                                                        Clogger.e("CombinedLineupRepository", "Failed to sync local lineup to remote: ${it.message}", it)
                                                    }
                                                }
                                            } else if (latestRemote.spots.isEmpty() && latestLocal != null && !editGuardActive) {
                                                // Remote has empty spots, local also has empty spots but is not recent
                                                // Check if remote is MUCH newer (more than 20 minutes) before overwriting
                                                val remoteIsMuchNewer = latestRemote.updatedAt.isAfter(
                                                    latestLocal.updatedAt.plusSeconds(1200)
                                                )
                                                if (remoteIsMuchNewer) {
                                                    Clogger.d("CombinedLineupRepository", "Remote lineup much newer (10+ min) with empty spots, local also empty, replacing for event=$eventId")
                                                    offline.replaceForEvent(eventId, normalized)
                                                } else {
                                                    Clogger.d("CombinedLineupRepository", "Remote lineup newer with empty spots but not much newer, preserving local for event=$eventId")
                        }
                                            } else if (!editGuardActive) {
                                                // Remote is newer and has spots (or both are empty)
                                                // Additional guard: if local is recent, prefer preserving local to avoid user-visible resets
                                                if (localIsRecent) {
                                                    Clogger.w(
                                                        "CombinedLineupRepository",
                                                        "Remote newer with spots but local is recent; preserving local and attempting remote sync for event=$eventId"
                                                    )
                                                    launch {
                                                        runCatching {
                                                            latestLocal?.let { online.upsert(it) }
                                                        }.onFailure {
                                                            Clogger.e("CombinedLineupRepository", "Failed to sync recent local lineup to remote: ${it.message}", it)
                                                        }
                                                    }
                                                } else {
                                                    Clogger.d("CombinedLineupRepository", "Remote lineup newer with spots, replacing local for event=$eventId")
                                                    offline.replaceForEvent(eventId, normalized)
                                                }
                                            } else {
                                                Clogger.w(
                                                    "CombinedLineupRepository",
                                                    "Edit guard active; skipping remote overwrite despite remote newer for event=$eventId"
                                                )
                                            }
                                        } else {
                                            // Local is newer - preserve local data
                                            Clogger.w(
                                                "CombinedLineupRepository",
                                                "Local lineup newer than remote for event=$eventId; preserving local to prevent data loss"
                                            )
                                            // Try to sync local to remote
                                            launch {
                                                runCatching {
                                                    latestLocal?.let { online.upsert(it) }
                                                    Clogger.d("CombinedLineupRepository", "Successfully synced local lineup to remote for event=$eventId")
                                                }.onFailure {
                                                    Clogger.e("CombinedLineupRepository", "Failed to sync local lineup to remote: ${it.message}", it)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    Clogger.e(
                        "CombinedLineupRepository",
                        "Failed to process remote lineup for event=$eventId: ${it.message}",
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

    override fun hydrateForTeam(id: String) {
        // TODO: implement if you want cross-layer hydration
        // Wrap in a trace once implemented
    }

    override fun sync() {
        // TODO: implement WorkManager-driven sync if needed here
        // Add a Firebase trace here when you build out sync logic
    }

    override suspend fun deleteAll() {
        val trace = FirebasePerformance.getInstance().newTrace("lineuprepo_deleteAll")
        trace.start()
        try {
            offline.deleteAll()
            online.deleteAll()
            trace.putMetric("lineups_deleted_all", 1)
        } finally {
            trace.stop()
        }
    }
}
