package com.ggetters.app.data.repository.match

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory guard to avoid overwriting very recent local match-event edits
 * with remote emissions. Not persisted; only protects the active session.
 */
object MatchEventLocalEditGuard {
    private val lastEditByMatchId: ConcurrentHashMap<String, Instant> = ConcurrentHashMap()

    fun markEdited(matchId: String, at: Instant = Instant.now()) {
        if (matchId.isNotBlank()) {
            lastEditByMatchId[matchId] = at
        }
    }

    fun wasRecentlyEdited(matchId: String, windowSeconds: Long = 8L): Boolean {
        val last = lastEditByMatchId[matchId] ?: return false
        val elapsed = java.time.Duration.between(last, Instant.now()).seconds
        return elapsed in 0..windowSeconds
    }
}


